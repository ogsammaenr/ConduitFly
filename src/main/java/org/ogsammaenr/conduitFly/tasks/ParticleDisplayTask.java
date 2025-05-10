package org.ogsammaenr.conduitFly.tasks;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
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


    private String particleName;

    public ParticleDisplayTask(ConduitFly plugin, String particleType) {
        this.plugin = plugin;
        this.particleName = particleType;
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
        Particle particle = Particle.valueOf(particleName.toUpperCase());

        if (particle == Particle.DUST) {
            ConfigurationSection colorSection = plugin.getConfig().getConfigurationSection("particles.color");
            Particle.DustOptions dustOptions;
            if (colorSection != null) {

                int red = colorSection.getInt("red");
                int green = colorSection.getInt("green");
                int blue = colorSection.getInt("blue");
                float size = (float) plugin.getConfig().getDouble("particles.size", 1.0);

                dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);

            } else {
                plugin.getLogger().warning("Color section not found in config.yml. Using default DUST color.");
                dustOptions = new Particle.DustOptions(Color.RED, 1.0F);
            }
            player.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dustOptions, true);

        } else {
            player.spawnParticle(particle, location, 1, 0, 0, 0, 0, null, true);
        }
    }


    record Block2D(double x, double z) {
    }

    private void showMergedArea(Player player, List<Location> conduitLocations, double range, double playery) {
        Set<Block2D> covered = new HashSet<>();

        // 1. Tüm alanları topla
        for (Location center : conduitLocations) {
            if (player.getLocation().getWorld() != center.getWorld()) continue;

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
        double y = playery + 1.2;
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
