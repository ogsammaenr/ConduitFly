package org.ogsammaenr.conduitFlyv2.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.util.IslandUtils;

import java.util.*;

public class ConduitCache {
    private final Map<String, List<Location>> islandConduits;

    private final ConduitFlyv2 plugin;

    /**************************************************************************************************************/
    //  constructor metodu
    public ConduitCache(ConduitFlyv2 plugin) {
        this.islandConduits = new HashMap<>();
        this.plugin = plugin;
    }

    /**************************************************************************************************************/
    //  belleğe conduit verilerini ekler
    public void addConduit(String islandUUID, Location location) {
        islandConduits
                .computeIfAbsent(islandUUID, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(location);
    }

    /**************************************************************************************************************/
    //  bellekten conduit verilerini kaldırır (eğer varsa)
    public void removeConduit(String islandUUID, Location location) {
        List<Location> conduits = islandConduits.get(islandUUID);
        if (conduits != null) {
            conduits.removeIf(loc -> loc.equals(location));
            if (conduits.isEmpty()) {
                islandConduits.remove(islandUUID);
            }
        }

    }

    /**************************************************************************************************************/
    //  belirli bir adadaki bütün conduitleri döndürür
    public List<Location> getConduit(String islandUUID) {
        return islandConduits.getOrDefault(islandUUID, Collections.emptyList());
    }

    /**************************************************************************************************************/
    //  bütün conduitleri döndürür
    public Map<String, List<Location>> getAllConduits() {
        return islandConduits;
    }

    /**************************************************************************************************************/
    //  belirli bir adadaki bütün conduit verilerini kaldırır
    public void removeIslandConduits(String islandUUID) {
        islandConduits.remove(islandUUID);
    }

    /**************************************************************************************************************/
    //  oyuncunun bulunduğu adada conduit var mı ?
    public boolean hasConduitInIsland(Player player) {
        Optional<String> islandId = IslandUtils.getIslandId(player.getLocation());
        return islandId.filter(id -> {
            List<Location> list = islandConduits.get(id);
            return list != null && !list.isEmpty();
        }).isPresent();
    }

    /**************************************************************************************************************/
    //  oyuncu herhangi bir conduite yeteri kadar yakın mı ?
    public boolean isPlayerNearAnyConduit(Player player, double maxDistance) {
        Optional<String> islandIdOpt = IslandUtils.getIslandId(player.getLocation());
        if (islandIdOpt.isEmpty()) return false;
        String islandId = islandIdOpt.get();
        Location playerLoc = player.getLocation();
        for (Location conduitLoc : getConduit(islandId)) {
            double minX = conduitLoc.getX() - maxDistance;
            double maxX = conduitLoc.getX() + maxDistance;
            double minZ = conduitLoc.getZ() - maxDistance;
            double maxZ = conduitLoc.getZ() + maxDistance;

            // Oyuncunun X ve Z koordinatları belirtilen alan içinde mi?
            if (playerLoc.getX() >= minX && playerLoc.getX() <= maxX &&
                    playerLoc.getZ() >= minZ && playerLoc.getZ() <= maxZ) {
                return true;
            }
        }
        return false;
    }
}
