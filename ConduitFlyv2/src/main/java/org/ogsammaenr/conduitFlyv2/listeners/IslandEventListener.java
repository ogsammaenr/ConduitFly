package org.ogsammaenr.conduitFlyv2.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.ogsammaenr.conduitFlyv2.manager.ConduitCache;
import org.ogsammaenr.conduitFlyv2.manager.ConduitStorage;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;

public class IslandEventListener implements Listener {

    private final ConduitCache cache;
    private final ConduitStorage storage;

    /**************************************************************************************************************/
    //  constructor metodu
    public IslandEventListener(ConduitCache cache, ConduitStorage storage) {
        this.cache = cache;
        this.storage = storage;
    }

    /**************************************************************************************************************/
    //  oyuncunun adası silindiğinde çalışır
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandDelete(IslandDeleteEvent event) {
        String islandID = event.getIsland().getUniqueId();

        /*  veriler bellekten kaldırılır  */
        cache.removeIslandConduits(islandID);

        /*  veriler conduits.yml dosyasından kaldırılır  */
        storage.removeIslandData(islandID);
    }

    /**************************************************************************************************************/
    //  oyuncu  adasını resetlediğinde çalışır
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandReset(IslandResetEvent event) {
        String islandID = event.getOldIsland().getUniqueId();

        /*  veriler bellekten kaldırılır  */
        cache.removeIslandConduits(islandID);

        /*  veriler conduits.yml dosyasından kaldırılır  */
        storage.removeIslandData(islandID);
    }
}
