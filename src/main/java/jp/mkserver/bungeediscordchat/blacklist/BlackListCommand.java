package jp.mkserver.bungeediscordchat.blacklist;

import jp.mkserver.bungeediscordchat.BungeeDiscordChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import static jp.mkserver.bungeediscordchat.blacklist.BlackListFile.getConfig;
import static jp.mkserver.bungeediscordchat.blacklist.BlackListFile.getData;

public class BlackListCommand extends Command {
    BungeeDiscordChat plugin;
    String prefix = "§7§l[§d§lM§8§lBlackList§7§l]§7";
    public static String prefixs = "§7§l[§d§lM§8§lBlackList§7§l]§7";
    public BlackListCommand(BungeeDiscordChat This) {
        super("mblacklist");
        plugin = This;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer)sender;
            if(!p.hasPermission("mblacklist.use")) {
                p.sendMessage(new TextComponent(prefix + "§c§l権限がありません！"));
                return;
            }
            if(args.length == 0){
                p.sendMessage(new TextComponent(prefix+"§2====ヘルプメニュー===="));
                p.sendMessage(new TextComponent(prefix + "§6/mblacklist list §f: ブラックリストを見る"));
                p.sendMessage(new TextComponent(prefix + "§6/mblacklist add [プレイヤー名] [メモ] §f: ブラックリスト追加"));
                p.sendMessage(new TextComponent(prefix + "§6/mblacklist remove [プレイヤー名] §f: ブラックリストから削除"));
                p.sendMessage(new TextComponent(prefix + "§6/mblacklist info [プレイヤー名] §f: プレイヤーの情報確認"));
                p.sendMessage(new TextComponent(prefix+"§2====================="));
                p.sendMessage(new TextComponent(prefix + "§c§lCreated by Mr_IK"));
                return;
            }else if(args.length == 1){
                if(args[0].equalsIgnoreCase("list")) {
                    p.sendMessage(new TextComponent(prefix + "§c§lブラックリスト"));
                    for(String key:getConfig().getKeys()) {
                        UUID uuid = UUID.fromString(key);
                        BlackListFile.BlackListData data = getData(uuid);
                        String message = prefix+"§7"+data.getmcid()+"("+data.getuuid()+") "+data.getmemo();
                        p.sendMessage(new TextComponent(message));
                    }
                    return;
                }
            }else if(args.length == 2){
                if(args[0].equalsIgnoreCase("remove")) {
                    if (ProxyServer.getInstance().getPlayer(args[1]) == null) {
                        p.sendMessage(new TextComponent(prefix + "§cそのプレイヤーはオフラインです"));
                        return;
                    }
                    if (BlackListFile.removeData(ProxyServer.getInstance().getPlayer(args[1]).getUniqueId())) {
                        p.sendMessage(new TextComponent(prefix + "§a成功しました"));
                        plugin.lists.remove(ProxyServer.getInstance().getPlayer(args[1]).getUniqueId());
                    } else {
                        p.sendMessage(new TextComponent(prefix + "§c失敗しました。おそらく存在しませんでした。"));
                    }
                    return;
                }else if(args[0].equalsIgnoreCase("info")){
                    if (ProxyServer.getInstance().getPlayer(args[1]) == null) {
                        p.sendMessage(new TextComponent(prefix + "§cそのプレイヤーはオフラインです"));
                        return;
                    }
                    BlackListFile.BlackListData data = BlackListFile.getData(ProxyServer.getInstance().getPlayer(args[1]).getUniqueId());
                    if(data == null){
                        p.sendMessage(new TextComponent(prefix + "§cそのプレイヤーはブラックリストに登録されていません"));
                        return;
                    }
                    p.sendMessage(new TextComponent(prefix + "§e"+data.getmcid()+"("+data.getuuid()+")　登録時刻: §a"+data.gettime()+" §eメモ: §a"+data.getmemo()));
                    return;
                }
            }else if(args.length == 3){
                if(args[0].equalsIgnoreCase("add")) {
                    if (ProxyServer.getInstance().getPlayer(args[1]) == null) {
                        p.sendMessage(new TextComponent(prefix + "§cそのプレイヤーはオフラインです"));
                        return;
                    }
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 a H時mm分ss秒");
                    if(BlackListFile.addData(args[1],ProxyServer.getInstance().getPlayer(args[1]).getUniqueId(),sdf.format(c.getTime()),args[2])){
                        p.sendMessage(new TextComponent(prefix + "§a追加に成功しました。"));
                        plugin.lists.add(ProxyServer.getInstance().getPlayer(args[1]).getUniqueId());
                    }else{
                        p.sendMessage(new TextComponent(prefix + "§a追加に失敗しました。恐らく既に存在します。"));
                    }
                    return;
                }
            }
            p.sendMessage(new TextComponent(prefix+"§2====ヘルプメニュー===="));
            p.sendMessage(new TextComponent(prefix + "§6/mblacklist list §f: ブラックリストを見る"));
            p.sendMessage(new TextComponent(prefix + "§6/mblacklist add [プレイヤー名] [メモ] §f: ブラックリスト追加"));
            p.sendMessage(new TextComponent(prefix + "§6/mblacklist remove [プレイヤー名] §f: ブラックリストから削除"));
            p.sendMessage(new TextComponent(prefix + "§6/mblacklist info [プレイヤー名] §f: プレイヤーの情報確認"));
            p.sendMessage(new TextComponent(prefix+"§2====================="));
            p.sendMessage(new TextComponent(prefix + "§c§lCreated by Mr_IK"));
        }
    }
}
