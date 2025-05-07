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

    /**************************************************************************************************************/
    //  Constructor methodu
    public RankUpgradeMenuListener(ConduitFly plugin) {
        this.plugin = plugin;
        rankSettingsManager = plugin.getRankSettingsManager();
    }

    /**************************************************************************************************************/
    //  Oyuncu envantere tıkladığında çalışır
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle(); /*   Tıklanan envanterin başlığını alır*/

        /*      Başlığı boşluklardan parçalara ayırır      */
        String[] titleParts = title.split(" ");
        int page;

        /*      Başlığın son parçasını sayfa sayısı olarak almaya çalışır       */
        try {
            page = Integer.parseInt(titleParts[titleParts.length - 1]);

        } catch (NumberFormatException e) {
            /*      eğer bir hata olursa işlem yapmaz       */
            return;
        }

        /*      Yükseltme menüsünün adı messages.yml dosyasından alınır     */
        String menutitle = plugin.getMessageManager().getRaw("rank.gui.title").
                replace("%page%", Integer.toString(page));

        /*      tıklanan menünün adı yükseltme menüsünün adıyla aynı değilse işlem yapmaz*/
        if (!title.equals(menutitle)) return;

        /*      menü ile oyuncunun envanteri alınır     */
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        /*      Tıklayan oyuncuyu alır      */
        Player player = (Player) event.getWhoClicked();

        /*      tıklanan envanter boş ise işlem yapmaz      */
        if (clickedInventory == null) return;

        /*      shift click yapılmış ise engeller       */
        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
        /*      Eğer tıklanan yer menü ise tıklanmayı engelle       */
        if (clickedInventory.equals(topInventory)) {
            event.setCancelled(true);
        }

        /*      menüye tıklanmamışsa işlem yapmaz       */
        if (!clickedInventory.equals(topInventory)) return;

        /*      tıklanan slot boş ise tıklanma engellenir ve devam etmez        */
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        /*      Tıklanan itemin bilgileri alınır boş ise devam etmez        */
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            event.setCancelled(true);
            return;
        }

        /*      tıklanan itemin adı renksiz olarak alınır       */
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        /*      tıklanma engellenir     */
        event.setCancelled(true);


        int currentPage = getCurrentPage(title);
        int totalPages = (int) Math.ceil((double) rankSettingsManager.getRankSettingsMap().size() / 4);

        String previousPage = ChatColor.stripColor(plugin.getMessageManager().getRaw("rank.gui.previous-page"));
        String nextPage = ChatColor.stripColor(plugin.getMessageManager().getRaw("rank.gui.next-page"));

        // Sayfa kontrolü
        /*      tıklanan itemin adı önceki sayfa itemiyle aynı ise sayfa değişir*/
        if (itemName.equals(previousPage)) {
            if (currentPage > 1) {
                new PaginatedRankUpgradeMenu(rankSettingsManager, plugin).open(player, currentPage - 1);
            }
            return;
        }
        /*      tıklanan itemin adı sonraki sayfa itemiyle aynı ise sayfa değişir*/
        else if (itemName.equals(nextPage)) {
            if (currentPage < totalPages) {
                new PaginatedRankUpgradeMenu(rankSettingsManager, plugin).open(player, currentPage + 1);
            }
            return;
        }

        // Rütbe kontrolü
        /*      Tıklanan item gri boya ise devam eder       */
        if (clicked.getType() == Material.LIGHT_GRAY_DYE) {
            /*      Rütbeler RankSettingsManager sınıfından alınır*/
            List<RankSettings> sortedRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
            /*      Rütbeler önem sırasına göre sıralanır*/
            sortedRanks.sort(Comparator.comparingInt(RankSettings::getPriority));


            int clickedIndex = -1;
            /*      tıklanan itemin hangi rütbeyi belirttiği bulunur*/
            for (int i = 0; i < sortedRanks.size(); i++) {
                RankSettings rank = sortedRanks.get(i);
                String display = ChatColor.stripColor("§a" + rank.getDisplayName());

                if (itemName.equals(display)) {
                    clickedIndex = i;
                    break;
                }
            }

            if (clickedIndex == -1) return;

            /*      tıklanan itemin rütbesi alınır*/
            RankSettings selectedRank = sortedRanks.get(clickedIndex);
            String perm = selectedRank.getPermission();

            /*      oyuncu zaten bu perme sahipse devam etmez*/
            if (player.hasPermission(perm)) {
                String message = plugin.getMessageManager().getMessage("rank.already-owned");
                player.sendMessage(message);
                return;
            }

            if (clickedIndex == 0) {
                // Bu rütbeyi almasına izin ver
            } else {
                RankSettings previousRank = sortedRanks.get(clickedIndex - 1);
                String previousPerm = previousRank.getPermission();
                /*      oyuncu önceki rütbelere sahip mi kontrol edilir*/
                if (!player.hasPermission(previousPerm)) {
                    String message = plugin.getMessageManager().getMessage("rank.requirement").replace("%previous-rank%", previousRank.getDisplayName());
                    player.sendMessage(message);
                    return;
                }
            }

            double price = selectedRank.getPrice();
            double balance = plugin.getEconomy().getBalance(player);

            /*      oyuncunun bakiyesi kontrol edilir       */
            if (balance > price) {
                plugin.getEconomy().withdrawPlayer(player, price);
                /*      konsol üzerinden oyuncuya rütbe verilir     */
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + perm);

                String message = plugin.getMessageManager().getMessage("rank.earned").replace("%price%", String.valueOf(price)).replace("%rank%",
                        String.valueOf(rankSettingsManager.getRankSettingsByPermission(perm).getDisplayName()));
                player.sendMessage(message);

                /*      5 tick gecikmeli olarak menüyü yeniler      */
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    new PaginatedRankUpgradeMenu(rankSettingsManager, plugin).open(player, currentPage);
                }, 5L);
            } else {
                /*      oyuncuya mesaj gönderir*/
                String message = plugin.getMessageManager().getMessage("rank.insufficient-balance").replace("%price%", String.valueOf(price));
                player.sendMessage(message);
            }
            return;


        }

        event.setCancelled(true);
    }

    /**************************************************************************************************************/

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        /*      başlığı parçalara ayıralım      */
        String[] titleParts = title.split(" ");
        int page;

        /*      son parçayı sayfa sayısı olarak almaya çalışır*/
        try {
            page = Integer.parseInt(titleParts[titleParts.length - 1]);

        } catch (NumberFormatException e) {
            /*      sayfa sayısı alınamazsa çalışmaz*/
            return;
        }

        /*      menünün ismi messages.ymlden alınır     */
        String menutitle = plugin.getMessageManager().getRaw("rank.gui.title").replace("%page%", Integer.toString(page));

        /*      tıklanan menü ismi aynı değilse işlem yapmaz*/
        if (!title.equals(menutitle)) return;

        /*      menüdeki sürükleme işlemleri engellenir*/
        Inventory topInventory = event.getView().getTopInventory();
        for (int slot : event.getRawSlots()) {
            if (slot < topInventory.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**************************************************************************************************************/
    //  aktif sayfayı döndürür
    private int getCurrentPage(String title) {
        String[] titleParts = title.split(" ");
        return Integer.parseInt(titleParts[titleParts.length - 1]);
    }
}
