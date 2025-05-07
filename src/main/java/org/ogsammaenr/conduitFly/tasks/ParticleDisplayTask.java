package org.ogsammaenr.conduitFly.tasks;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.util.IslandUtils;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ParticleDisplayTask extends BukkitRunnable {
    private final ConduitFly plugin;

    public ParticleDisplayTask(ConduitFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getAreaToggles().containsKey(player.getUniqueId())) continue;

            Optional<Island> islandOpt = IslandUtils.getIsland(player);
            if (islandOpt.isEmpty()) continue;

            Island island = islandOpt.get();
            List<Location> conduitLocations = plugin.getConduitCache().getConduit(island.getUniqueId());

            if (conduitLocations.isEmpty() || conduitLocations == null) continue;

            double range = plugin.getRankSettingsManager().
                    getRankSettingsByPermission(plugin.getRankSettingsManager().getPermission(player)).
                    getRadius();

            double playery = plugin.getAreaToggles().get(player.getUniqueId());

            showMergedArea(player, conduitLocations, range, playery);
        }
    }


    private void spawnParticle(Player player, Location location) {
        player.spawnParticle(Particle.END_ROD, location, 1, 0, 0, 0, 0, null, true);
    }


    record Block2D(double x, double z) {
    }

    private void showMergedArea(Player player, List<Location> conduitLocations, double range, double playery) {
        Set<Block2D> covered = new HashSet<>();

        // 1. Tüm alanları topla
        for (Location center : conduitLocations) {
            double minX = center.getBlockX() - range + 0.5;
            double maxX = center.getBlockX() + range - 0.5;
            double minZ = center.getBlockZ() - range + 0.5;
            double maxZ = center.getBlockZ() + range - 0.5;

            for (double x = minX; x <= maxX; x++) {
                for (double z = minZ; z <= maxZ; z++) {
                    covered.add(new Block2D(x, z));
                }
            }
        }

        // 2. Her blok için kenarları kontrol et, sadece dış çizgileri çiz
        double y = playery + 1.3;
        for (Block2D block : covered) {
            double x = block.x();
            double z = block.z();

            // Kenar kontrolü: Sağ, Sol, Üst, Alt
            if (!covered.contains(new Block2D(x + 1, z))) {
                spawnParticle(player, new Location(player.getWorld(), x + 1.0, y, z + 0.5));
            }
            if (!covered.contains(new Block2D(x - 1, z))) {
                spawnParticle(player, new Location(player.getWorld(), x, y, z + 0.5));
            }
            if (!covered.contains(new Block2D(x, z + 1))) {
                spawnParticle(player, new Location(player.getWorld(), x + 0.5, y, z + 1.0));
            }
            if (!covered.contains(new Block2D(x, z - 1))) {
                spawnParticle(player, new Location(player.getWorld(), x + 0.5, y, z));
            }
        }
    }
}
