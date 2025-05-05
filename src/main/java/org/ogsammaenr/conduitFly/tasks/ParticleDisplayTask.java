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

public class ParticleDisplayTask {
    private final ConduitFly plugin;

    public ParticleDisplayTask(ConduitFly plugin) {
        this.plugin = plugin;
    }

    public void showArea(Player player) {
        Optional<Island> islandIdOpt = IslandUtils.getIsland(player);
        if (islandIdOpt.isEmpty()) return;
        Island island = islandIdOpt.get();

        String islandId = island.getUniqueId();
        List<Location> conduitLocations = plugin.getConduitCache().getConduit(islandId);

        if (conduitLocations == null || conduitLocations.isEmpty()) {
            String message = plugin.getMessageManager().getMessage("no-conduit-found");
            player.sendMessage(message);
            return;
        }

        double playery = player.getLocation().getBlockY();


        double range = plugin.getRankSettingsManager().getRankSettingsByPermission(plugin.getRankSettingsManager().getPermission(player)).getRadius();

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 200;

            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    cancel();
                    return;
                }

                showMergedArea(player, conduitLocations, range, playery);

                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }


    private void spawnParticle(Player player, Location location) {
        player.spawnParticle(Particle.END_ROD, location, 1, 0, 0, 0, 0);
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
