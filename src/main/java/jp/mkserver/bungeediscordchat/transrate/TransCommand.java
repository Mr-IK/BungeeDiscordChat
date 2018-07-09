package jp.mkserver.bungeediscordchat.transrate;

import jp.mkserver.bungeediscordchat.BungeeDiscordChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TransCommand extends Command {
    private BungeeDiscordChat bdc;
    public String prefix = "§7§l[§d§lM§b§lTranslate§7§l]§r";
    public TransCommand(BungeeDiscordChat bdc) {
        super("mtranslate");
        this.bdc = bdc;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if(!p.hasPermission("bd.op")){
            p.sendMessage(new TextComponent(prefix+"§4あなたはこのコマンドを実行できません"));
            p.sendMessage(new TextComponent(prefix+"§4You can not execute this command"));
            return;
        }
        if(args.length == 0) {
            p.sendMessage(new TextComponent(prefix + "§2====ヘルプメニュー===="));
            p.sendMessage(new TextComponent(prefix + "§e/mtranslate [元言語] [翻訳先言語] §f: 翻訳モードをonします。"));
            p.sendMessage(new TextComponent(prefix + "§e/mtranslate off §f: 翻訳モードをoffします。"));
            if(p.hasPermission("bd.op")){
                p.sendMessage(new TextComponent(prefix + "§c/mtranslate [元言語] [翻訳先言語] [Player名] §f: 他人の翻訳モードを強制onします。"));
                p.sendMessage(new TextComponent(prefix + "§c/mtranslate off [Player名] §f: 他人の翻訳モードを強制offします。"));
            }
            p.sendMessage(new TextComponent(prefix + "§6言語一覧: ja:日本語 en:英語 ru:ロシア語 zh:中国語 ko:韓国語"));
            p.sendMessage(new TextComponent(prefix + "§6 es:スペイン語 fr:フランス語 it:イタリア語 de:ドイツ語"));
            p.sendMessage(new TextComponent(prefix + "§6Language_list: ja:日本語 en:English ru:Русский язык zh:中国語 ko:한국"));
            p.sendMessage(new TextComponent(prefix + "§6 es:Español fr:Français it:italiano de:Deutsch"));
            p.sendMessage(new TextComponent(prefix + "§2===================="));
            p.sendMessage(new TextComponent(prefix + "§cCreated by Mr_IK"));
            return;
        }else if(args.length == 1) {
            if(!Translate.onoff){
                p.sendMessage(new TextComponent(prefix+"§4機能停止中"));
                return;
            }
            if(args[0].equalsIgnoreCase("off")){
                if(!bdc.list.containsKey(p.getUniqueId())){
                    p.sendMessage(new TextComponent(prefix + "§4あなたは翻訳モードではありません"));
                    p.sendMessage(new TextComponent(prefix + "§4You are not in translation mode"));
                    return;
                }
                bdc.list.remove(p.getUniqueId());
                p.sendMessage(new TextComponent(prefix + "§c翻訳モードを解除しました。"));
                p.sendMessage(new TextComponent(prefix + "§cI canceled the translation mode."));
                return;
            }
        }else if(args.length == 2) {
            if(!Translate.onoff){
                p.sendMessage(new TextComponent(prefix+"§4機能停止中"));
                return;
            }
            if(args[0].equalsIgnoreCase("off")){
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                if(player == null){
                    p.sendMessage(new TextComponent(prefix + "§4そのプレイヤーは現在オフラインです。"));
                    return;
                }
                if(!bdc.list.containsKey(player.getUniqueId())){
                    p.sendMessage(new TextComponent(prefix + "§4そのプレイヤーは翻訳モードではありません"));
                    return;
                }
                bdc.list.remove(player.getUniqueId());
                p.sendMessage(new TextComponent(prefix + "§c"+args[1]+"の翻訳モードを解除しました。"));
                return;
            }
            if(!Translate.translist.contains(args[0])){
                p.sendMessage(new TextComponent(prefix+"§4翻訳元の言語が間違っています"));
                p.sendMessage(new TextComponent(prefix+"§4The source language name is incorrect"));
                return;
            }
            if(!Translate.translist.contains(args[1])){
                p.sendMessage(new TextComponent(prefix+"§4翻訳先の言語が間違っています"));
                p.sendMessage(new TextComponent(prefix+"§4The language of the translation destination is incorrect"));
                return;
            }
            bdc.list.put(p.getUniqueId(),args[0]+"/"+args[1]);
            p.sendMessage(new TextComponent(prefix+"§a"+args[0]+"->"+args[1]+" の翻訳モードを起動しました。"));
            p.sendMessage(new TextComponent(prefix+"§a"+args[0]+"->"+args[1]+" I started the translation mode of."));
            return;
        }else if(args.length == 3) {
            if(!Translate.onoff){
                p.sendMessage(new TextComponent(prefix+"§4機能停止中"));
                return;
            }
            if(!Translate.translist.contains(args[0])){
                p.sendMessage(new TextComponent(prefix+"§4翻訳元の言語が間違っています"));
                return;
            }
            if(!Translate.translist.contains(args[1])){
                p.sendMessage(new TextComponent(prefix+"§4翻訳先の言語が間違っています"));
                return;
            }
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[2]);
            if(player == null){
                p.sendMessage(new TextComponent(prefix + "§4そのプレイヤーは現在オフラインです。"));
                return;
            }
            bdc.list.put(player.getUniqueId(),args[0]+"/"+args[1]);
            p.sendMessage(new TextComponent(prefix+"§a"+args[0]+"->"+args[1]+" の翻訳モードを起動しました。"));
            return;
        }
        p.sendMessage(new TextComponent(prefix + "§2====ヘルプメニュー===="));
        p.sendMessage(new TextComponent(prefix + "§e/mtranslate [元言語] [翻訳先言語] §f: 翻訳モードをonします。"));
        p.sendMessage(new TextComponent(prefix + "§e/mtranslate off §f: 翻訳モードをoffします。"));
        if(p.hasPermission("bd.op")){
            p.sendMessage(new TextComponent(prefix + "§c/mtranslate [元言語] [翻訳先言語] [Player名] §f: 他人の翻訳モードを強制onします。"));
            p.sendMessage(new TextComponent(prefix + "§c/mtranslate off [Player名] §f: 他人の翻訳モードを強制offします。"));
        }
        p.sendMessage(new TextComponent(prefix + "§6言語一覧: ja:日本語 en:英語 ru:ロシア語 zh:中国語 ko:韓国語"));
        p.sendMessage(new TextComponent(prefix + "§6Language_list: ja:日本語 en:English ru:Русский язык zh:中国語 ko:한국"));
        p.sendMessage(new TextComponent(prefix + "§2===================="));
        p.sendMessage(new TextComponent(prefix + "§cCreated by Mr_IK"));
    }
}
