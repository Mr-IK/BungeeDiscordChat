package jp.mkserver.bungeediscordchat;

import jp.mkserver.bungeediscordchat.blacklist.BlackListCommand;
import jp.mkserver.bungeediscordchat.blacklist.BlackListFile;
import jp.mkserver.bungeediscordchat.commands.ReplyCommand;
import jp.mkserver.bungeediscordchat.commands.TellCommand;
import jp.mkserver.bungeediscordchat.japanizer.JapanizeType;
import jp.mkserver.bungeediscordchat.japanizer.Japanizer;
import jp.mkserver.bungeediscordchat.mat.MAT_BanSystem;
import jp.mkserver.bungeediscordchat.mat.WhiteListCommand;
import jp.mkserver.bungeediscordchat.mdice.MDiceCommand;
import jp.mkserver.bungeediscordchat.transrate.TransCommand;
import jp.mkserver.bungeediscordchat.transrate.Translate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import org.apache.lucene.search.spell.JaroWinklerDistance;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class BungeeDiscordChat extends Plugin implements Listener{
    Config_file cf;
    Configuration config;
    public Discord discord;
    public String prefix = "§7§l[§e§lB§b§lDiscord§7§l]§r";
    public HashMap<UUID,String> list;
    boolean connect = false;
    public boolean power = true;
    boolean joinquitmsg = false;
    HashMap<String, String> history;
    ArrayList<String> mutes;
    ArrayList<String> commandmutes;
    public ArrayList<UUID> lists;

    String bottoken = null;
    long channelid = -1;
    public String lunachat = null;
    HashMap<UUID,Long> link;
    HashMap<UUID,Long> links;

    int mutedtime = 30;
    int mutedpercent = 80;

    MDiceCommand mdice;

    /////////////////////////
    //M.A.T Ban System
    /////////////////////////
    MAT_BanSystem mat_ban;

    //////////////////////
    //M.A.T Whitelist System
    //////////////////////
    WhiteListCommand whiteListCommand;

    //////////////////////
    //M.A.T Command View System
    //////////////////////
    List<UUID> commandview;

    //////////////////////
    //M.A.T Bad Command System
    //////////////////////
    List<String> badcommands;

    @Override
    public void onEnable() {
        // Plugin startup logic
        cf = new Config_file(this);
        config = cf.getConfig();
        bottoken = config.getString("bottoken");
        channelid = config.getLong("channelid");
        lunachat = config.getString("lunachat");
        if(config.contains("mutedtime")){
            mutedtime = config.getInt("mutedtime");
        }
        commandview = new ArrayList<>();
        if(config.contains("commandview")){
            List<String> str = config.getStringList("commandview");
            for(String uuid : str){
                commandview.add(UUID.fromString(uuid));
            }
        }else{
            config.set("commandview",new ArrayList<>());
            cf.saveConfig();
        }
        badcommands = new ArrayList<>();
        if(config.contains("badcommands")){
            badcommands = config.getStringList("badcommands");
        }
        if(config.contains("mutedpercent")){
            mutedpercent = config.getInt("mutedpercent");
        }
        if(config.getString("translate").equalsIgnoreCase("true")) {
            Translate.TransEnable(this,config.getString("translation_api_key"));
        }
        if(config.getString("joinquitmsg").equalsIgnoreCase("on")) {
            joinquitmsg = true;
        }
        link = new HashMap<>();
        list = new HashMap<>();
        history = new HashMap<>();
        mutes = new ArrayList<>();
        if(config.contains("mutes")) {
            mutes.addAll(config.getStringList("mutes"));
        }
        commandmutes = new ArrayList<>();
        if(config.contains("commandmutes")) {
            commandmutes.addAll(config.getStringList("commandmutes"));
        }
        links = FileManager.loadEnable(this);
        lists = BlackListFile.loadEnable(this);
        try {
            getLogger().info("Connecting to bot…");
            discord = new Discord(this,bottoken);
            getLogger().info("Bot Connect complete!");
            getLogger().info("Channel Connecting…");
            if(discord.getChannel(channelid)){
                connect = true;
                getLogger().info("Channel Connect complete!");
            }else{
                getLogger().info("Channel Connect failed.");
            }
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            getLogger().info("Bot Connect failed.");
        }
        getProxy().getPluginManager().registerCommand(this, new MainCommand(this));
        getProxy().getPluginManager().registerCommand(this, new TransCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BlackListCommand(this));
        whiteListCommand = new WhiteListCommand(this);
        getProxy().getPluginManager().registerCommand(this, whiteListCommand);
        mdice = new MDiceCommand(this);
        getProxy().getPluginManager().registerCommand(this, mdice);
        //tell commandを置き換える
        for ( String command : new String[]{"tell", "msg", "message", "m", "w", "t"}) {
            getProxy().getPluginManager().registerCommand(this, new TellCommand(this, command));
        }
        //reply commandを置き換える
        for ( String command : new String[]{"reply", "r"}) {
            getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this, command));
        }
        getProxy().getPluginManager().registerListener(this, this);
        mat_ban = new MAT_BanSystem(this);
        discord.sendMessage(":ballot_box_with_check: **サーバーが起動しました**");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Thread th = new Thread(()->{
            getLogger().info("Ban data saving…");
            mat_ban.saveFile();
            getLogger().info("Ban data save complete!");
        });
        th.start();
        discord.sendMessage(":no_entry:  **サーバーが停止しました**");
    }

    public void addCommandViewList(UUID uuid){
        commandview.add(uuid);
        saveCommandViewList();
    }

    public void removeCommandViewList(UUID uuid){
        commandview.remove(uuid);
        saveCommandViewList();
    }

    public void saveCommandViewList(){
        config.set("commandview",commandview);
        cf.saveConfig();
    }

    public void addBadCommandList(String uuid){
        badcommands.add(uuid);
        saveBadCommandList();
    }

    public void removeBadCommandList(String uuid){
        badcommands.remove(uuid);
        saveBadCommandList();
    }

    public void saveBadCommandList(){
        config.set("badcommands",badcommands);
        cf.saveConfig();
    }

    HashMap<Integer,playerChatData> playerchatdatas = new HashMap<>();
    List<playerChatData> warningchatdatas = new ArrayList<>();

    class playerChatData {
        String name;
        UUID uuid;
        String msg;
        int outpoint;

        public void addpoint(){
            outpoint++;
        }
    }


    public boolean addold(ProxiedPlayer p,String msg){
        HashMap<Integer,playerChatData> copy = new HashMap<>();
        for(int i = 99;i>=0;i--){
            if(!playerchatdatas.containsKey(i)){
               continue;
            }
            playerChatData pcd = playerchatdatas.get(i);
            copy.put(i+1,pcd);
        }
        playerchatdatas = copy;
        playerChatData pcd = new playerChatData();
        pcd.uuid = p.getUniqueId();
        pcd.name = p.getName();
        pcd.msg = msg;
        pcd.outpoint = 0;
        playerchatdatas.put(0,pcd);
        return !checkSpaming();
    }


    public boolean checkSpaming(){
        playerChatData pcd = playerchatdatas.get(0);
        for(int i = 1;i<100;i++){
            if(!playerchatdatas.containsKey(i)){
                break;
            }
            playerChatData pcd_c = playerchatdatas.get(i);
            if(checkIttiritu(pcd.msg,pcd_c.msg)>=mutedpercent){
                pcd.addpoint();
                pcd_c.addpoint();
                if(pcd_c.outpoint>=10){
                    warningchatdatas.add(pcd_c);
                }
                playerchatdatas.put(i,pcd_c);
            }
        }
        playerchatdatas.put(0,pcd);
        for(playerChatData pc : warningchatdatas){
            if(checkIttiritu(pcd.msg,pc.msg)>=mutedpercent){
                pcd.addpoint();
                pc.addpoint();
                if(pc.outpoint>=15){
                    warningchatdatas.remove(pc);
                    timemutedplayer(pcd.uuid,mutedtime);
                    timemutedplayer(pc.uuid,mutedtime);
                    return true;
                }
                warningchatdatas.add(pc);
            }
        }
        return false;
    }

    @EventHandler
    public void onChat(ChatEvent e){
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if(e.isCommand()){
            if(commandmutes.contains(player.getUniqueId().toString())){
                player.sendMessage(new TextComponent(prefix+"§cあなたはコマンド実行を禁止されています"));
                e.setCancelled(true);
                return;
            }
            for(String str:badcommands){
                if(e.getMessage().startsWith(str)||e.getMessage().equalsIgnoreCase(str)){
                    if(!player.hasPermission("bd.op")){
                        e.setCancelled(true);
                        player.sendMessage(TextComponent.fromLegacyText("Unknown command. Type \"/help\" for help."));
                        for(ProxiedPlayer p : getProxy().getPlayers()){
                            if(p.hasPermission("bd.op")){
                                p.sendMessage(TextComponent.fromLegacyText("§c§l[C-Alert]§f§l不正なコマンドを検知: §d<"+player.getName()+"@"+((ProxiedPlayer) e.getSender()).getServer().getInfo().getName()+">§e: §c"+e.getMessage()));
                            }
                        }
                        return;
                    }
                }
            }
            for(UUID uuid:commandview){
                ProxiedPlayer p = getProxy().getPlayer(uuid);
                if(p!=null){
                    p.sendMessage(TextComponent.fromLegacyText("§8[C-View]§7<"+player.getName()+"@"+((ProxiedPlayer) e.getSender()).getServer().getInfo().getName()+">§e: §7"+e.getMessage()));
                }
            }
            return;
        }
        if(mutes.contains(player.getUniqueId().toString())){
            player.sendMessage(new TextComponent(prefix+"§cあなたはミュートされています"));
            e.setCancelled(true);
            return;
        }
        String name = player.getName();
        String sname = ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName();
        String msg = repColor(e.getMessage());
        if(!addold(player,msg)){
            player.sendMessage(new TextComponent(prefix+"§cあなたはミュートされています"));
            e.setCancelled(true);
            return;
        }
        if(mdice.d_now){
            int mynumber = 0;
            try{
                Integer s = Integer.valueOf(e.getMessage());
                if (s <= 0 || s > mdice.dmax){
                    player.sendMessage(new TextComponent(prefix + "§c§l1~"+mdice.dmax+"の数字を入力してください!"));
                    e.setCancelled(true);
                    return;
                }
                e.setCancelled(mdice.putMyNumberD(player,s));
                return;
            }catch (NumberFormatException ignored){

            }
        }
        String msgs = "";
        if(lunachat.equalsIgnoreCase("true")) {
            msgs = Japanizer.japanize(msg,JapanizeType.GOOGLE_IME);
            if(!msgs.equalsIgnoreCase("")){
                msg = msg +" ("+msgs+")";
            }
        }
        if(!mat_ban.DiscordLogSafetyMode){ //discordログセーフティモードがオンでなければ
            discord.sendMessage("<"+name+"@"+sname+"> "+msg); //Discordにメッセージを送信
        }
        //チャットした人のサーバー"以外"にメッセージを送信
        msg = ChatColor.translateAlternateColorCodes('&', e.getMessage());
        if(!msgs.equalsIgnoreCase("")){
            msg = msg +" §6("+msgs+")";
        }
        for ( String server : getProxy().getServers().keySet() ) {
            if ( server.equals(player.getServer().getInfo().getName()) ) {
                continue;
            }
            ServerInfo info = getProxy().getServerInfo(server);
            for ( ProxiedPlayer players : info.getPlayers() ) {
                players.sendMessage(TextComponent.fromLegacyText("§d<"+name+"@"+sname+"> §f"+msg));
            }
        }
        getLogger().info("<"+name+"@"+sname+"> "+msg);
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            if(list.containsKey(player.getUniqueId())){
                String[] args = list.get(player.getUniqueId()).split("/");
                String transmsg = Translate.Translates(repColor(e.getMessage()),args[0],args[1]);
                sendBroadcast("§7§l[§a"+args[0]+">"+args[1]+"§7§l]§r("+player.getName()+"): "+transmsg);
                discord.sendMessage("["+args[0]+">"+args[1]+"]("+player.getName()+"): "+transmsg);
            }
        });
    }
    @EventHandler
    public void onLogout(PlayerDisconnectEvent e){
        ProxiedPlayer player = e.getPlayer();
        String name = player.getName();
        if(!mat_ban.DiscordLogSafetyMode) { //discordログセーフティモードがオンでなければ
            discord.sendMessage(":x: **" + name + " さんがログアウトしました**");
        }
    }
    @EventHandler
    public void onLogin(PostLoginEvent e) {
        String name = e.getPlayer().getName();
        if(mat_ban.defaultBan.containsKey(e.getPlayer().getUniqueId())){
            e.getPlayer().disconnect(TextComponent.fromLegacyText("§f§l[§4§lM.A.T§f§l]\n§c§lあなたは以下の理由でこのサーバーからBanされています。\n"+mat_ban.defaultBan.get(e.getPlayer().getUniqueId())));
            return;
        }
        if(mat_ban.ipBan.containsKey(e.getPlayer().getAddress().getHostName())){
            e.getPlayer().disconnect(TextComponent.fromLegacyText("§f§l[§4§lM.A.T§f§l]\n§c§lあなたは以下の理由でこのサーバーからBanされています。\n"+mat_ban.ipBan.get(e.getPlayer().getAddress().getHostName())));
            return;
        }
        if(!whiteListCommand.enabledWhitelist.equalsIgnoreCase("")){
            String whitelistname = whiteListCommand.enabledWhitelist;
            if(whiteListCommand.listlist.containsKey(whitelistname)){
                List<UUID> plist = whiteListCommand.listlist.get(whitelistname);
                if(!plist.contains(e.getPlayer().getUniqueId())){
                    e.getPlayer().disconnect(TextComponent.fromLegacyText("§f§l[§4§lM.A.T§f§l]\n§c§lあなたは以下の理由でこのサーバーから切断されました。\nホワイトリストに入っていない"));
                    return;
                }
            }
        }
        if(whiteListCommand.listlist.containsKey("default")){
            List<UUID> plist = whiteListCommand.listlist.get("default");
            if(!plist.contains(e.getPlayer().getUniqueId())){
                plist.add(e.getPlayer().getUniqueId());
            }
        }
        if(!mat_ban.DiscordLogSafetyMode) { //discordログセーフティモードがオンでなければ
            discord.sendMessage(":bangbang: **" + name + " さんがログインしました**");
        }
        if(lists.contains(e.getPlayer().getUniqueId())){
            BlackListFile.upData(name,e.getPlayer().getUniqueId(),null,null);
            BlackListFile.BlackListData data = BlackListFile.getData(e.getPlayer().getUniqueId());
            String message = BlackListCommand.prefixs+"§7"+data.getmcid()+"("+data.getuuid()+") "+data.getmemo();
            for ( String server : getProxy().getServers().keySet() ) {
                ServerInfo info = getProxy().getServerInfo(server);
                for (ProxiedPlayer players : info.getPlayers()) {
                    if(players.hasPermission("mblacklist.use")){
                        players.sendMessage(TextComponent.fromLegacyText(BlackListCommand.prefixs+"§7§lブラックリスト入りしているプレイヤーがサーバーに参加しました。"));
                        players.sendMessage(TextComponent.fromLegacyText(message));
                    }
                }
            }
        }
    }
    @EventHandler
    public void onJoin(ServerConnectedEvent e) {
        String name = e.getPlayer().getName();
        String servername = e.getServer().getInfo().getName();
        if(joinquitmsg) {
            discord.sendMessage(":arrow_right:  **" + name + " さんが " + servername + " サーバーにログインしました**");
        }
    }
    @EventHandler
    public void onQuit(ServerDisconnectEvent e) {
        String name = e.getPlayer().getName();
        String servername = e.getTarget().getName();
        if(joinquitmsg) {
            discord.sendMessage(":door: **" + name + " さんが " + servername + " サーバーからログアウトしました**");
        }
    }

    public void sendBroadcast(String message) {
        if(!power){
            return;
        }
        ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(message));
    }

    //そのプレイヤがリンク済みかチェック
    public boolean link_contain_p(UUID uuid){
        if(links.containsKey(uuid)){
            return true;
        }
        return false;
    }

    //そのidがリンク済みかチェック
    public boolean link_contain_d(long id){
        if(links.containsValue(id)){
            return true;
        }
        return false;
    }

    //idからuuidをゲット
    public UUID link_get_p(long id){
        if(!link_contain_d(id)){
            return null;
        }
        for(UUID uuid:links.keySet()){
            Long ids =links.get(uuid);
            if(ids==id){
                return uuid;
            }
        }
        return null;
    }

    //uuidからidをゲット
    public long link_get_d(UUID uuid){
        if(!link_contain_p(uuid)){
            return -1;
        }
        return links.get(uuid);
    }

    //リンクを追加
    public void link_add(UUID uuid,long id){
        if(link_contain_p(uuid)){
            return;
        }
        FileManager.createConfig(uuid.toString(),id);
        links.put(uuid,id);
    }

    //リンクを削除
    public void link_remove(UUID uuid){
        if(!link_contain_p(uuid)){
            return;
        }
        FileManager.removeConfig(uuid.toString());
        links.remove(uuid);
    }

    //muteを追加
    public void mutedplayer(UUID uuid){
        mutes.add(uuid.toString());
        config.set("mutes",mutes);
        cf.saveConfig();
    }


    //時間制でmuteを追加
    public void timemutedplayer(UUID uuid,int min){
        mutedplayer(uuid);
        Timer timer = new Timer(false);
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                unmutedplayer(uuid);
                timer.cancel();
            }
        };
        timer.schedule(task, TimeUnit.MINUTES.toMillis(min));
    }

    //muteを削除
    public void unmutedplayer(UUID uuid){
        mutes.remove(uuid.toString());
        config.set("mutes",mutes);
        cf.saveConfig();
    }

    //cmuteを追加
    public void commandmutedplayer(UUID uuid){
        commandmutes.add(uuid.toString());
        config.set("mutes",commandmutes);
        cf.saveConfig();
    }

    //cmuteを削除
    public void commandunmutedplayer(UUID uuid){
        mutes.remove(uuid.toString());
        config.set("mutes",commandmutes);
        cf.saveConfig();
    }


    private static int checkIttiritu(String s1, String s2){
        // 入力チェックは割愛
        JaroWinklerDistance dis =  new JaroWinklerDistance();
        return (int) (dis.getDistance(s1, s2) * 100);
    }

    //カラーコード除去
    public String repColor(String msg){
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',msg));
    }

    public void putHistory(String reciever, String sender) {
        history.put(reciever, sender);
    }

    public String getHistory(String reciever) {
        return history.get(reciever);
    }


}
