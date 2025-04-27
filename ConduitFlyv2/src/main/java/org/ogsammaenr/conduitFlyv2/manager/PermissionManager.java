package org.ogsammaenr.conduitFlyv2.manager;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;

public class PermissionManager {
    private final ConduitFlyv2 plugin;
    private final PluginManager pm;

    public PermissionManager(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
    }

    public void loadPermissions() {
        // Eğer config'te ranks kısmı yoksa işlem yapma
        if (!plugin.getConfig().isConfigurationSection("ranks")) {
            return;
        }

        plugin.getLogger().info("Loading permissions");

        // Ranks kısmını al
        for (String rankKey : plugin.getConfig().getConfigurationSection("ranks").getKeys(false)) {
            String base = "ranks." + rankKey + ".";

            // Rank ayarlarını al
            String permission = plugin.getConfig().getString(base + "permission");
            String description = "Permission for " + rankKey;
            String defStr = plugin.getConfig().getString(base + "permissionDefault", "false");
            PermissionDefault defVal = PermissionDefault.getByName(defStr.toUpperCase());

            // Permission nesnesini oluştur
            Permission p = new Permission(permission, description, defVal);
            pm.addPermission(p);

            plugin.getLogger().info("Loaded permission for rank: " + rankKey + " with permission: " + permission);
        }

        // Conduit material bilgisini al
        String conduitMaterial = plugin.getConfig().getString("conduit.material", "CONDUIT");
        plugin.getLogger().info("Conduit material is set to: " + conduitMaterial);
    }
}