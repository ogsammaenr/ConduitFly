package org.ogsammaenr.conduitFlyv2.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.settings.RankSettingsManager;

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

        if (clickedInventory == null) {
            return;
        }

        String title = event.getView().getTitle();
        plugin.getLogger().info(title);
        if (!title.startsWith("§bRütbe Yükseltme")) {
            return;
        }

        if (event.getCurrentItem() == null || event.getClickedInventory().getType() == null) {
            return;
        }

        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int currentPage = getCurrentPage(title);
        int totalPages = (int) Math.ceil((double) rankSettingsManager.getRankSettingsMap().size() / 4);

        plugin.getLogger().info("Tıklanan item adı (renksiz): " + itemName);

        if (itemName.equalsIgnoreCase("← Önceki Sayfa")) {
            plugin.getLogger().info("önceki sayfa tıklandı" + itemName + "||" + currentPage + "/" + totalPages);
            if (currentPage > 1) {
                new PaginatedRankUpgradeMenu(rankSettingsManager).open(player, currentPage - 1);
                plugin.getLogger().info("önceki sayfaya geçiliyor");
            }
        } else if (itemName.equalsIgnoreCase("Sonraki Sayfa →")) {
            plugin.getLogger().info("sonraki sayfa tıklandı" + itemName + "||" + currentPage + "/" + totalPages);
            if (currentPage < totalPages) {
                new PaginatedRankUpgradeMenu(rankSettingsManager).open(player, currentPage + 1);
                plugin.getLogger().info("sonraki sayfaya geçiliyor");
            }
        }
    }

    private int getCurrentPage(String title) {
        String[] titleParts = title.split(" ");
        return Integer.parseInt(titleParts[titleParts.length - 1]);
    }
}
