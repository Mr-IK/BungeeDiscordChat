package jp.mkserver.bungeediscordchat.mat;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

public class WhiteListFileSystem {

    private Plugin plugin;
    private File file;
    private Configuration config;

    public WhiteListFileSystem(Plugin plugin) {

        this.plugin = plugin;

        this.file = new File(getDataFolder(), "whitelist.yml");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (InputStream is = getResourceAsStream("whitelist.yml");
                     OutputStream os = new FileOutputStream(file)) {
                     ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create storage file", e);
            }
        }
    }

    public Configuration getConfig(){
        try {
            return (this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveConfig(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "whitelist.yml"));
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Couldn't save storage file!");
        }
    }

    private File getDataFolder(){
        return plugin.getDataFolder();
    }

    private InputStream getResourceAsStream(String file){
        return plugin.getResourceAsStream(file);
    }

}