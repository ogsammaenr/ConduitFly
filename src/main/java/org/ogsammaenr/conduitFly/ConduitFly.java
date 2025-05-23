package org.ogsammaenr.conduitFly;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.ogsammaenr.conduitFly.commands.CommandTabCompleter;
import org.ogsammaenr.conduitFly.commands.MainCommand;
import org.ogsammaenr.conduitFly.gui.RankUpgradeMenuListener;
import org.ogsammaenr.conduitFly.listeners.ConduitListener;
import org.ogsammaenr.conduitFly.listeners.IslandEventListener;
import org.ogsammaenr.conduitFly.manager.MessageManager;
import org.ogsammaenr.conduitFly.manager.PermissionManager;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;
import org.ogsammaenr.conduitFly.storage.*;
import org.ogsammaenr.conduitFly.tasks.FlightCheckTask;
import org.ogsammaenr.conduitFly.tasks.FlightTimeTask;
import org.ogsammaenr.conduitFly.tasks.ParticleDisplayTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ConduitFly extends JavaPlugin implements Listener {

    private ConduitCache conduitCache;
    private ConduitStorage conduitStorage;
    private FlightTimeTask flightTimeTask;
    private RankSettingsManager rankSettingsManager;
    private FileConfiguration config;
    private PermissionManager permissionManager;
    private ConduitListener conduitListener;
    private MessageManager messageManager;
    private ParticleDisplayTask particleDisplayTask;
    private BukkitTask particleTask;

    private ConduitDataStorage conduitDataStorage;

    private static Economy economy;

    private final ConcurrentHashMap<UUID, Double> areaToggles = new ConcurrentHashMap<>();

    /**************************************************************************************************************/
    //  sdece sunucu başlarken çalışır diğer sınıflar bu metod sayesinde bir işe yarar
    @Override
    public void onEnable() {
        // Config dosyasını yükle
        saveDefaultConfig();

        String storageType = getConfig().getString("storage.type", "sqlite").toLowerCase();

        if (storageType.equals("sqlite")) {
            getLogger().info("Using SQLite storage");
            this.conduitDataStorage = new SQLiteConduitStorage(this);
        } else if (storageType.equals("mysql")) {
            getLogger().info("Using MySQL storage");

            String host = getConfig().getString("storage.mysql.host");
            String port = getConfig().getString("storage.mysql.port");
            String database = getConfig().getString("storage.mysql.database");
            String username = getConfig().getString("storage.mysql.username");
            String password = getConfig().getString("storage.mysql.password");

            MySQLConnection connection = new MySQLConnection(host, port, database, username, password);

            if (!connection.testConnection()) {
                getLogger().severe("*****MySQL connection failed! Disabling the plugin.*****");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            this.conduitDataStorage = new MySQLConduitStorage(connection);

        } else {
            getLogger().warning("Unknown storage type in config.yml! Defaulting to SQLite.");
            this.conduitDataStorage = new SQLiteConduitStorage(this);
        }

        this.config = getConfig();

        new PermissionManager(this).loadPermissions();

        registerCustomConduitRecipe();

        if (!setupEconomy()) {
            getLogger().severe("Vault or a compatible economy plugin is missing!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // RankSettingsManager'ı başlat
        this.rankSettingsManager = new RankSettingsManager(config, this);

        this.conduitCache = new ConduitCache(this);

        this.flightTimeTask = new FlightTimeTask(this);
        this.permissionManager = new PermissionManager(this);
        this.conduitListener = new ConduitListener(this);
        this.messageManager = new MessageManager(this);


        /*  dünyalar yüklendikten sonra dosyadaki veriler belleğe yüklenir*/
        getServer().getScheduler().runTask(this, () -> {
            Map<String, List<Location>> loadedData = conduitDataStorage.loadAll();
            this.conduitCache.getAllConduits().putAll(loadedData);
            getLogger().info("Loaded " + loadedData.size() + " islands from " + storageType + ".");

            getLogger().info("*************Conduits loaded***************");
        });

        /*  diğer sınıfların kaydı yapılır  */
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(conduitListener, this);
        pm.registerEvents(new IslandEventListener(this), this);
        pm.registerEvents(new FlightCheckTask(this), this);
        pm.registerEvents(new RankUpgradeMenuListener(this), this);
        pm.registerEvents(this, this);
        flightTimeTask.runTaskTimer(this, 20L, 20L);

        /**=========Partükül ayarları=========*/

        long delay = getConfig().getLong("particles.delay", 40);
        String particleType = getConfig().getString("particles.type");
        particleTask = new ParticleDisplayTask(this, particleType).runTaskTimer(this, 0L, delay);

        /**===================================*/

        getCommand("conduitfly").setExecutor(new MainCommand(this));
        getCommand("conduitfly").setTabCompleter(new CommandTabCompleter());

        /*  sunucunun başladığını konsolda belirtir  */
        getLogger().info("*******ConduitFly enabled!*******");

    }

    /**************************************************************************************************************/
    //  sadece sunucu kapanırken çalışır
    @Override
    public void onDisable() {
        /*  Sunucu kapatılırken cache'deki conduit verilerini kaydet  */
        if (conduitDataStorage != null) {
            conduitDataStorage.saveAll(conduitCache.getAllConduits());
        }

        if (conduitDataStorage instanceof MySQLConduitStorage) {
            getLogger().info("MySQL is active - backing up to the SQLite file...");
            try {
                Map<String, List<Location>> allData = conduitDataStorage.loadAll();

                SQLiteConduitStorage sqliteStorage = new SQLiteConduitStorage(this);
                sqliteStorage.saveAll(allData);

                getLogger().info("MySQL data has been successfully backed up to the conduits.db file.");
            } catch (Exception e) {
                getLogger().warning("SQLite backup failed: " + e.getMessage());
            }
        }

        /*  pluginin düzgünce kapandığını konsola yaz*/
        getLogger().info("*******ConduitFly disabled!*******");
    }


    /**************************************************************************************************************/
    //  getterlar
    public ConduitCache getConduitCache() {
        return conduitCache;
    }

    public ConduitStorage getConduitStorage() {
        return conduitStorage;
    }

    public FlightTimeTask getFlightTimeTask() {
        return flightTimeTask;
    }

    public RankSettingsManager getRankSettingsManager() {
        return rankSettingsManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ParticleDisplayTask getParticleDisplayTask() {
        return particleDisplayTask;
    }

    public Map<UUID, Double> getAreaToggles() {
        return areaToggles;
    }

    public ConduitDataStorage getConduitDataStorage() {
        return conduitDataStorage;
    }

    /**************************************************************************************************************/
    //  plugini reloadlar
    public void reloadPlugin() {
        /*      config.yml yeniden yüklenir     */
        reloadConfig();

        /*      permler yeniden oluşturulur     */
        this.permissionManager = new PermissionManager(this);
        permissionManager.loadPermissions();

        /*      rütbe ayarları yeniden oluşturulur      */
        rankSettingsManager.loadRankSettings(getConfig());

        if (particleTask != null) {
            particleTask.cancel();
        }
        long delay = getConfig().getLong("particles.delay", 40);
        String particleType = getConfig().getString("particles.type");
        particleTask = new ParticleDisplayTask(this, particleType).runTaskTimer(this, 0L, delay);


        /*      conduit materyali configden alınır      */
        Material material = Material.matchMaterial(getConfig().getString("conduit.material"));

        /*      hata var mı yok mu kontrol edilir       */
        if (material == null) {
            getLogger().warning("Invalid Conduit Material Provided. Using default : CONDUIT");
            material = Material.CONDUIT;
        }
        /*      conduit materyali güncellenir       */
        conduitListener.updateConduitMaterial(material);

        this.messageManager = new MessageManager(this);

        getLogger().info("ConduitFly settings reloaded successfully!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (areaToggles.containsKey(uuid)) {
            areaToggles.remove(uuid);
        }
    }

    public void registerCustomConduitRecipe() {
        FileConfiguration config = getConfig();
        NamespacedKey oldRecipeKey = new NamespacedKey(this, "custom_conduit");

        if (!config.getBoolean("custom-recipe", true)) {
            getLogger().info("Custom conduit recipe has been disabled");
            Bukkit.removeRecipe(oldRecipeKey);
            return;
        }

        // Eski custom conduit tarifini kaldırıyoruz

        Bukkit.removeRecipe(oldRecipeKey);
        getLogger().info("Old custom conduit recipe has been removed.");


        NamespacedKey vanillaKey = NamespacedKey.minecraft("conduit");
        boolean removed = Bukkit.removeRecipe(vanillaKey);
        if (removed) {
            getLogger().info("Default conduit recipe has been removed.");
        } else {
            getLogger().warning("Default conduit recipe has not been removed.");
        }

        List<String> shapeList = config.getStringList("conduit-recipe.shape");
        if (shapeList.size() != 3) {
            getLogger().warning("Invalid recipe shape! 3 lines expected.");
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("conduit-recipe.ingredients");
        if (section == null) {
            getLogger().warning("Ingredients not found.");
            return;
        }

        Map<Character, Material> ingredientMap = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Material mat = Material.matchMaterial(section.getString(key));
            if (mat != null && key.length() == 1) {
                ingredientMap.put(key.charAt(0), mat);
            } else {
                getLogger().warning("Invalid ingredient definition: " + key);
            }
        }

        // Şekilde kullanılan harflerin malzemesi tanımlı mı?
        for (String row : shapeList) {
            for (char c : row.toCharArray()) {
                if (c != ' ' && !ingredientMap.containsKey(c)) {
                    getLogger().warning("Undefined letter in recipe shape: '" + c + "'");
                }
            }
        }
        // Config'ten conduit materyalini alıyoruz
        String conduitMaterialName = config.getString("conduit.material", "CONDUIT");
        Material conduitMaterial = Material.matchMaterial(conduitMaterialName);

        if (conduitMaterial == null) {
            getLogger().warning("Invalid conduit material! Default conduit is being used.");
            conduitMaterial = Material.CONDUIT;  // Varsayılan conduit
        }

        ItemStack result = new ItemStack(conduitMaterial);
        NamespacedKey recipeKey = new NamespacedKey(this, "custom_conduit");

        // Önceki tarif varsa kaldır
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe r = it.next();
            if (r instanceof ShapedRecipe shaped && shaped.getKey().equals(recipeKey)) {
                it.remove();
            }
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape(shapeList.toArray(new String[0]));
        ingredientMap.forEach(recipe::setIngredient);

        Bukkit.addRecipe(recipe);
        getLogger().info("****Custom conduit recipe successfully loaded.****");
    }

}
