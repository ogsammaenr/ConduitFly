package org.ogsammaenr.conduitFly.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ogsammaenr.conduitFly.settings.RankSettings;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaginatedRankUpgradeMenu {

    private final RankSettingsManager rankSettingsManager;
    private final int[] itemSlots = {10, 12, 14, 16};

    public PaginatedRankUpgradeMenu(RankSettingsManager rankSettingsManager) {
        this.rankSettingsManager = rankSettingsManager;
    }

    public void open(Player player, int page) {
        List<RankSettings> sortedRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
        sortedRanks.sort(Comparator.comparingInt(RankSettings::getPriority));

        int totalPages = (int) Math.ceil((double) sortedRanks.size() / itemSlots.length);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        Inventory gui = Bukkit.createInventory(null, 36, "§bRütbe Yükseltme §7- Sayfa " + page);

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
            gui.setItem(27, createControlItem("§c← Önceki Sayfa", Material.ARROW));
        }

        // İleri butonu
        if (page < totalPages) {
            gui.setItem(35, createControlItem("§aSonraki Sayfa →", Material.ARROW));
        }

        player.openInventory(gui);
    }

    private ItemStack createRankItem(RankSettings rank, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§a" + rank.getDisplayName());

        List<String> lore = new ArrayList<>();
        lore.add("§7Alan çapı: §f" + rank.getRadius());
        lore.add("§7Uçuş süresi: §f" + rank.getDuration() + " saniye");
        lore.add("§7Düşme hasarı engeli: §f" + (rank.shouldPreventFallDamage() ? "§aAçık" : "§cKapalı"));
        lore.add("§7Fiyatı: §f" + rank.getPrice());

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
