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
    private final int[] itemSlots = {10, 12, 14, 16};

    public PaginatedRankUpgradeMenu(RankSettingsManager rankSettingsManager, ConduitFly plugin) {
        this.rankSettingsManager = rankSettingsManager;
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        List<RankSettings> sortedRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
        sortedRanks.sort(Comparator.comparingInt(RankSettings::getPriority));

        int totalPages = (int) Math.ceil((double) sortedRanks.size() / itemSlots.length);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        String message = plugin.getMessageManager().getRaw("gui-rankup-title").replace("%page%", String.valueOf(page));
        Inventory gui = Bukkit.createInventory(null, 36, message);

        int startIndex = (page - 1) * itemSlots.length;
        int endIndex = Math.min(startIndex + itemSlots.length, sortedRanks.size());

        for (int i = startIndex, slotIndex = 0; i < endIndex; i++, slotIndex++) {
            if (player.hasPermission(sortedRanks.get(i).getPermission()))
                gui.setItem(itemSlots[slotIndex], createRankItem(sortedRanks.get(i), Material.LIME_DYE));

            else
                gui.setItem(itemSlots[slotIndex], createRankItem(sortedRanks.get(i), Material.LIGHT_GRAY_DYE));
        }

        // Geri butonu
        if (page > 1) {
            String previousPage = plugin.getMessageManager().getRaw("gui-previous-page");
            gui.setItem(27, createControlItem(previousPage, Material.ARROW));
        }

        // İleri butonu
        if (page < totalPages) {
            String nextPage = plugin.getMessageManager().getRaw("gui-next-page");
            gui.setItem(35, createControlItem(nextPage, Material.ARROW));
        }

        player.openInventory(gui);
    }

    private ItemStack createRankItem(RankSettings rank, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§a" + rank.getDisplayName());

        List<String> loreTemplate = plugin.getMessageManager().getList("rank-lore"); // messages.yml'den çekiliyor
        List<String> lore = new ArrayList<>();

        String fallOn = plugin.getMessageManager().getRaw("fall-damage-on");
        String fallOff = plugin.getMessageManager().getRaw("fall-damage-off");
        for (String line : loreTemplate) {
            line = line
                    .replace("%radius%", String.valueOf(rank.getRadius()))
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
}
