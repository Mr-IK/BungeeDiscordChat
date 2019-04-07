package jp.mkserver.bungeediscordchat.mdice;

import jp.mkserver.bungeediscordchat.BungeeDiscordChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.*;

public class MDiceCommand  extends Command {

    public boolean waittime = false;
    public boolean d_now = false;
    public int dmax = 100;
    HashMap<Integer,UUID> dmap = new HashMap<>();
    UUID d_owner = null;
    BungeeDiscordChat plugin;
    String prefix = "§l[§d§lM§f§la§a§ln§f§l10§5§lDice§f§l]";
    public static String prefixs = "§l[§d§lM§f§la§a§ln§f§l10§5§lDice§f§l]";
    Timer timer = new Timer();

    public void dstart(ProxiedPlayer starter,int max){
        if(!d_now){
            d_now = true;
            dmax = max;
            starter.sendMessage(new TextComponent(prefix+"§a"+max+"Dを開始しました！"));
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(prefix + starter.getDisplayName() + "§d§lさんが§e§l"+max+"D§d§lをスタートしました！§a§l(半角数字のみだけ入力してください！)"));
            d_owner = starter.getUniqueId();
            TimerTask dtask = new TimerTask() {
                @Override
                public void run() {
                    int result = rollDice(ProxyServer.getInstance().getPlayer(d_owner),1,max);
                    if (dmap.containsKey(result)) {
                        ProxiedPlayer nowplayer = ProxyServer.getInstance().getPlayer(dmap.get(result));
                        if(nowplayer != null){
                            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(prefix + "§5§l§n" + nowplayer.getName() + "§a§lさんが§e§lピタリと§6§l当てました！！！"));
                        }
                    }
                    if (dmap.containsKey(result-1)) {
                        ProxiedPlayer nowplayer = ProxyServer.getInstance().getPlayer(dmap.get(result-1));
                        if(nowplayer != null){
                            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(prefix + "§b§l" + ProxyServer.getInstance().getPlayer(dmap.get(result-1)) + "§6§lさんが１少ない前後で当てました！"));
                        }
                    }
                    if (dmap.containsKey(result+1)) {
                        ProxiedPlayer nowplayer = ProxyServer.getInstance().getPlayer(dmap.get(result+1));
                        if(nowplayer != null){
                            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(prefix + "§b§l"  +ProxyServer.getInstance().getPlayer(dmap.get(result+1)) + "§6§lさんが１多い前後で当てました！"));
                        }
                    }
                    dreset();
                }
            };
            timer = new Timer();
            timer.schedule(dtask, 20000);
        }else{
            starter.sendMessage(new TextComponent(prefix+"§c現在D中です！"));
        }
    }

    public void dreset(){
        d_owner = null;
        dmap.clear();
        d_now = false;
        dmax = 100;
    }

    public void waitstart(){
        waittime = true;
        TimerTask dtask = new TimerTask() {
            @Override
            public void run() {
                waittime = false;
            }
        };
        timer = new Timer();
        timer.schedule(dtask, 4000);
    }

    public boolean putMyNumberD(ProxiedPlayer p,int num){
        if(dmap.containsValue(p.getUniqueId())){
            p.sendMessage(new TextComponent(prefix + "§a§lあなたはもう数字を言いました"));
            return true;
        }

        if(dmap.containsKey(num)){
            ProxiedPlayer nowplayer = ProxyServer.getInstance().getPlayer(dmap.get(num));
            if(nowplayer == null){
                p.sendMessage(new TextComponent(prefix + "§e§l" + num + "§a§lと回答しました!"));
                ProxyServer.getInstance().getPlayer(d_owner).sendMessage(new TextComponent(prefix + p.getName() + "さんが" + "§e§l" + num + "§a§lと回答しました"));

                dmap.put(num,p.getUniqueId());
                return true;
            }
            p.sendMessage(new TextComponent(prefix + "§c§lすでにその数字は言われています！"));
            return true;
        }

        p.sendMessage(new TextComponent(prefix + "§e§l" + num + "§a§lと回答しました!"));
        ProxyServer.getInstance().getPlayer(d_owner).sendMessage(new TextComponent(prefix + p.getName() + "さんが" + "§e§l" + num + "§a§lと回答しました"));

        dmap.put(num,p.getUniqueId());
        return true;

    }

    public MDiceCommand(BungeeDiscordChat This) {
        super("mdice");
        plugin = This;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer)sender;
            //opのみ使いたいとき
            //if(!p.hasPermission("bd.op")) {
            //    p.sendMessage(new TextComponent(prefix + "§c§l権限がありません！"));
            //    return;
            //}
            if(args.length == 0){
                p.sendMessage(new TextComponent(prefix+"§2====ヘルプメニュー===="));
                p.sendMessage(new TextComponent(prefix + "§f/mdice [数字] : ダイスを回す"));
                p.sendMessage(new TextComponent(prefix + "§6/mdice [数字A] [数字B] : [数字A]が最小、[数字B]が最大のダイスを回す"));
                if(p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(prefix + "§6/mdice [数字]d : [数字]Dをする(OP専用)"));
                }
                p.sendMessage(new TextComponent(prefix+"§2====================="));
                p.sendMessage(new TextComponent(prefix + "§c§lCreated by Mr_IK"));
                return;
            }else if(args.length == 1){
                try{
                    int put = Integer.parseInt(args[0]);
                    if(put <= 0){
                        p.sendMessage(new TextComponent(prefix + "§c１以上の数字を入力してください！"));
                        return;
                    }
                    if (!p.hasPermission("bd.op")) {
                        if(waittime){
                            p.sendMessage(new TextComponent(prefix + "§c現在クールタイム中です！"));
                            return;
                        }
                        waitstart();
                    }
                    rollDice(p,1,put);
                }catch (NumberFormatException e){
                    if(args[0].endsWith("d")||args[0].endsWith("D")){
                        if (!p.hasPermission("bd.op")) {
                            p.sendMessage(new TextComponent(prefix + "§cあなたは権限を持っていません！"));
                            return;
                        }
                        String puts = args[0].replace("d","").replace("D","");
                        try{
                            int put = Integer.parseInt(puts);
                            dstart(p,put);
                            return;
                        }catch (NumberFormatException e1){
                            p.sendMessage(new TextComponent(prefix + "§c数字を入力してください！"));
                            return;
                        }
                    }
                    p.sendMessage(new TextComponent(prefix + "§c数字を入力してください！"));
                    return;
                }
                return;
            }else if(args.length == 2){
                try{
                    int put = Integer.parseInt(args[0]);
                    int put2 = Integer.parseInt(args[1]);
                    if(put <= 0||put2 <= 0){
                        p.sendMessage(new TextComponent(prefix + "§c１以上の数字を入力してください！"));
                        return;
                    }
                    if(put > put2){
                        p.sendMessage(new TextComponent(prefix + "§c最小値より最大値が小さいです！"));
                        return;
                    }
                    if (!p.hasPermission("bd.op")) {
                        if(waittime){
                            p.sendMessage(new TextComponent(prefix + "§c現在クールタイム中です！"));
                            return;
                        }
                        waitstart();
                    }
                    rollDice(p,put,put2);
                }catch (NumberFormatException e){
                    p.sendMessage(new TextComponent(prefix + "§c数字を入力してください！"));
                    return;
                }
                return;
            }
            p.sendMessage(new TextComponent(prefix+"§2====ヘルプメニュー===="));
            p.sendMessage(new TextComponent(prefix + "§f/mdice [数字] : ダイスを回す"));
            p.sendMessage(new TextComponent(prefix + "§6/mdice [数字A] [数字B] : [数字A]が最小、[数字B]が最大のダイスを回す"));
            if(p.hasPermission("bd.op")) {
                p.sendMessage(new TextComponent(prefix + "§6/mdice [数字]d : [数字]Dをする(OP専用)"));
            }
            p.sendMessage(new TextComponent(prefix+"§2====================="));
            p.sendMessage(new TextComponent(prefix + "§c§lCreated by Mr_IK"));
        }
    }

    public int rollDice(ProxiedPlayer p,int min,int max){
        int result = rollDice(min,max);
        if(min==1){
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(prefix + "§3§l" + p.getDisplayName() + "§3§lは§l" + ChatColor.YELLOW + "§l" + max + "§3§l面サイコロを振って" + ChatColor.YELLOW + "§l" + result + "§3§lが出た"));
        }else{
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(prefix + "§3§l" + p.getDisplayName() + "§3§lは最小" + ChatColor.YELLOW + "§l"+ min + "§3§l面、最大" + ChatColor.YELLOW + "§l" + max + "§3§l面サイコロを振って" + ChatColor.YELLOW + "§l" + result + "§3§lが出た"));
        }
        return result;
    }

    //minが1,maxが8の場合
    //r.nextInt(8-1+1) + 1;
    public int rollDice(int min,int max){
        Random r = new Random();
        return r.nextInt(max-min+1) + min;
    }
}
