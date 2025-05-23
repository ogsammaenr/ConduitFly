package org.ogsammaenr.conduitFly.manager;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.ogsammaenr.conduitFly.ConduitFly;

public class PermissionManager {
    private final org.ogsammaenr.conduitFly.ConduitFly plugin;
    private final PluginManager pm;

    /**************************************************************************************************************/
    //  Constructor methodu
    public PermissionManager(ConduitFly plugin) {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
    }

    /**************************************************************************************************************/
    //  Configdeki permler oluşturulur
    public void loadPermissions() {
        /*      eski permleri kaldır        */
        for (Permission permission : pm.getPermissions()) {
            if (permission.getName().startsWith("conduitfly.")) {
                pm.removePermission(permission);
                plugin.getLogger().info("Removed old permission: " + permission.getName());
            }
        }

        /*      Eğer config'te ranks kısmı yoksa işlem yapma        */
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

            /*      girilen permde hata var mı kontrol edilir       */
            if (permission == null || permission.isEmpty()) {
                plugin.getLogger().warning("Permission is missing for rank: " + rankKey);
                continue;
            }

            /*      veriler ile bir permission nesnesi oluşturulur      */
            if (pm.getPermission(permission) == null) {
                Permission p = new Permission(permission, description, defVal);
                pm.addPermission(p);
                plugin.getLogger().info("Loaded permission for rank: " + rankKey + " with permission: " + permission);
            } else {
                plugin.getLogger().info("Permission already exists for rank: " + rankKey + " with permission: " + permission);
            }
        }

        /**--------------------------------------------------------------------------------------*/
        /*      bypassconduit permini oluşturur (configden alınmaz)      */
        String bypassPermission = "conduitfly.bypassconduit";
        if (pm.getPermission(bypassPermission) == null) {
            Permission pBypassConduit = new Permission(bypassPermission, "Bypass conduit", PermissionDefault.FALSE);
            pm.addPermission(pBypassConduit);
            plugin.getLogger().info("Loaded permission: " + bypassPermission);
        } else {
            plugin.getLogger().info("Permission already exists: " + bypassPermission);
        }

        /**--------------------------------------------------------------------------------------*/
        /*      reload permini oluşturur (configden alınmaz)        */

        String reloadPermission = "conduitfly.reload";
        if (pm.getPermission(reloadPermission) == null) {
            Permission pReload = new Permission(reloadPermission, "Reload plugin", PermissionDefault.OP);
            pm.addPermission(pReload);
            plugin.getLogger().info("Loaded permission: " + reloadPermission);
        } else {
            plugin.getLogger().info("Permission already exists: " + reloadPermission);
        }
        /**--------------------------------------------------------------------------------------*/
        /*      Conduit materyalinin verisini al        */

        String conduitMaterial = plugin.getConfig().getString("conduit.material", "CONDUIT");
        plugin.getLogger().info("Conduit material is set to: " + conduitMaterial);
    }
}