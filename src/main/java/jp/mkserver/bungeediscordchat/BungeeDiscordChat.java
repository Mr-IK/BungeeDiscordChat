package jp.mkserver.bungeediscordchat;

import jp.mkserver.bungeediscordchat.commands.ReplyCommand;
import jp.mkserver.bungeediscordchat.commands.TellCommand;
import jp.mkserver.bungeediscordchat.japanizer.JapanizeType;
import jp.mkserver.bungeediscordchat.japanizer.Japanizer;
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

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.UUID;

public final class BungeeDiscordChat extends Plugin implements Listener{

    Config_file cf;
    Configuration config;
    Discord discord;
    public String prefix = "§7§l[§e§lB§b§lDiscord§7§l]§r";
    public HashMap<UUID,String> list;
    boolean connect = false;
    boolean power = true;
    HashMap<String, String> history;

    String bottoken = null;
    long channelid = -1;
    public String lunachat = null;
    HashMap<UUID,Long> link;
    HashMap<UUID,Long> links;
    @Override
    public void onEnable() {
        // Plugin startup logic
        cf = new Config_file(this);
        config = cf.getConfig();
        bottoken = config.getString("bottoken");
        channelid = config.getLong("channelid");
        lunachat = config.getString("lunachat");
        if(config.getString("translate").equalsIgnoreCase("true")) {
            Translate.TransEnable(this,config.getString("translation_api_key"));
        }
        link = new HashMap<>();
        list = new HashMap<>();
        history = new HashMap<>();
        links = FileManager.loadEnable(this);
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
        getProxy().getPluginManager().registerCommand(this,new TransCommand(this));
        //tell commandを置き換える
        for ( String command : new String[]{"tell", "msg", "message", "m", "w", "t"}) {
            getProxy().getPluginManager().registerCommand(this, new TellCommand(this, command));
        }
        //reply commandを置き換える
        for ( String command : new String[]{"reply", "r"}) {
            getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this, command));
        }
        getProxy().getPluginManager().registerListener(this, this);
        discord.sendMessage(":ballot_box_with_check: **サーバーが起動しました**");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        discord.sendMessage(":no_entry:  **サーバーが停止しました**");
    }

    @EventHandler
    public void onChat(ChatEvent e){
        if(e.isCommand()){
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String name = player.getName();
        String sname = ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName();
        String msg = repColor(e.getMessage());
        String msgs = "";
        if(lunachat.equalsIgnoreCase("true")) {
            msgs = Japanizer.japanize(msg,JapanizeType.GOOGLE_IME);
            if(!msgs.equalsIgnoreCase("")){
                msg = msg +" ("+msgs+")";
            }
        }
        //Discordにメッセージを送信
        discord.sendMessage("<"+name+"@"+sname+"> "+msg);
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
                players.sendMessage(new TextComponent("§d<"+name+"@"+sname+"> §f"+msg));
            }
        }
        getLogger().info(msg);
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
        discord.sendMessage(":x: **"+name+" さんがログアウトしました**");
    }
    @EventHandler
    public void onLogin(PreLoginEvent e) {
        String name = e.getConnection().getName();
        discord.sendMessage(":bangbang: **"+name+" さんがログインしました**");
    }
    @EventHandler
    public void onJoin(ServerConnectedEvent e) {
        String name = e.getPlayer().getName();
        String servername = e.getServer().getInfo().getName();
        discord.sendMessage(":arrow_right:  **"+name+" さんが "+servername+" サーバーにログインしました**");
    }
    @EventHandler
    public void onQuit(ServerDisconnectEvent e) {
        String name = e.getPlayer().getName();
        String servername = e.getTarget().getName();
        discord.sendMessage(":door: **"+name+" さんが "+servername+" サーバーからログアウトしました**");
    }

    public void sendBroadcast(String message) {
        if(!power){
            return;
        }
        ProxyServer.getInstance().broadcast(new TextComponent(message));
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

    //カラーコード除去
    public String repColor(String msg){
        String msgs = msg.replaceAll("&1","")
                .replaceAll("&2","").replaceAll("&3","")
                .replaceAll("&4","").replaceAll("&5","")
                .replaceAll("&6","").replaceAll("&7","")
                .replaceAll("&8","").replaceAll("&9","")
                .replaceAll("&0","").replaceAll("&l","")
                .replaceAll("&m","").replaceAll("&n","")
                .replaceAll("&o","").replaceAll("&a","")
                .replaceAll("&b","").replaceAll("&c","")
                .replaceAll("&d","").replaceAll("&e","")
                .replaceAll("&f","");
        return msgs;
    }

    public void putHistory(String reciever, String sender) {
        history.put(reciever, sender);
    }

    public String getHistory(String reciever) {
        return history.get(reciever);
    }


}
