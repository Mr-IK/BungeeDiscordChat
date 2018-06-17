package jp.mkserver.bungeediscordchat;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class FileManager {

    private static Plugin plugin;

    public static HashMap<UUID,Long> loadEnable(Plugin plugins) {
        plugin = plugins;
        File file = new File(plugin.getDataFolder(), File.separator +"links");
        HashMap<UUID,Long> links = new HashMap<>();
        if(!file.exists()){
            file.mkdir();
        }
        if(file.listFiles().length!=0) {
            for (File files : file.listFiles()) {
                String name = files.getName().replace(".yml","");
                links.put(UUID.fromString(name),getConfig(name).getLong(name));
            }
        }
        return links;
    }

    public static void removeConfig(String filename){
        File file = new File(plugin.getDataFolder(), File.separator +"links"+File.separator +filename+".yml");
        if (file.exists()) {
            file.delete();
        }
    }


    public static Configuration getConfig(String filename){
        File file = new File(plugin.getDataFolder(), File.separator +"links"+File.separator +filename+".yml");
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

    public static void createConfig(String filename,long id){
        File file = new File(plugin.getDataFolder(), File.separator +"links"+File.separator +filename+".yml");

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
            cof.set(filename,id);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cof, file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }


}
