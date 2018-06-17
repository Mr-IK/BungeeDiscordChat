package jp.mkserver.bungeediscordchat;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import javax.security.auth.login.LoginException;
import java.util.UUID;

public class Discord extends ListenerAdapter {
    BungeeDiscordChat main;
    JDA jda;
    TextChannel channel;
    public Discord(BungeeDiscordChat plugin,String token)throws LoginException, InterruptedException{
        main = plugin;
        jda = new JDABuilder(AccountType.BOT).setToken(token).buildBlocking();
        jda.addEventListener(this);
    }
    public boolean getChannel(long channelid){
        try {
            channel = jda.getTextChannelById(channelid);
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().getId().equalsIgnoreCase(jda.getSelfUser().getId())){
            return;
        }
        if(event.isFromType(ChannelType.PRIVATE)){
            PrivateChannel ch = event.getPrivateChannel();
            if(event.getMessage().getContentRaw().startsWith("!bd ")){
                String playername = event.getMessage().getContentRaw().replaceFirst("!bd ","");
                if(playername.equalsIgnoreCase("info")){
                    if(main.link_contain_d(event.getAuthor().getIdLong())){
                        ch.sendMessage("あなたは "+main.getProxy().getPlayer(main.link_get_p(event.getAuthor().getIdLong())).getName()+"("+main.getProxy().getPlayer(main.link_get_p(event.getAuthor().getIdLong())).getUniqueId()+")  さんとリンクしています").queue();
                        return;
                    }else{
                        ch.sendMessage("あなたはリンクしていません").queue();
                        return;
                    }
                }
                if(main.link_contain_d(event.getAuthor().getIdLong())){
                    ch.sendMessage("すでにあなたは "+main.getProxy().getPlayer(main.link_get_p(event.getAuthor().getIdLong()))+"("+main.getProxy().getPlayer(main.link_get_p(event.getAuthor().getIdLong())).getUniqueId()+") さんとリンクしています").queue();
                    return;
                }
                if (ProxyServer.getInstance().getPlayer(playername) != null) {
                    UUID uuid = main.getProxy().getPlayer(playername).getUniqueId();
                    if(main.link_contain_p(uuid)){
                        ch.sendMessage(playername+"("+uuid.toString()+") さんはすでに "+getName_link(event.getAuthor())+" さんとリンクしてます").queue();
                        return;
                    }
                    ch.sendMessage(playername+"("+uuid.toString()+") さんにメッセージを送信しました。").queue();
                    ch.sendMessage("ゲーム内にて/bd link を実行してください。").queue();
                    main.getProxy().getPlayer(playername).sendMessage(new TextComponent(main.prefix+getName_link(event.getAuthor())+"さんからリンク申請が届きました。"));
                    main.getProxy().getPlayer(playername).sendMessage(new TextComponent(main.prefix+"/bd link もしくは /bd ignore を実行してください。"));
                    main.link.put(uuid,event.getAuthor().getIdLong());
                    return;
                }else{
                    ch.sendMessage("そのプレイヤーは現在ログインしていません").queue();
                    return;
                }
            }else{
                ch.sendMessage("リンクするには !bd [player名] です").queue();
                ch.sendMessage("リンク情報を確認するには !bd infoです").queue();
                return;
            }
        }else if(event.getChannelType()!=ChannelType.TEXT){
            return;
        }
        if(event.getTextChannel().getIdLong()!=channel.getIdLong()) {
            return;
        }
        main.sendBroadcast(main.prefix+"§f("+getName(event.getAuthor())+"§f) "+event.getMessage().getContentRaw());
    }

    public String getName(User user){
        if(channel.getGuild().getMember(user).getNickname()==null||channel.getGuild().getMember(user).getNickname().equalsIgnoreCase("")){
            return user.getName();
        }
        return channel.getGuild().getMember(user).getNickname();
    }

    public String getName_link(User user){
        if(channel.getGuild().getMember(user).getNickname()==null||channel.getGuild().getMember(user).getNickname().equalsIgnoreCase("")){
            return user.getName() + "#" +user.getDiscriminator();
        }
        return channel.getGuild().getMember(user).getNickname() + "#" +user.getDiscriminator();
    }

    public void sendMessage(String message){
        if(channel == null||message == null){
            return;
        }
        channel.sendMessage(message).queue();
    }

}
