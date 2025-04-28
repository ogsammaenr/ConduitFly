package org.ogsammaenr.conduitFlyv2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;

public class MainCommand implements CommandExecutor {

    private final ConduitFlyv2 plugin;

    public MainCommand(ConduitFlyv2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage("§cBilinmeyen komut. §7'/conduitfly help' yazarak komutları görebilirsin.");
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("conduitfly.reload")) {
            sender.sendMessage("§cBu komutu kullanmak için yetkin yok.");
            return;
        }

        plugin.reloadPlugin();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§b§lConduitFly v2 §7- Yardım Menüsü");
        sender.sendMessage("§e/conduitfly reload §7- Plugin ayarlarını yeniden yükler.");
        sender.sendMessage("§e/conduitfly help §7- Bu yardım menüsünü gösterir.");
        sender.sendMessage("§8§m----------------------------------------");
    }
}
