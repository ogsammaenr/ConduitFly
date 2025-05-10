package org.ogsammaenr.conduitFly.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.storage.ConduitCache;
import org.ogsammaenr.conduitFly.storage.ConduitDataStorage;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;

public class IslandEventListener implements Listener {

    private final ConduitCache cache;
    private final ConduitDataStorage storage;

    /**************************************************************************************************************/
    //  constructor metodu
    public IslandEventListener(ConduitFly plugin) {
        this.cache = plugin.getConduitCache();
        storage = plugin.getConduitDataStorage();
    }

    /**************************************************************************************************************/
    //  oyuncunun adası silindiğinde çalışır
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandDelete(IslandDeleteEvent event) {
        String islandID = event.getIsland().getUniqueId();

        /*  veriler bellekten kaldırılır  */
        cache.removeIslandConduits(islandID);

        /*  veriler veritabanından kaldırılır  */
        storage.removeIslandData(islandID);
    }

    /**************************************************************************************************************/
    //  oyuncu  adasını resetlediğinde çalışır
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandReset(IslandResetEvent event) {
        String islandID = event.getOldIsland().getUniqueId();

        /*  veriler bellekten kaldırılır  */
        cache.removeIslandConduits(islandID);

        /*  veriler veritabanından kaldırılır  */
        storage.removeIslandData(islandID);
    }
}
