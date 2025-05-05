package org.ogsammaenr.conduitFly.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.gui.PaginatedRankUpgradeMenu;

import java.util.HashMap;
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
            String message = plugin.getMessageManager().getMessage("no-permission");
            sender.sendMessage(message);
            return;
        }
        /*      plugin reloadlanır      */
        plugin.reloadPlugin();

        String message = plugin.getMessageManager().getMessage("reload-success");
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

    private void handleRankup(CommandSender sender) {
        if (!(sender instanceof Player)) {
            String message = plugin.getMessageManager().getMessage("only-players-command");

            sender.sendMessage(message);
            return;
        }

        Player player = (Player) sender;
        new PaginatedRankUpgradeMenu(plugin.getRankSettingsManager(), plugin).open((org.bukkit.entity.Player) sender, 1);
    }


    private final Map<UUID, Long> areaCooldowns = new HashMap<>();
    private static final long AREA_COOLDOWN_MS = 5000; // 5 saniye

    private void handleArea(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            String message = plugin.getMessageManager().getMessage("only-players-command");
            sender.sendMessage(message);
            return;
        }
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (areaCooldowns.containsKey(uuid)) {
            long lastUsed = areaCooldowns.get(uuid);
            long timeSince = now - lastUsed;

            if (timeSince < AREA_COOLDOWN_MS) {
                long secondsLeft = (AREA_COOLDOWN_MS - timeSince) / 1000;
                String message = plugin.getMessageManager().getMessage("cooldown-message").replace("%seconds%", Long.toString(secondsLeft));
                player.sendMessage(message);
                return;
            }
        }
        areaCooldowns.put(uuid, now);
        // Conduit alanını gösterecek sınıfı çağır
        plugin.getParticleDisplayTask().showArea(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        areaCooldowns.remove(event.getPlayer().getUniqueId());
    }
}
