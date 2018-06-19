package jp.mkserver.bungeediscordchat.transrate;

import jp.mkserver.bungeediscordchat.BungeeDiscordChat;

import java.util.ArrayList;

public class Translate{
    static boolean onoff = false;
    static BungeeDiscordChat bdc;
    static String apikey = "";
    static ArrayList<String> translist;
    public static void TransEnable(BungeeDiscordChat bdc,String apikey){
        Translate.bdc = bdc;
        Translate.onoff = true;
        Translate.apikey = apikey;
        translist = new ArrayList<>();
        translist.add("ja");
        translist.add("en");
        translist.add("ko");
        translist.add("zh");
        translist.add("es");
        translist.add("fr");
        translist.add("de");
        translist.add("it");
    }
    public static String Translates(String word,String from,String to){
        if(!translist.contains(from)||!translist.contains(to)){
            return null;
        }
        if(from.equalsIgnoreCase(to)){
            return word;
        }
        TranslateAPI trans = new TranslateAPI(bdc,word,apikey);
        return trans.Translate(from,to);
    }

}
