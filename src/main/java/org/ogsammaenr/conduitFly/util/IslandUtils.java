package org.ogsammaenr.conduitFly.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class IslandUtils {

    /**************************************************************************************************************/
    //  oyuncunun adasını döndürür
    public static Optional<Island> getIsland(Player player) {
        return BentoBox.getInstance()
                .getIslandsManager()
                .getIslandAt(player.getLocation())
                ;
    }

    /**************************************************************************************************************/
    //  oyuncu bir adada mı ?
    public static boolean isPlayerOnIsland(Player player) {
        return getIsland(player).isPresent();
    }

    /**************************************************************************************************************/
    //  oyuncu adalar dünyasında mı ?
    public static boolean isInBSkyBlockWorld(Player player) {
        String worldName = player.getWorld().getName().toLowerCase();
        return worldName.equals("bskyblock_world")
                || worldName.equals("bskyblock_world_nether")
                || worldName.equals("bskyblock_world_the_end");
    }

    /**************************************************************************************************************/

    public static Optional<String> getIslandId(Location location) {
        Optional<Island> island = BentoBox.getInstance()
                .getIslands()
                .getIslandAt(location);
        return island.map(Island::getUniqueId);
    }

    /**************************************************************************************************************/


}

