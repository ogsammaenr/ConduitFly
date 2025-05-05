package org.ogsammaenr.conduitFly.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.settings.RankSettings;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankUpgradeMenuListener implements Listener {
    ConduitFly plugin;
    RankSettingsManager rankSettingsManager;

    public RankUpgradeMenuListener(ConduitFly plugin) {
        this.plugin = plugin;
        rankSettingsManager = plugin.getRankSettingsManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Başlıkta son kısmı almak için boşlukla ayıralım
        String[] titleParts = title.split(" "); // Başlığı boşluklardan ayırıyoruz
        int page;
        // Son parçayı sayfa numarası olarak alıyoruz
        try {
            page = Integer.parseInt(titleParts[titleParts.length - 1]); // Son parçayı sayfa numarası olarak alıyoruz
            // Artık sayfa numarasını 'page' değişkeninde tutuyoruz
        } catch (NumberFormatException e) {
            // Sayfa numarasını alırken hata oluşursa, işlem yapılmaz
            plugin.getLogger().warning(e.getMessage());
            return;
        }

        String menutitle = plugin.getMessageManager().getRaw("gui-rankup-title").replace("%page%", Integer.toString(page));

        if (!title.equals(menutitle)) return;

        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        Player player = (Player) event.getWhoClicked();

        // Eğer tıklanan envanter null ise işlem yapma
        if (clickedInventory == null) return;

        // Shift tıklama ile GUI'ye eşya aktarılmasını engelle
        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
        // Eğer tıklama üst envantere (GUI) yapılıyorsa iptal et
        if (clickedInventory.equals(topInventory)) {
            event.setCancelled(true);
        }

        // Aşağıdaki kod sadece GUI'ye tıklanmışsa çalışsın
        if (!clickedInventory.equals(topInventory)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            event.setCancelled(true);
            return;
        }

        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        event.setCancelled(true);

        int currentPage = getCurrentPage(title);
        int totalPages = (int) Math.ceil((double) rankSettingsManager.getRankSettingsMap().size() / 4);

        String previousPage = ChatColor.stripColor(plugin.getMessageManager().getRaw("gui-previous-page"));
        String nextPage = ChatColor.stripColor(plugin.getMessageManager().getRaw("gui-next-page"));

        // Sayfa kontrolü
        if (itemName.equals(previousPage)) {
            if (currentPage > 1) {
                new PaginatedRankUpgradeMenu(rankSettingsManager, plugin).open(player, currentPage - 1);
            }
            return;
        } else if (itemName.equals(nextPage)) {
            if (currentPage < totalPages) {
                new PaginatedRankUpgradeMenu(rankSettingsManager, plugin).open(player, currentPage + 1);
            }
            return;
        }

        // Rütbe kontrolü
        if (clicked.getType() == Material.LIGHT_GRAY_DYE) {
            List<RankSettings> sortedRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
            sortedRanks.sort(Comparator.comparingInt(RankSettings::getPriority));


            int clickedIndex = -1;
            for (int i = 0; i < sortedRanks.size(); i++) {
                RankSettings rank = sortedRanks.get(i);
                String display = ChatColor.stripColor("§a" + rank.getDisplayName());

                if (itemName.equals(display)) {
                    clickedIndex = i;
                    break;
                }
            }

            if (clickedIndex == -1) return;

            RankSettings selectedRank = sortedRanks.get(clickedIndex);
            String perm = selectedRank.getPermission();


            if (player.hasPermission(perm)) {
                String message = plugin.getMessageManager().getMessage("rank-already-owned");
                player.sendMessage(message);
                return;
            }

            if (clickedIndex == 0) {
                // Bu rütbeyi almasına izin ver
            } else {
                RankSettings previousRank = sortedRanks.get(clickedIndex - 1);
                String previousPerm = previousRank.getPermission();

                if (!player.hasPermission(previousPerm)) {
                    String message = plugin.getMessageManager().getMessage("rank-requirement").replace("%previous-rank%", previousRank.getDisplayName());
                    player.sendMessage(message);
                    return;
                }
            }

            double price = selectedRank.getPrice();
            double balance = plugin.getEconomy().getBalance(player);

            if (balance > price) {
                plugin.getEconomy().withdrawPlayer(player, price);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + perm);

                String message = plugin.getMessageManager().getMessage("rank-earned").replace("%price%", String.valueOf(price)).replace("%rank%",
                        String.valueOf(rankSettingsManager.getRankSettingsByPermission(perm).getDisplayName()));
                player.sendMessage(message);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    new PaginatedRankUpgradeMenu(rankSettingsManager, plugin).open(player, currentPage);
                }, 5L);
            } else {
                String message = plugin.getMessageManager().getMessage("insufficient-balance").replace("%price%", String.valueOf(price));
                player.sendMessage(message);
            }
            return;


        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        // Başlıkta son kısmı almak için boşlukla ayıralım
        String[] titleParts = title.split(" "); // Başlığı boşluklardan ayırıyoruz
        int page;
        // Son parçayı sayfa numarası olarak alıyoruz
        try {
            page = Integer.parseInt(titleParts[titleParts.length - 1]); // Son parçayı sayfa numarası olarak alıyoruz
            // Artık sayfa numarasını 'page' değişkeninde tutuyoruz
        } catch (NumberFormatException e) {
            // Sayfa numarasını alırken hata oluşursa, işlem yapılmaz
            plugin.getLogger().warning(e.getMessage());
            return;
        }

        String menutitle = plugin.getMessageManager().getRaw("gui-rankup-title").replace("%page%", Integer.toString(page));

        if (!title.equals(menutitle)) return;

        Inventory topInventory = event.getView().getTopInventory();
        for (int slot : event.getRawSlots()) {
            if (slot < topInventory.getSize()) {
                event.setCancelled(true); // GUI'ye sürükleme ile eşya bırakmayı engelle
                return;
            }
        }
    }

    private int getCurrentPage(String title) {
        String[] titleParts = title.split(" ");
        return Integer.parseInt(titleParts[titleParts.length - 1]);
    }
}
