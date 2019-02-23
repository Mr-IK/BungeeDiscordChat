package jp.mkserver.bungeediscordchat;

import jp.mkserver.bungeediscordchat.commands.TellCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class MainCommand extends Command {
    BungeeDiscordChat plugin;
    public MainCommand(BungeeDiscordChat This) {
        super("bd");
        plugin = This;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            if(args.length == 0){
                plugin.getLogger().info("bd check : 正常に接続しているかチェックします");
                plugin.getLogger().info("bd info [Player名] : 他のプレイヤーのリンク状況を確認します");
                plugin.getLogger().info("bd on : 機能を起動します");
                plugin.getLogger().info("bd off : 機能を停止します(緊急時のみ使うこと)");
                plugin.getLogger().info("bd title <main>//<sub>//<time> : (おまけ機能)全プレイヤーにタイトルを表示");
                plugin.getLogger().info("bd msg [内容] : Discordにメッセージを送信します");
                plugin.getLogger().info("Created by Mr_IK");
            }else if(args.length == 1){
                if(args[0].equalsIgnoreCase("check")){
                    if(plugin.connect){
                        plugin.getLogger().info("正常に接続されています");
                        plugin.getLogger().info("BOTトークン: §f"+plugin.bottoken);
                        plugin.getLogger().info("サーバー名: §f"+plugin.discord.channel.getGuild().getName());
                        plugin.getLogger().info("チャンネル名: §f"+plugin.discord.channel.getName());
                    }else{
                        plugin.getLogger().info("接続できていません。ボットトークン・チャンネルIDを見直してください。");
                    }
                    return;
                }else if(args[0].equalsIgnoreCase("on")){
                    plugin.power = true;
                    plugin.getLogger().info("§aBdiscordの機能を開放しました。");
                    return;
                }else if(args[0].equalsIgnoreCase("off")){
                    plugin.power = false;
                    plugin.getLogger().info("§aBdiscordの機能を停止しました。");
                    return;
                }
            }else if(args.length == 2){
                if(args[0].equalsIgnoreCase("info")){
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                    if (player != null) {
                        if(!plugin.link_contain_p(player.getUniqueId())){
                            plugin.getLogger().info(args[1]+" さんは誰ともリンクしていません");
                            return;
                        }
                        plugin.getLogger().info(args[1]+"さんは "+plugin.discord.getName_link(plugin.discord.jda.getUserById(plugin.links.get(player.getUniqueId())))+" さんとリンクしています");
                        return;
                    }else {
                        plugin.getLogger().info("そのプレイヤーは現在オフラインです");
                        return;
                    }
                }else if(args[0].equalsIgnoreCase("title")) {
                    String[] tt = args[1].split("//");
                    if (tt.length > 3 || tt.length == 0) {
                        plugin.getLogger().info("コマンドが間違っています");
                        plugin.getLogger().info("bd title <main>//<sub>//<time>");
                        return;
                    }
                    Title title = ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.translateAlternateColorCodes('&', tt[0])));
                    if (tt.length >= 2) {
                        title.subTitle(new TextComponent(ChatColor.translateAlternateColorCodes('&', tt[1])));
                    }
                    int time = 100;
                    if (tt.length == 3) {
                        try {
                            time = Integer.parseInt(tt[2]) * 20;
                        } catch (NumberFormatException e) {
                            plugin.getLogger().info("timeは数字(秒数)で入力してください");
                            plugin.getLogger().info("bd title <main>//(sub)//(time)");
                            return;
                        }
                    }
                    title.stay(time);
                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                        player.sendTitle(title);
                    }
                    return;
                }else if(args[0].equalsIgnoreCase("msg")){
                    plugin.discord.sendMessage(":mega: "+args[1]);
                    return;
                }
            }
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if(!plugin.power&&!p.hasPermission("bd.op")){
            p.sendMessage(new TextComponent(plugin.prefix+"§4機能停止中"));
            return;
        }
        if(args.length == 0){
            p.sendMessage(new TextComponent(plugin.prefix+"§2====ヘルプメニュー===="));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd link §f: Discordアカウントとマイクラをリンクします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd info §f: リンク情報をチェックします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd ignore §f: リンク申請を拒否します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd unlink §f: Discordアカウントとマイクラのリンクを削除します"));
            if(p.hasPermission("bd.op")) {
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd check : 正常に接続しているかチェックします"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd info [Player名] : 他のプレイヤーのリンク状況を確認します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd msg [内容] : Discordにメッセージを送信します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd view : 個人チャットの表示を切り替えます"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd on : 機能を起動します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd off : 機能を停止します(緊急時のみ使うこと)"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd mute [player名] : 特定のプレイヤーをmuteします"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd unmute [player名] : muteを解除します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd cmmute [player名] : 特定のプレイヤーをコマンドmuteします"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd cmunmute [player名] : コマンドmuteを解除します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd amute [player名] : 特定のプレイヤーを2種類ともmuteします"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd aunmute [player名] : 2種類のmuteを解除します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//<sub>//<time> : (おまけ機能)全プレイヤーにタイトルを表示"));
                if(!plugin.power){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4§l機能停止中"));
                }
            }
            p.sendMessage(new TextComponent(plugin.prefix+"§2====================="));
            p.sendMessage(new TextComponent(plugin.prefix + "§c§lCreated by Mr_IK || v1.6.0"));
            return;
        }else if(args.length == 1){
            if(args[0].equalsIgnoreCase("check")){
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                if(plugin.connect){
                    p.sendMessage(new TextComponent(plugin.prefix+"§a正常に接続されています"));
                    p.sendMessage(new TextComponent(plugin.prefix+"§aBOTトークン: §f"+plugin.bottoken));
                    p.sendMessage(new TextComponent(plugin.prefix+"§aサーバー名: §f"+plugin.discord.channel.getGuild().getName()));
                    p.sendMessage(new TextComponent(plugin.prefix+"§aチャンネル名: §f"+plugin.discord.channel.getName()));
                }else{
                    p.sendMessage(new TextComponent(plugin.prefix+"§c接続できていません。ボットトークン・チャンネルIDを見直してください。"));
                }
                return;
            }else if(args[0].equalsIgnoreCase("link")){
                if(!plugin.link.containsKey(p.getUniqueId())){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたにリンク申請は来ていません"));
                    return;
                }
                plugin.link_add(p.getUniqueId(),plugin.link.get(p.getUniqueId()));
                p.sendMessage(new TextComponent(plugin.prefix+"§aあなたは "+plugin.discord.getName_link(plugin.discord.jda.getUserById(plugin.link.get(p.getUniqueId())))+" さんとリンクしました。"));
                plugin.discord.jda.getUserById(plugin.link.get(p.getUniqueId())).openPrivateChannel().complete().sendMessage("あなたは "+p.getName()+"("+p.getUniqueId()+") さんとリンクしました。").queue();
                plugin.link.remove(p.getUniqueId());
                return;
            }else if(args[0].equalsIgnoreCase("unlink")){
                if(!plugin.link_contain_p(p.getUniqueId())){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたは誰ともリンクしていません"));
                    return;
                }
                plugin.link_remove(p.getUniqueId());
                p.sendMessage(new TextComponent(plugin.prefix+"§cリンクを解除しました"));
                return;
            }else if(args[0].equalsIgnoreCase("ignore")){
                if(!plugin.link.containsKey(p.getUniqueId())){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたにリンク申請は来ていません"));
                    return;
                }
                p.sendMessage(new TextComponent(plugin.prefix+"§cあなたは "+plugin.discord.getName_link(plugin.discord.jda.getUserById(plugin.link.get(p.getUniqueId())))+" さんからの申請を拒否しました。"));
                plugin.discord.jda.getUserById(plugin.link.get(p.getUniqueId())).openPrivateChannel().complete().sendMessage(p.getName()+"("+p.getUniqueId()+") さんが申請を拒否しました。").queue();
                plugin.link.remove(p.getUniqueId());
                return;
            }else if(args[0].equalsIgnoreCase("info")){
                if(!plugin.link_contain_p(p.getUniqueId())){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたは誰ともリンクしていません"));
                    return;
                }
                p.sendMessage(new TextComponent(plugin.prefix+"§aあなたは "+plugin.discord.getName_link(plugin.discord.jda.getUserById(plugin.links.get(p.getUniqueId())))+" さんとリンクしています"));
                return;
            }else if(args[0].equalsIgnoreCase("on")){
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                plugin.power = true;
                p.sendMessage(new TextComponent(plugin.prefix+"§aBdiscordの機能を開放しました。"));
                return;
            }else if(args[0].equalsIgnoreCase("off")){
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                plugin.power = false;
                p.sendMessage(new TextComponent(plugin.prefix+"§aBdiscordの機能を停止しました。"));
                return;
            }else if(args[0].equalsIgnoreCase("view")){
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                if(TellCommand.viewlist.contains(p.getUniqueId())){
                    TellCommand.viewlist.remove(p.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix + "§c表示をoffしました。"));
                    return;
                }else{
                    TellCommand.viewlist.add(p.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix + "§a表示をonしました。"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("mutelist")){
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                p.sendMessage(new TextComponent(plugin.prefix + "§4muteされているリスト"));
                for(String uuid : plugin.mutes){
                    p.sendMessage(new TextComponent(plugin.prefix + "§f"+uuid+"("+
                            ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).getName()+")"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("cmmutelist")){
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                p.sendMessage(new TextComponent(plugin.prefix + "§4cmmuteされているリスト"));
                for(String uuid : plugin.commandmutes){
                    p.sendMessage(new TextComponent(plugin.prefix + "§f"+uuid+"("+
                            ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).getName()+")"));
                    return;
                }
            }
        }else if(args.length == 2) {
            if (args[0].equalsIgnoreCase("info")) {
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    if (!plugin.link_contain_p(player.getUniqueId())) {
                        p.sendMessage(new TextComponent(plugin.prefix + "§4" + args[1] + " さんは誰ともリンクしていません"));
                        return;
                    }
                    p.sendMessage(new TextComponent(plugin.prefix + "§c" + args[1] + "さんは " + plugin.discord.getName_link(plugin.discord.jda.getUserById(plugin.links.get(player.getUniqueId()))) + " さんとリンクしています"));
                    return;
                } else {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            } else if (args[0].equalsIgnoreCase("title")) {
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                String[] tt = args[1].split("//");
                if (tt.length > 3 || tt.length == 0) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4コマンドが間違っています"));
                    p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//<sub>//<time>"));
                    return;
                }
                Title title = ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.translateAlternateColorCodes('&', tt[0])));
                if (tt.length >= 2) {
                    title.subTitle(new TextComponent(ChatColor.translateAlternateColorCodes('&', tt[1])));
                }
                int time = 100;
                if (tt.length == 3) {
                    try {
                        time = Integer.parseInt(tt[2]) * 20;
                    } catch (NumberFormatException e) {
                        p.sendMessage(new TextComponent(plugin.prefix + "§4timeは数字(秒数)で入力してください"));
                        p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//(sub)//(time)"));
                        return;
                    }
                }
                title.stay(time);
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    player.sendTitle(title);
                }
                return;
            } else if (args[0].equalsIgnoreCase("msg")) {
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                plugin.discord.sendMessage(":mega: " + args[1]);
                return;
            }else if(args[0].equalsIgnoreCase("mute")) {
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.mutedplayer(player.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix+"§amuteしました。"));
                    return;
                }else {
                    p.sendMessage(new TextComponent(plugin.prefix+"§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("unmute")) {
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.unmutedplayer(player.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix + "§aunmuteしました。"));
                    return;
                } else {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("cmmute")) {
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.commandmutedplayer(player.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix+"§acmmuteしました。"));
                    return;
                }else {
                    p.sendMessage(new TextComponent(plugin.prefix+"§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("cmunmute")) {
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.commandunmutedplayer(player.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix + "§acmunmuteしました。"));
                    return;
                } else {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("amute")) {
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.commandmutedplayer(player.getUniqueId());
                    plugin.mutedplayer(player.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix+"§aamuteしました。"));
                    return;
                }else {
                    p.sendMessage(new TextComponent(plugin.prefix+"§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("aunmute")) {
                if (!p.hasPermission("bd.op")) {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.commandunmutedplayer(player.getUniqueId());
                    plugin.unmutedplayer(player.getUniqueId());
                    p.sendMessage(new TextComponent(plugin.prefix + "§aaunmuteしました。"));
                    return;
                } else {
                    p.sendMessage(new TextComponent(plugin.prefix + "§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }
        }else if(args.length == 3) {
            if(args[0].equalsIgnoreCase("mute")) {
                int mutedtime = 30;
                try{
                    mutedtime = Integer.parseInt(args[2]);
                }catch (NumberFormatException e){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4数字ではありません"));
                    return;
                }
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    plugin.timemutedplayer(player.getUniqueId(),mutedtime);
                    p.sendMessage(new TextComponent(plugin.prefix+"§a"+mutedtime+"分muteしました。"));
                    return;
                }else {
                    p.sendMessage(new TextComponent(plugin.prefix+"§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }
        }
        p.sendMessage(new TextComponent(plugin.prefix+"§2====ヘルプメニュー===="));
        p.sendMessage(new TextComponent(plugin.prefix + "§6/bd link §f: Discordアカウントとマイクラをリンクします"));
        p.sendMessage(new TextComponent(plugin.prefix + "§6/bd info §f: リンク情報をチェックします"));
        p.sendMessage(new TextComponent(plugin.prefix + "§6/bd ignore §f: リンク申請を拒否します"));
        p.sendMessage(new TextComponent(plugin.prefix + "§6/bd unlink §f: Discordアカウントとマイクラのリンクを削除します"));
        if(p.hasPermission("bd.op")) {
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd check : 正常に接続しているかチェックします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd info [Player名] : 他のプレイヤーのリンク状況を確認します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd msg [内容] : Discordにメッセージを送信します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd view : 個人チャットの表示を切り替えます"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd mutelist : muteされているリストを表示"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd cmmutelist : cmmuteされているリストを表示"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd on : 機能を起動します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd off : 機能を停止します(緊急時のみ使うこと)"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd mute [player名] : 特定のプレイヤーをmuteします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd unmute [player名] : muteを解除します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd cmmute [player名] : 特定のプレイヤーをコマンドmuteします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd cmunmute [player名] : コマンドmuteを解除します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd amute [player名] : 特定のプレイヤーを2種類ともmuteします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd aunmute [player名] : 2種類のmuteを解除します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//<sub>//<time> : (おまけ機能)全プレイヤーにタイトルを表示"));
            if(!plugin.power){
                p.sendMessage(new TextComponent(plugin.prefix+"§4§l機能停止中"));
            }
        }
        p.sendMessage(new TextComponent(plugin.prefix+"§2====================="));
        p.sendMessage(new TextComponent(plugin.prefix + "§c§lCreated by Mr_IK || v1.6.0"));
    }
}
