package org.ogsammaenr.conduitFlyv2.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ogsammaenr.conduitFlyv2.settings.RankSettings;
import org.ogsammaenr.conduitFlyv2.settings.RankSettingsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankUpgradeMenu {
    private final RankSettingsManager rankSettingsManager;

    public RankUpgradeMenu(RankSettingsManager rankSettingsManager) {
        this.rankSettingsManager = rankSettingsManager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§bRütbe Yükseltme Menüsü");

        // Arka plan: cam panel
        ItemStack filler = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }

        // Rütbeleri sıraya koy
        List<RankSettings> allRanks = new ArrayList<>(rankSettingsManager.getRankSettingsMap().values());
        allRanks.sort(Comparator.comparingInt(RankSettings::getPriority));

        // Merkez slotlara yerleştir (10, 12, 14, 16 gibi)
        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < Math.min(allRanks.size(), slots.length); i++) {
            gui.setItem(slots[i], createRankItem(allRanks.get(i)));
        }

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRankItem(RankSettings rank) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§a" + rank.getPermission());

        List<String> lore = new ArrayList<>();
        lore.add("§7Alan çapı: §f" + rank.getRadius());
        lore.add("§7Uçuş süresi: §f" + rank.getDuration() + " saniye");
        lore.add("§7Düşme hasarı engeli: §f" + (rank.shouldPreventFallDamage() ? "§aAçık" : "§cKapalı"));
        lore.add("§e§lTıkla ve yükselt!");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
