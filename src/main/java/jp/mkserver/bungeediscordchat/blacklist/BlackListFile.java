package jp.mkserver.bungeediscordchat.blacklist;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BlackListFile {

    private static Plugin plugin;

    public static ArrayList<UUID> loadEnable(Plugin plugins) {
        BlackListFile.plugin = plugins;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        File file = new File(plugin.getDataFolder(), File.separator +"blacklist.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create storage file", e);
            }
        }
        ArrayList<UUID> list = new ArrayList<>();
        for(String key:getConfig().getKeys()){
            UUID uuid = UUID.fromString(key);
            list.add(uuid);
        }
        return list;
    }
    public static class BlackListData{
        private String mcid = null;
        private UUID uuid = null;
        private String time = null;
        private String memo = null;
        BlackListData(String mcid,UUID uuid,String time,String memo){
            this.mcid = mcid;
            this.uuid = uuid;
            this.time = time;
            this.memo = memo;
        }
        public String getmcid(){
            return mcid;
        }
        public UUID getuuid(){
            return uuid;
        }
        public String gettime(){
            return time;
        }
        public String getmemo(){
            return memo;
        }

    }

    public static boolean removeData(UUID uuid){
        File file = new File(plugin.getDataFolder(), File.separator +"blacklist.yml");
        if (!file.exists()) {
            return false;
        }
        Configuration cof = getConfig();
        cof.set(uuid.toString(),null);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cof, file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static Configuration getConfig(){
        File file = new File(plugin.getDataFolder(), File.separator +"blacklist.yml");
        if (!file.exists()) {
            return null;
        }
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addData(String name, UUID uuid,String time,String memo){
        File file = new File(plugin.getDataFolder(), File.separator +"blacklist.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create storage file", e);
            }
        }
        try{
            Configuration cof =  ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            if(cof.contains(uuid.toString())){
                return false;
            }
            cof.set(uuid.toString()+".name",name);
            cof.set(uuid.toString()+".time",time);
            cof.set(uuid.toString()+".memo",memo);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cof, file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean upData(String name, UUID uuid,String time,String memo){
        File file = new File(plugin.getDataFolder(), File.separator +"blacklist.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create storage file", e);
            }
        }
        try{
            Configuration cof =  ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            if(!cof.contains(uuid.toString())){
                return false;
            }
            if(name != null) {
                cof.set(uuid.toString() + ".name", name);
            }
            if(time != null) {
                cof.set(uuid.toString() + ".time", time);
            }
            if(memo != null) {
                cof.set(uuid.toString() + ".memo", memo);
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cof, file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static BlackListData getData(UUID uuid){
        Configuration cof = getConfig();
        if(!cof.contains(uuid.toString())){
            return null;
        }
        return new BlackListData(cof.getString(uuid.toString()+".name"),uuid,cof.getString(uuid.toString()+".time"),cof.getString(uuid.toString()+".memo"));
    }

}
