package jp.mkserver.bungeediscordchat.mat;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.config.Configuration;


public class MAT_BanSystem{

    Plugin plugin;
    public HashMap<UUID,String> defaultBan = new HashMap<>();
    public HashMap<String,String> ipBan = new HashMap<>();
    FileSystem file;
    Configuration config;

    public void saveFile(){
        config.set("default",defaultBantoList());
        config.set("ip",ipBantoList());
        file.saveConfig();
    }

    public List<String> defaultBantoList(){
        List<String> list = new ArrayList<>();
        for(UUID uuid: defaultBan.keySet()){
            list.add(uuid.toString()+":"+defaultBan.get(uuid));
        }
        return list;
    }

    public List<String> ipBantoList(){
        List<String> list = new ArrayList<>();
        for(String uuid: ipBan.keySet()){
            list.add(uuid+":"+ipBan.get(uuid));
        }
        return list;
    }


    public MAT_BanSystem(Plugin plugin){
        file = new FileSystem(plugin);
        this.plugin = plugin;
        config =  file.getConfig();
        if(config.contains("default")){
            List<String> string = config.getStringList("default");
            for(String s : string){
                String[] ss = s.split(":");
                defaultBan.put(UUID.fromString(ss[0]),ss[1]);
            }
        }

        if(config.contains("ip")){
            List<String> string = config.getStringList("ip");
            for(String s : string){
                String[] ss = s.split(":");
                ipBan.put(ss[0],ss[1]);
            }
        }
    }

    public boolean DiscordLogSafetyMode = false;

    public void setDiscordLogSafetyMode(boolean bool){
        DiscordLogSafetyMode = bool;
    }


    public void defaultBan(UUID u,String reason){
        if(!defaultBan.containsKey(u)){
            defaultBan.put(u,reason);
            plugin.getLogger().info("[M.A.T] Default Banned　["+u.toString()+"]");
        }
    }

    public void ipBan(String ip,String reason){
        if(!ipBan.containsKey(ip)){
            ipBan.put(ip,reason);
            plugin.getLogger().info("[M.A.T] IP Banned　["+ip+"]");
        }
    }
}
