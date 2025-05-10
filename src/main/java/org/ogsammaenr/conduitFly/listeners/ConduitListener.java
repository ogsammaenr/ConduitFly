package org.ogsammaenr.conduitFly.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.storage.ConduitCache;
import org.ogsammaenr.conduitFly.storage.ConduitStorage;
import org.ogsammaenr.conduitFly.util.IslandUtils;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class ConduitListener implements Listener {
    private final ConduitStorage conduitStorage;
    private final ConduitCache conduitCache;
    private final ConduitFly plugin;

    private Material conduitMaterial;

    /**************************************************************************************************************/
    //  Constructor metodu
    public ConduitListener(ConduitFly plugin) {
        this.conduitStorage = plugin.getConduitStorage();
        this.conduitCache = plugin.getConduitCache();
        this.plugin = plugin;
        this.conduitMaterial = Material.valueOf(plugin.getConfig().getString("conduit.material", "CONDUIT"));

        /*  Cache 'i periyodik olarak yml dosyasına kaydet  */
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getConduitDataStorage().saveAll(plugin.getConduitCache().getAllConduits());
            }
        }.runTaskTimerAsynchronously(plugin, 200, 12000);
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
                return;
            }
            Island island = optionalIsland.get();
            String uuid = island.getUniqueId();

            Location location = event.getBlock().getLocation().add(0.5, 0.0, 0.5);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (event.getBlock().getType() == conduitMaterial) {
                    conduitCache.addConduit(uuid, location);

                    String message = plugin.getMessageManager().getMessage("conduit.placed");
                    player.sendMessage(message);
                }
            });
        }
    }

    /**************************************************************************************************************/
    //  Conduit kırıldığında çalışır
    @EventHandler
    public void onConduitBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getType() == conduitMaterial) { /*  kırılan blok conduit mi?  */
            Player player = event.getPlayer();
            /*  oyuncu bir ada üzerinde mi?  */
            Optional<Island> optionalIsland = IslandUtils.getIsland(player);
            if (optionalIsland.isEmpty()) {
                return;
            }
            Island island = optionalIsland.get();
            String uuid = island.getUniqueId();

            Location location = event.getBlock().getLocation().add(0.5, 0.0, 0.5);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (event.getBlock().getType() != conduitMaterial) {
                    conduitCache.removeConduit(uuid, location);
                    String message = plugin.getMessageManager().getMessage("conduit.broken");
                    player.sendMessage(message);
                }
            });
        }
    }

    /**************************************************************************************************************/
    //  conduit materyali güncellenir
    public void updateConduitMaterial(Material material) {
        this.conduitMaterial = material;
    }

}