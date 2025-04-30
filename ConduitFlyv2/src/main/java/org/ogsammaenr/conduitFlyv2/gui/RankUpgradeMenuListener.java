package org.ogsammaenr.conduitFlyv2.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.settings.RankSettings;
import org.ogsammaenr.conduitFlyv2.settings.RankSettingsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankUpgradeMenuListener implements Listener {
    ConduitFlyv2 plugin;
    RankSettingsManager rankSettingsManager;

    public RankUpgradeMenuListener(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        rankSettingsManager = plugin.getRankSettingsManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§bRütbe Yükseltme")) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        int currentPage = getCurrentPage(title);
        int totalPages = (int) Math.ceil((double) rankSettingsManager.getRankSettingsMap().size() / 4);

        plugin.getLogger().info("Clicked type: " + clicked.getType());
        plugin.getLogger().info("DisplayName: " + meta.getDisplayName());
        plugin.getLogger().info("Raw title: " + title);

        // Sayfa kontrolü
        if (itemName.equalsIgnoreCase("← Önceki Sayfa")) {
            plugin.getLogger().info("önceki sayfa tıklandı : " + currentPage + "/" + totalPages);
            if (currentPage > 1) {
                new PaginatedRankUpgradeMenu(rankSettingsManager).open(player, currentPage - 1);
            }
            return;
        } else if (itemName.equalsIgnoreCase("Sonraki Sayfa →")) {
            plugin.getLogger().info("sonraki sayfa tıklandı : " + currentPage + "/" + totalPages);
            if (currentPage < totalPages) {
                new PaginatedRankUpgradeMenu(rankSettingsManager).open(player, currentPage + 1);
            }
            return;
        }

        // Rütbe kontrolü
        if (clicked.getType() == Material.LIGHT_GRAY_DYE) {
            List<RankSettings> sortedRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
            sortedRanks.sort(Comparator.comparingInt(RankSettings::getPriority));

            for (RankSettings rank : sortedRanks) {
                String display = ChatColor.stripColor("§a" + rank.getPermission());

                if (itemName.equals(display)) {
                    String perm = rank.getPermission();

                    if (player.hasPermission(perm)) {
                        player.sendMessage("§eZaten bu rütbeye sahipsin.");
                        return;
                    }

                    double price = rank.getPrice();
                    double balance = plugin.getEconomy().getBalance(player);

                    if (balance > price) {
                        plugin.getEconomy().withdrawPlayer(player, price);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + perm);
                        player.sendMessage("§aYeni rütbe kazandın: §f" + perm + "§7(-$" + price + ")");

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            new PaginatedRankUpgradeMenu(rankSettingsManager).open(player, currentPage);
                        }, 5L);
                    } else {
                        player.sendMessage("§cYetersiz bakiye. Gerekli: §f$" + price);
                    }
                    return;
                }
            }
        }
    }

    private int getCurrentPage(String title) {
        String[] titleParts = title.split(" ");
        return Integer.parseInt(titleParts[titleParts.length - 1]);
    }
}
