package jp.mkserver.bungeediscordchat.mat;

import jp.mkserver.bungeediscordchat.BungeeDiscordChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.*;

public class WhiteListCommand extends Command {
    BungeeDiscordChat plugin;
    String prefix = "§f§l[§d§lM§f§lWhiteList§f§l]§f";
    public static String prefixs = "§f§l[§d§lM§f§lWhiteList§f§l]§f";

    WhiteListFileSystem file;
    Configuration config;
    public WhiteListCommand(BungeeDiscordChat This) {
        super("mwhitelist");
        plugin = This;
        file = new WhiteListFileSystem(This);
        config = file.getConfig();
        for(String key : config.getKeys()){
            List<String> strs = config.getStringList(key);
            listlist.put(key,stringlisttoUUIDlist(strs));
        }
    }

    public String enabledWhitelist = "";
    public HashMap<String,List<UUID>> listlist = new HashMap<>();

    public void saveFile(){
        for(String key:listlist.keySet()){
            config.set(key,uuidlisttoStringlist(listlist.get(key)));
        }
        file.saveConfig();
    }

    public List<String> uuidlisttoStringlist(List<UUID> uuids){
        List<String> str = new ArrayList<>();
        for(UUID uuid : uuids){
            str.add(uuid.toString());
        }
        return str;
    }

    public List<UUID> stringlisttoUUIDlist(List<String> uuids){
        List<UUID> str = new ArrayList<>();
        for(String uuid : uuids){
            str.add(UUID.fromString(uuid));
        }
        return str;
    }

    public void addmember(String listname,UUID uuid){
        if(listlist.containsKey(listname)){
            List<UUID> list = listlist.get(listname);
            list.add(uuid);
            listlist.put(listname,list);
        }else{
            List<UUID> list = new ArrayList<>();
            list.add(uuid);
            listlist.put(listname,list);
        }
        saveFile();
    }

    public boolean removemember(String listname,UUID uuid){
        if(listlist.containsKey(listname)) {
            List<UUID> list = listlist.get(listname);
            list.remove(uuid);
            if(list.size()!=0) {
                listlist.put(listname, list);
            }else{
                listlist.remove(listname);
            }
            saveFile();
            return true;
        }
        return false;
    }

    public boolean removeallmember(String listname){
        if(listlist.containsKey(listname)) {
            listlist.remove(listname);
            saveFile();
            return true;
        }
        return false;
    }


    public void viewWhitelist(ProxiedPlayer p,String listname){
        if(listname.equalsIgnoreCase("")){
            p.sendMessage(new TextComponent(prefix+"§eホワイトリスト一覧"));
            for(String key:listlist.keySet()) {
                p.sendMessage(new TextComponent(prefix+"§e"+key));
            }
        }else{
            p.sendMessage(new TextComponent(prefix+"§e"+listname+"のプレイヤー 一覧"));
            List<UUID> list = listlist.get(listname);
            for(UUID uuid:list){
                if(plugin.getProxy().getPlayer(uuid)==null){
                    p.sendMessage(new TextComponent(prefix+"§eOffline("+uuid.toString()+")"));
                }else{
                    p.sendMessage(new TextComponent(prefix+"§e"+plugin.getProxy().getPlayer(uuid).getName()+"("+uuid.toString()+")"));
                }
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer)sender;
            if(!p.hasPermission("mwhitelist.use")) {
                p.sendMessage(new TextComponent(prefix + "§c§l権限がありません！"));
                return;
            }
            if(args.length == 0){
                p.sendMessage(new TextComponent(prefix+"§2====ヘルプメニュー===="));
                p.sendMessage(new TextComponent(prefix + "§6/mwhitelist list (リスト名)§f: ホワイトリストのリストを見る(リスト名を入れるとプレイヤーリスト)"));
                p.sendMessage(new TextComponent(prefix + "§6/mwhitelist add [リスト名] [プレイヤー名] §f: ホワイトリスト追加"));
                p.sendMessage(new TextComponent(prefix + "§6/mwhitelist remove [リスト名] [プレイヤー名] §f: ホワイトリストから削除"));
                p.sendMessage(new TextComponent(prefix + "§6/mwhitelist clear [リスト名] §f: ホワイトリストを削除"));
                p.sendMessage(new TextComponent(prefix + "§6/mwhitelist enable [リスト名] §f: ホワイトリスト起動(変更)"));
                p.sendMessage(new TextComponent(prefix + "§6/mwhitelist disable §f: ホワイトリスト停止"));
                p.sendMessage(new TextComponent(prefix+"§2====================="));
                p.sendMessage(new TextComponent(prefix + "§c§lCreated by Mr_IK"));
                return;
            }else if(args.length == 1){
                if(args[0].equalsIgnoreCase("list")) {
                    viewWhitelist(p,"");
                    return;
                }else if(args[0].equalsIgnoreCase("disable")) {
                    enabledWhitelist = "";
                    p.sendMessage(new TextComponent(prefix + "§a停止に成功しました"));
                    return;
                }
            }else if(args.length == 2){
                if(args[0].equalsIgnoreCase("list")) {
                    viewWhitelist(p,args[1]);
                    return;
                }else if(args[0].equalsIgnoreCase("clear")) {
                    removeallmember(args[1]);
                    p.sendMessage(new TextComponent(prefix + "§a成功しました"));
                    return;
                }

                //起動・停止処理
                if(args[0].equalsIgnoreCase("enable")) {
                    enabledWhitelist = args[1];
                    p.sendMessage(new TextComponent(prefix + "§e"+args[1]+"§aの起動/上書きに成功しました"));
                    return;
                }
            }else if(args.length == 3){
                if(args[0].equalsIgnoreCase("add")) {
                    if (ProxyServer.getInstance().getPlayer(args[2]) == null) {
                        p.sendMessage(new TextComponent(prefix + "§cそのプレイヤーはオフラインです"));
                        return;
                    }
                    addmember(args[1],ProxyServer.getInstance().getPlayer(args[2]).getUniqueId());
                    p.sendMessage(new TextComponent(prefix + "§a成功しました"));
                    return;
                }else  if(args[0].equalsIgnoreCase("remove")) {
                    if (ProxyServer.getInstance().getPlayer(args[2]) == null) {
                        p.sendMessage(new TextComponent(prefix + "§cそのプレイヤーはオフラインです"));
                        return;
                    }
                    if(removemember(args[1],ProxyServer.getInstance().getPlayer(args[2]).getUniqueId())){
                        p.sendMessage(new TextComponent(prefix + "§a成功しました"));
                    }else {
                        p.sendMessage(new TextComponent(prefix + "§c失敗しました"));
                    }
                    return;
                    //起動・停止処理
                }else if(args[0].equalsIgnoreCase("enable")) {
                    enabledWhitelist = args[1];
                    p.sendMessage(new TextComponent(prefix + "§e"+args[1]+"§aの起動/上書きに成功しました"));
                    if(args[2].equalsIgnoreCase("allkick")){
                        for(ProxiedPlayer ps : plugin.getProxy().getPlayers()){
                            if(!enabledWhitelist.equalsIgnoreCase("")){
                                String whitelistname = enabledWhitelist;
                                if(listlist.containsKey(whitelistname)){
                                    List<UUID> plist = listlist.get(whitelistname);
                                    if(!plist.contains(ps.getUniqueId())){
                                         ps.disconnect(TextComponent.fromLegacyText("§f§l[§4§lM.A.T§f§l]\n§c§lあなたは以下の理由でこのサーバーから切断されました。\nホワイトリスト起動"));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    return;
                }
            }
            p.sendMessage(new TextComponent(prefix+"§2====ヘルプメニュー===="));
            p.sendMessage(new TextComponent(prefix + "§6/mwhitelist list (リスト名)§f: ホワイトリストのリストを見る(リスト名を入れるとプレイヤーリスト)"));
            p.sendMessage(new TextComponent(prefix + "§6/mwhitelist add [リスト名] [プレイヤー名] §f: ホワイトリスト追加"));
            p.sendMessage(new TextComponent(prefix + "§6/mwhitelist remove [リスト名] [プレイヤー名] §f: ホワイトリストから削除"));
            p.sendMessage(new TextComponent(prefix + "§6/mwhitelist clear [リスト名] §f: ホワイトリストを削除"));
            p.sendMessage(new TextComponent(prefix + "§6/mwhitelist enable [リスト名] (kickall) §f: ホワイトリスト起動(kickallを付け足すとホワリスに入ってない奴kickします)"));
            p.sendMessage(new TextComponent(prefix + "§6/mwhitelist disable §f: ホワイトリスト停止"));
            p.sendMessage(new TextComponent(prefix+"§2====================="));
            p.sendMessage(new TextComponent(prefix + "§c§lCreated by Mr_IK"));
        }
    }
}
