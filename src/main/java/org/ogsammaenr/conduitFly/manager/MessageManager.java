package org.ogsammaenr.conduitFly.manager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ogsammaenr.conduitFly.ConduitFly;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MessageManager {
    private FileConfiguration messages;
    private final ConduitFly plugin;

    public MessageManager(ConduitFly plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false); // klasörde yoksa kopyala
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.plugin = plugin;
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&',
                messages.getString("prefix", "") + messages.getString(key, "§c[ERROR: " + key + " Undefined]"));
    }

    public String getRaw(String key) {
        return ChatColor.translateAlternateColorCodes('&',
                messages.getString(key, "§c[ERROR: " + key + " Undefined]"));
    }

    public List<String> getList(String path) {
        List<String> list = messages.getStringList(path);
        return list.stream()
                .map(this::color) // Renk kodlarını uygula
                .collect(Collectors.toList());
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void reloadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false); // dosya hiç yoksa oluşturur
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
