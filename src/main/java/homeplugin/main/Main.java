package homeplugin.main;

import homeplugin.listener.PlayerJoinListener;
import homeplugin.others.Home;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import homeplugin.cmds.HomeCommand;
import homeplugin.cmds.HomesCommand;
import homeplugin.listener.InventoryClickListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin {

    FileConfiguration cfg = getConfig();

    private static Main plugin;

    public static String prefix = "";
    public static String no_perm = prefix + "";

    public static HashMap<Player, Integer> page = new HashMap<>();
    public static HashMap<Player, String> lastGui = new HashMap<>();
    public static HashMap<Player, Home> currentHome = new HashMap<>();

    @Override
    public void onEnable() {

        plugin = this;
        setUp();

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new InventoryClickListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);

        getCommand("home").setExecutor(new HomeCommand());
        getCommand("homes").setExecutor(new HomesCommand());

        Bukkit.getServer().getConsoleSender().sendMessage(prefix + " §6by Vampucha successfully loaded!");
        if (!prefix.equals(""))
            prefix += " §f| ";
    }

    public void setUp() {

        if (!cfg.contains("Settings.ChatMessages.Prefix")) {
            cfg.set("Settings.ChatMessages.Prefix", "§cHomePlugin");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Settings.ChatMessages.NoPermission")) {
            cfg.set("Settings.ChatMessages.NoPermission", "§cYou don't have the permission to do that!");
            getPlugin().saveConfig();
        }

        prefix = cfg.getString("Settings.ChatMessages.Prefix");
        no_perm = cfg.getString("Settings.ChatMessages.NoPermission");

        if (!cfg.contains("Settings.ChatMessages.Create")) {
            cfg.set("Settings.ChatMessages.Create.Success", "§aYou successfully created the home §6%name%");
            cfg.set("Settings.ChatMessages.Create.IsExisting", "§cThe home §6%name% §cis already existing!");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Settings.ChatMessages.Delete")) {
            cfg.set("Settings.ChatMessages.Delete.Success", "§aYou successfully deleted the home §6%name%");
            cfg.set("Settings.ChatMessages.Delete.NotExisting", "§cThe home §6%name% §cis not existing!");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Settings.ChatMessages.Modify")) {
            cfg.set("Settings.ChatMessages.Modify.NotExisting", "§cThe home §6%name% §cis not existing!");
            cfg.set("Settings.ChatMessages.Modify.Name.Success", "§aYou successfully changed the name to §6%name%");
            cfg.set("Settings.ChatMessages.Modify.Location.Success", "§aYou successfully changed the location to §6X: %x% Y: %y% Z: %z%");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Settings.ChatMessages.Visit")) {
            cfg.set("Settings.ChatMessages.Visit.Success", "§aYou successfully visited the home §6%name%");
            cfg.set("Settings.ChatMessages.Visit.NotExisting", "§cThe home §6%name% §cis not existing!");
            getPlugin().saveConfig();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            setUpPlayer(p);
        }
    }

    public static void setUpPlayer(Player p) {

        FileConfiguration cfg = getPlugin().getConfig();

        page.put(p, 1);
        lastGui.put(p, null);
        currentHome.put(p, null);

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.ShowInformation")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.ShowInformation", true);
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.DeleteProtection", true);
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Orientation")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Orientation", true);
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Visitors")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled", true);
            cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist", new ArrayList<String>());
            cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist", new ArrayList<String>());
            getPlugin().saveConfig();
        }

        // sorting-homelist
        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "name");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction", "rising");
            getPlugin().saveConfig();
        }

        // sorting-iconlist
        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type", "name");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction", "rising");
            getPlugin().saveConfig();
        }

        // sorting-playerlist
        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "name");
            getPlugin().saveConfig();
        }

        if (!cfg.contains("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction")) {
            cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "rising");
            getPlugin().saveConfig();
        }
    }

    public static Main getPlugin() {
        return plugin;
    }
}
