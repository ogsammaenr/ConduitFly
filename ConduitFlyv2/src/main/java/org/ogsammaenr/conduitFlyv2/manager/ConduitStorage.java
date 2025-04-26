package org.ogsammaenr.conduitFlyv2.manager;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles loading and saving conduit Locations between the in-memory cache and the conduits.yml file.
 */
public class ConduitStorage {
    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;
    private final ConduitCache conduitCache;

    /**************************************************************************************************************/
    //  constructor metodu
    public ConduitStorage(JavaPlugin plugin, ConduitCache conduitCache) {
        this.plugin = plugin;
        this.conduitCache = conduitCache;

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.file = new File(dataFolder, "conduits.yml");
        /*  conduits.yml dosyası yok ise oluştur  */
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create conduits.yml");
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**************************************************************************************************************/
    //  bellekteki veriler conduits.yml dosyasına kaydedilir
    public void saveToYML() {
        config.set("conduits", null);
        for (Map.Entry<String, List<Location>> entry : conduitCache.getAllConduits().entrySet()) {
            String islandID = entry.getKey();
            List<Location> locs = entry.getValue();
            for (int i = 0; i < locs.size(); i++) {
                Location loc = locs.get(i);
                if (loc.getWorld() == null) continue;
                String base = "conduits." + islandID + "." + i;
                config.set(base + ".world", loc.getWorld().getName());
                config.set(base + ".x", loc.getX());
                config.set(base + ".y", loc.getY());
                config.set(base + ".z", loc.getZ());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save conduits.yml");
            e.printStackTrace();
        }
    }

    /**************************************************************************************************************/
    //  conduits.yml dosyasından veriler belleğe yüklenir
    public void loadFromYML() {
        if (!file.exists()) return;
        FileConfiguration cfg = this.config;
        if (cfg.getConfigurationSection("conduits") == null) return;

        for (String islandID : cfg.getConfigurationSection("conduits").getKeys(false)) {
            String sectionPath = "conduits." + islandID;
            // clear any existing list so we don't duplicate
            conduitCache.getConduit(islandID).clear();
            for (String key : cfg.getConfigurationSection(sectionPath).getKeys(false)) {
                String base = sectionPath + "." + key;
                String worldName = cfg.getString(base + ".world");
                double x = cfg.getDouble(base + ".x");
                double y = cfg.getDouble(base + ".y");
                double z = cfg.getDouble(base + ".z");
                if (worldName == null) continue;
                if (plugin.getServer().getWorld(worldName) == null) {
                    plugin.getLogger().warning("World '" + worldName + "' not found for conduit entry.");
                    continue;
                }
                Location loc = new Location(plugin.getServer().getWorld(worldName), x, y, z);
                conduitCache.addConduit(islandID, loc);
            }
        }
    }

    /**************************************************************************************************************/
    //  sunucu kapanırken veriler conduits.yml dosyasına yüklenir   (pek bi olayı yok sadece daha anlaşılır olması için)
    public void saveOnShutdown() {
        saveToYML();
    }

    /**************************************************************************************************************/
    //  belirli bir adanın conduit verileri silinir
    public void removeIslandData(String islandID) {
        File file = new File(plugin.getDataFolder(), "conduits.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("conduits." + islandID)) {
            config.set("conduits" + islandID, null);
            try {
                config.save(file);
                plugin.getLogger().info("Removed conduits." + islandID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().warning("Could not remove conduits." + islandID);
        }
    }
}

