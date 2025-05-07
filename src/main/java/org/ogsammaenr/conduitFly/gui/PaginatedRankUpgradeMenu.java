package org.ogsammaenr.conduitFly.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.settings.RankSettings;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaginatedRankUpgradeMenu {

    private final RankSettingsManager rankSettingsManager;
    private final ConduitFly plugin;
    /*      Rütbelerin duracağı slotlar     */
    private final List<Integer> itemSlots;

    public PaginatedRankUpgradeMenu(RankSettingsManager rankSettingsManager, ConduitFly plugin) {
        this.rankSettingsManager = rankSettingsManager;
        this.plugin = plugin;
        this.itemSlots = plugin.getConfig().getIntegerList("rank-gui.item-slots");
    }

    public void open(Player player, int page) {

        Material activeMaterial = Material.valueOf(plugin.getConfig().getString("rank-gui.active-rank-material", "LIME_DYE"));
        Material inactiveMaterial = Material.valueOf(plugin.getConfig().getString("rank-gui.inactive-rank-material", "LIGHT_GRAY_DYE"));
        Material fillerMaterial = Material.valueOf(plugin.getConfig().getString("rank-gui.filler-material", "GRAY_STAINED_GLASS_PANE"));
        Material bottomBarMaterial = Material.valueOf(plugin.getConfig().getString("rank-gui.bottom-bar-material", "BLACK_STAINED_GLASS_PANE"));
        Material previousPageMaterial = Material.valueOf(plugin.getConfig().getString("rank-gui.previous-page-material", "ARROW"));
        Material nextPageMaterial = Material.valueOf(plugin.getConfig().getString("rank-gui.next-page-material", "ARROW"));

        List<RankSettings> sortedRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
        sortedRanks.sort(Comparator.comparingInt(RankSettings::getPriority));

        int totalPages = (int) Math.ceil((double) sortedRanks.size() / itemSlots.size());
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        String message = plugin.getMessageManager().getRaw("rank.gui.title").replace("%page%", String.valueOf(page));
        Inventory gui = Bukkit.createInventory(null, 36, message);

        ItemStack filler = createFillerItem(fillerMaterial);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        ItemStack bottomBar = createBottomBarItem(bottomBarMaterial);
        for (int i = 27; i <= 35; i++) {
            gui.setItem(i, bottomBar);
        }

        int startIndex = (page - 1) * itemSlots.size();
        int endIndex = Math.min(startIndex + itemSlots.size(), sortedRanks.size());

        for (int i = startIndex, slotIndex = 0; i < endIndex; i++, slotIndex++) {
            int slot = itemSlots.get(slotIndex);
            RankSettings rank = sortedRanks.get(i);

            if (player.hasPermission(sortedRanks.get(i).getPermission()))
                gui.setItem(slot, createRankItem(rank, activeMaterial));

            else
                gui.setItem(slot, createRankItem(rank, inactiveMaterial));
        }

        // Geri butonu
        if (page > 1) {
            String previousPage = plugin.getMessageManager().getRaw("rank.gui.previous-page");
            gui.setItem(27, createControlItem(previousPage, previousPageMaterial));
        }

        // İleri butonu
        if (page < totalPages) {
            String nextPage = plugin.getMessageManager().getRaw("rank.gui.next-page");
            gui.setItem(35, createControlItem(nextPage, nextPageMaterial));
        }

        player.openInventory(gui);
    }

    private ItemStack createRankItem(RankSettings rank, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§a" + rank.getDisplayName());

        List<String> loreTemplate = plugin.getMessageManager().getList("rank.lore"); // messages.yml'den çekiliyor
        List<String> lore = new ArrayList<>();

        String fallOn = plugin.getMessageManager().getRaw("rank.fall-damage-on");
        String fallOff = plugin.getMessageManager().getRaw("rank.fall-damage-off");
        for (String line : loreTemplate) {
            line = line
                    .replace("%radius%", String.valueOf(rank.getRadius() - 0.5))
                    .replace("%duration%", String.valueOf(rank.getDuration()))
                    .replace("%price%", String.valueOf(rank.getPrice()))
                    .replace("%fall-damage%", rank.shouldPreventFallDamage() ? fallOn : fallOff);
            lore.add(line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createControlItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }


    private ItemStack createFillerItem(Material material) {
        ItemStack item = new ItemStack(material); // veya istediğin başka bir item
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8"); // Görünmeyen isim (isteğe bağlı)
            item.setItemMeta(meta);
        }
        return item;
    }


    private ItemStack createBottomBarItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8"); // Boş isim
            item.setItemMeta(meta);
        }
        return item;
    }

}
