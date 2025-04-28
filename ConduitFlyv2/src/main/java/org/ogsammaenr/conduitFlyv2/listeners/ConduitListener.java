package org.ogsammaenr.conduitFlyv2.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.manager.ConduitCache;
import org.ogsammaenr.conduitFlyv2.manager.ConduitStorage;
import org.ogsammaenr.conduitFlyv2.util.IslandUtils;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class ConduitListener implements Listener {
    private final ConduitStorage conduitStorage;
    private final ConduitCache conduitCache;
    private final JavaPlugin plugin;

    private Material conduitMaterial;

    /**************************************************************************************************************/
    //  Constructor metodu
    public ConduitListener(ConduitFlyv2 plugin) {
        this.conduitStorage = plugin.getConduitStorage();
        this.conduitCache = plugin.getConduitCache();
        this.plugin = plugin;
        this.conduitMaterial = Material.valueOf(plugin.getConfig().getString("conduit.material", "CONDUIT"));

        /*  Cache 'i periyodik olarak yml dosyasına kaydet  */
        new BukkitRunnable() {
            @Override
            public void run() {
                conduitStorage.saveToYML();
            }
        }.runTaskTimerAsynchronously(plugin, 200, 200);
    }

    /**************************************************************************************************************/
    //  Conduit yerleştirildiğinde çalışır
    @EventHandler
    public void onConduitPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == conduitMaterial) { /*  yerleştirilen blok conduit mi ? */
            Player player = event.getPlayer();

            /*  oyuncu bir ada üzerinde mi?  */
            Optional<Island> optionalIsland = IslandUtils.getIsland(player);
            if (optionalIsland.isEmpty()) {
                player.sendMessage("§cBir ada üzerinde değilsin!");
                return;
            }
            Island island = optionalIsland.get();
            String uuid = island.getUniqueId();

            Location location = event.getBlock().getLocation().add(0.5, 0.0, 0.5);

            conduitCache.addConduit(uuid, location);
            player.sendMessage("§aConduit yerleştirildi.");
        }
    }

    /**************************************************************************************************************/
    //  Conduit kırıldığında çalışır
    @EventHandler
    public void onConduitBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == conduitMaterial) { /*  kırılan blok conduit mi?  */
            Player player = event.getPlayer();
            /*  oyuncu bir ada üzerinde mi?  */
            Optional<Island> optionalIsland = IslandUtils.getIsland(player);
            if (optionalIsland.isEmpty()) {
                player.sendMessage("§cAda bulunamadı!");
                return;
            }
            Island island = optionalIsland.get();
            String uuid = island.getUniqueId();

            Location location = event.getBlock().getLocation().add(0.5, 0.0, 0.5);

            conduitCache.removeConduit(uuid, location);

            player.sendMessage("§cConduit kırıldı.");
        }
    }

    /**************************************************************************************************************/
    //  conduit materyali güncellenir
    public void updateConduitMaterial(Material material) {
        this.conduitMaterial = material;
    }

}