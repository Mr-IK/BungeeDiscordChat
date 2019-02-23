package jp.mkserver.bungeediscordchat.commands;

import jp.mkserver.bungeediscordchat.BungeeDiscordChat;
import jp.mkserver.bungeediscordchat.blacklist.BlackListCommand;
import jp.mkserver.bungeediscordchat.japanizer.JapanizeType;
import jp.mkserver.bungeediscordchat.japanizer.Japanizer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * BungeeJapanizeMessengerのtellコマンド実装クラス
 * @author ucchy
 */
public class TellCommand extends Command {

    static public ArrayList<UUID> viewlist;

    static public HashMap<UUID,List<UUID>> ignorelist;

    private BungeeDiscordChat bdc;

    /**
     * コンストラクタ
     * @param bdc
     * @param name コマンド
     */
    public TellCommand(BungeeDiscordChat bdc, String name) {
        super(name);
        this.bdc = bdc;
        viewlist = new ArrayList<>();
        ignorelist = new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        //プレイヤーからの個人チャットを拒否

        if(args.length==2&&args[0].equalsIgnoreCase("ignore")){
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
            if(target == null){
                sendMessage(sender, ChatColor.RED +
                        "実行例： /" + this.getName() + " ignore [player名]");
                return;
            }
            ProxiedPlayer p = (ProxiedPlayer) sender;
            List<UUID> uuid = ignorelist.get(p.getUniqueId());
            if(uuid==null){
                uuid = new ArrayList<>();
            }
            if(uuid.contains(target.getUniqueId())){
                uuid.remove(target.getUniqueId());
                ignorelist.put(p.getUniqueId(),uuid);
                sendMessage(sender, ChatColor.RED +
                        args[1]+" §7からの個人チャットを表示するようにしました。");
                return;
            }
            uuid.add(target.getUniqueId());
            ignorelist.put(p.getUniqueId(),uuid);
            sendMessage(sender, ChatColor.RED +
                    args[1]+" §7からの個人チャットを非表示にしました。\n表示するには同じコマンドをもう一度");
            return;
        }

        // 引数が足らないので、Usageを表示して終了する。
        if ( args.length <= 1 ) {
            sendMessage(sender, ChatColor.RED +
                    "実行例： /" + this.getName() + " <player> <message> : 個人チャットを送る");
            sendMessage(sender, ChatColor.RED +
                    "実行例： /" + this.getName() + " ignore [player名] : [player名]からの個人チャットを非表示/表示する");
            return;
        }

        // 自分自身には送信できない。
        if ( args[0].equals(sender.getName()) ) {
            sendMessage(sender, ChatColor.RED +
                    "自分自身にはプライベートメッセージを送信することができません。");
            return;
        }

        // 送信先プレイヤーの取得。取得できないならエラーを表示して終了する。
        ProxiedPlayer reciever = bdc.getProxy().getPlayer(args[0]);
        if ( reciever == null ) {
            sendMessage(sender, ChatColor.RED +
                    "メッセージ送信先 " + args[0] + " が見つかりません。");
            return;
        }

        // 送信メッセージの作成
        StringBuilder str = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            str.append(args[i] + " ");
        }
        String message = str.toString().trim();

        // 送信
        sendPrivateMessage(sender, reciever, message);
    }

    /**
     * プライベートメッセージを送信する
     * @param sender 送信者
     * @param reciever 受信者名
     * @param message メッセージ
     */
    protected void sendPrivateMessage(CommandSender sender, ProxiedPlayer reciever, String message) {

        //受け取りを拒否されていないかチェック
        ProxiedPlayer target = (ProxiedPlayer) sender;
        List<UUID> uuid = ignorelist.get(reciever.getUniqueId());
        if(uuid==null){
            uuid = new ArrayList<>();
        }
        if(uuid.contains(target.getUniqueId())){
            sendMessage(sender, ChatColor.RED +
                    reciever.getName()+" §7はあなたの個人チャットを拒否しています");
            return;
        }

        // Japanizeの付加
        String msg = ChatColor.translateAlternateColorCodes('&',message);
        String msgs = "";
        if(bdc.lunachat.equalsIgnoreCase("true")) {
            msgs = Japanizer.japanize(msg,JapanizeType.GOOGLE_IME);
            if(!msgs.equalsIgnoreCase("")){
                msg = msg +" §6("+msgs+")";
            }
        }

        // フォーマットの適用
        String senderServer = "console";
        if ( sender instanceof ProxiedPlayer ) {
            senderServer = ((ProxiedPlayer)sender).getServer().getInfo().getName();
        }


        String endmsg = "§7["+sender.getName()+"@"+senderServer+" > "+reciever.getName()+"@"+reciever.getServer().getInfo().getName()+"] §f"+msg;
        // メッセージ送信
        sendMessage(sender, endmsg);
        sendMessage(reciever, endmsg);
        //履歴をput
        bdc.putHistory(reciever.getName(), sender.getName());
        // コンソールに表示設定なら、コンソールに表示する
        bdc.getLogger().info(endmsg);
        // 権限もちで、かつviewモードがon さらに送信者でも受け取り者でもなければ 個チャを表示
        for ( String server : ProxyServer.getInstance().getServers().keySet() ) {
            ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
            for (ProxiedPlayer players : info.getPlayers()) {
                if(players.hasPermission("bd.op")){
                    if(viewlist.contains(players.getUniqueId())) {
                        if(!players.getName().equals(sender.getName())&&!players.getName().equals(reciever.getName())) {
                            players.sendMessage(TextComponent.fromLegacyText("§8[View]§r" + endmsg));
                        }
                    }
                }
            }
        }
    }


    /**
     * 指定した対象にメッセージを送信する
     * @param reciever 送信先
     * @param message メッセージ
     */
    protected void sendMessage(CommandSender reciever, String message) {
        if ( message == null ) return;
        reciever.sendMessage(TextComponent.fromLegacyText(message));
    }
}
