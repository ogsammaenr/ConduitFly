package org.ogsammaenr.conduitFly.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.gui.PaginatedRankUpgradeMenu;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            case "area":
                handleArea(sender);
                break;
            default:
                String message = plugin.getMessageManager().getMessage("general.unknown-command");
                sender.sendMessage(message);
                break;
        }

        return true;
    }

    /**************************************************************************************************************/
    //  /conduitfly reload yazıldığında çalışır
    private void handleReload(CommandSender sender) {
        /*      komudu yazanın yetkisi var mı kontrol edilir        */
        if (!sender.hasPermission("conduitfly.reload")) {
            String message = plugin.getMessageManager().getMessage("general.no-permission");
            sender.sendMessage(message);
            return;
        }
        /*      plugin reloadlanır      */
        plugin.reloadPlugin();
        plugin.registerCustomConduitRecipe();

        String message = plugin.getMessageManager().getMessage("general.reload-success");
        sender.sendMessage(message);
    }

    /**************************************************************************************************************/
    //  /conduitfly help yazıldığında çalışır
    private void sendHelp(CommandSender sender) {

        List<String> helpMessages = plugin.getMessageManager().getList("help-menu");
        for (String line : helpMessages) {
            sender.sendMessage(line);
        }

    }

    /**************************************************************************************************************/
    //  /conduitfly rankup yazıldığında çalışır
    private void handleRankup(CommandSender sender) {
        if (!(sender instanceof Player)) {
            String message = plugin.getMessageManager().getMessage("general.only-players");

            sender.sendMessage(message);
            return;
        }

        Player player = (Player) sender;
        new PaginatedRankUpgradeMenu(plugin.getRankSettingsManager(), plugin).open((org.bukkit.entity.Player) sender, 1);
    }

    /**************************************************************************************************************/
    //  /conduitfly area yazıldığında çalışır
    private void handleArea(CommandSender sender) {
        /*      komudu yazan kişi oyuncu mu kontrol edilir      */
        if (!(sender instanceof Player player)) {
            String message = plugin.getMessageManager().getMessage("general.only-players");
            sender.sendMessage(message);
            return;
        }

        UUID uuid = player.getUniqueId();

        double y = player.getLocation().getY();

        Map<UUID, Double> toggles = plugin.getAreaToggles();

        /*      gösterim açık mı kapalı mı kontrol edilir*/
        if (toggles.containsKey(uuid)) {
            toggles.remove(uuid);
            String message = plugin.getMessageManager().getMessage("conduit.area-toggle-off");
            player.sendMessage(message);
        } else {
            toggles.put(uuid, y);
            String message = plugin.getMessageManager().getMessage("conduit.area-toggle-on");
            player.sendMessage(message);
        }
    }


}
