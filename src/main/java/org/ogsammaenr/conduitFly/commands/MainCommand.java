package org.ogsammaenr.conduitFly.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.gui.PaginatedRankUpgradeMenu;

public class MainCommand implements CommandExecutor {

    private final ConduitFly plugin;

    /**************************************************************************************************************/
    //  constructor methodu
    public MainCommand(ConduitFly plugin) {
        this.plugin = plugin;
    }

    /**************************************************************************************************************/
    //  komut girildiğinde çalışır
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /*      /conduitfly girmiş ise help komuduna aktar      */
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        /*      girilen komuda göre uygun yere gönderir     */
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            case "rankup":
                handleRankup(sender);
                break;
            default:
                sender.sendMessage("§cBilinmeyen komut. §7'/conduitfly help' yazarak komutları görebilirsin.");
                break;
        }

        return true;
    }

    /**************************************************************************************************************/
    //  /conduitfly reload yazıldığında çalışır
    private void handleReload(CommandSender sender) {
        /*      komudu yazanın yetkisi var mı kontrol edilir        */
        if (!sender.hasPermission("conduitfly.reload")) {
            sender.sendMessage("§cBu komutu kullanmak için yetkin yok.");
            return;
        }
        /*      plugin reloadlanır      */
        plugin.reloadPlugin();
        sender.sendMessage(ChatColor.GREEN + "Conduitfly Başarıyla Yeniden Yüklendi!");
    }

    /**************************************************************************************************************/
    //  /conduitfly help yazıldığında çalışır
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§b§lConduitFly v2 §7- Yardım Menüsü");
        sender.sendMessage("§e/conduitfly reload §7- Plugin ayarlarını yeniden yükler.");
        sender.sendMessage("§e/conduitfly rankup §7- Rütbe yükseltme menüsünü açar.");
        sender.sendMessage("§e/conduitfly help §7- Bu yardım menüsünü gösterir.");
        sender.sendMessage("§8§m----------------------------------------");
    }

    private void handleRankup(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komutu sadece oyuncular kullanabilir.");
            return;
        }

        Player player = (Player) sender;
        new PaginatedRankUpgradeMenu(plugin.getRankSettingsManager()).open((org.bukkit.entity.Player) sender, 1);
    }
}
