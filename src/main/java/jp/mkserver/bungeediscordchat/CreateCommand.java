package jp.mkserver.bungeediscordchat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CreateCommand extends Command {
    BungeeDiscordChat plugin;
    public CreateCommand(BungeeDiscordChat This) {
        super("bd");
        plugin = This;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if(args.length == 0){
            p.sendMessage(new TextComponent(plugin.prefix+"§2====ヘルプメニュー===="));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd link §f: Discordアカウントとマイクラをリンクします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd info §f: リンク情報をチェックします"));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd ignore §f: リンク申請を拒否します"));
            p.sendMessage(new TextComponent(plugin.prefix + "§6/bd unlink §f: Discordアカウントとマイクラのリンクを削除します"));
            if(p.hasPermission("bd.op")) {
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd check : 正常に接続しているかチェックします"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd info [Player名] : 他のプレイヤーのリンク状況を確認します"));
                p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//<sub>//<time> : (おまけ機能)全プレイヤーにタイトルを表示"));
            }
            p.sendMessage(new TextComponent(plugin.prefix+"§2==================="));
            p.sendMessage(new TextComponent(plugin.prefix + "§c§lCreated by Mr_IK"));
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
            }
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("info")){
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if (player != null) {
                    if(!plugin.link_contain_p(player.getUniqueId())){
                        p.sendMessage(new TextComponent(plugin.prefix+"§4"+args[1]+" さんは誰ともリンクしていません"));
                        return;
                    }
                    p.sendMessage(new TextComponent(plugin.prefix+"§c"+args[1]+"さんは "+plugin.discord.getName_link(plugin.discord.jda.getUserById(plugin.links.get(player.getUniqueId())))+" さんとリンクしています"));
                    return;
                }else {
                    p.sendMessage(new TextComponent(plugin.prefix+"§4そのプレイヤーは現在オフラインです"));
                    return;
                }
            }else if(args[0].equalsIgnoreCase("title")){
                if(!p.hasPermission("bd.op")){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4あなたはこのコマンドを実行できません"));
                    return;
                }
                String[] tt = args[1].split("//");
                if(tt.length > 3||tt.length == 0){
                    p.sendMessage(new TextComponent(plugin.prefix+"§4コマンドが間違っています"));
                    p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//<sub>//<time>"));
                    return;
                }
                Title title = ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.translateAlternateColorCodes('&',tt[0])));
                if(tt.length >= 2){
                    title.subTitle(new TextComponent(ChatColor.translateAlternateColorCodes('&',tt[1])));
                }
                int time = 100;
                if(tt.length == 3){
                    try{
                        time = Integer.parseInt(tt[2]) * 20;
                    }catch (NumberFormatException e){
                        p.sendMessage(new TextComponent(plugin.prefix+"§4timeは数字(秒数)で入力してください"));
                        p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//(sub)//(time)"));
                        return;
                    }
                }
                title.stay(time);
                for(ProxiedPlayer player:ProxyServer.getInstance().getPlayers()){
                    player.sendTitle(title);
                }
                return;
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
            p.sendMessage(new TextComponent(plugin.prefix + "§c/bd title <main>//<sub>//<time> : (おまけ機能)全プレイヤーにタイトルを表示"));
        }
        p.sendMessage(new TextComponent(plugin.prefix+"§2====================="));
        p.sendMessage(new TextComponent(plugin.prefix + "§c§lCreated by Mr_IK"));
    }
}
