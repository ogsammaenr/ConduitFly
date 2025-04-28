package org.ogsammaenr.conduitFlyv2.manager;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;

public class PermissionManager {
    private final ConduitFlyv2 plugin;
    private final PluginManager pm;

    /**************************************************************************************************************/
    //  Constructor methodu
    public PermissionManager(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
    }

    /**************************************************************************************************************/
    //  Configdeki permler oluşturulur
    public void loadPermissions() {
        /* Eğer config'te ranks kısmı yoksa işlem yapma */
        if (!plugin.getConfig().isConfigurationSection("ranks")) {
            return;
        }

        plugin.getLogger().info("Loading permissions");

        /*      Ranks kısmınındaki rankları döndürür        */
        for (String rankKey : plugin.getConfig().getConfigurationSection("ranks").getKeys(false)) {
            String base = "ranks." + rankKey + ".";

            /*      rankların verisini alır     */
            String permission = plugin.getConfig().getString(base + "permission");
            String description = "Permission for " + rankKey;
            String defStr = plugin.getConfig().getString(base + "permissionDefault", "false");
            PermissionDefault defVal = PermissionDefault.getByName(defStr.toUpperCase());

            /*      veriler ile bir permission nesnesi oluşturulur*/
            Permission p = new Permission(permission, description, defVal);
            pm.addPermission(p);

            plugin.getLogger().info("Loaded permission for rank: " + rankKey + " with permission: " + permission);
        }

        /*      bypassconduit permini oluşturur (configden alınmaz)      */
        Permission p = new Permission("conduitfly.bypassconduit", "bypass conduit", PermissionDefault.FALSE);
        pm.addPermission(p);
        plugin.getLogger().info("Loaded permissions for conduit: bypassconduit with permission: conduitfly.bypassconduit");

        /*      Conduit materyalinin verisini al        */
        String conduitMaterial = plugin.getConfig().getString("conduit.material", "CONDUIT");
        plugin.getLogger().info("Conduit material is set to: " + conduitMaterial);
    }
}