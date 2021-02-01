package homeplugin.others;

import java.util.*;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import homeplugin.main.Main;
import org.bukkit.inventory.meta.SkullMeta;

public class Inventories {

    static FileConfiguration cfg = Main.getPlugin().getConfig();

    public enum InventoryType {
        DELETE, HOME, SETTINGS
    }

    public static void openHomeList(Player p, Player target) {

        Inventory inv = Bukkit.createInventory(target, 6 * 9, "Homes of " + target.getName());
        int page = Main.page.get(p);

        // ItemStacks
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemStack wBook = new ItemStack(Material.WRITABLE_BOOK);

        // ItemMetas
        ItemMeta barrierMeta = barrier.getItemMeta();
        ItemMeta wBookMeta = wBook.getItemMeta();

        // Lores
        ArrayList<String> barrierLore = new ArrayList<>();
        ArrayList<String> wBookLore = new ArrayList<>();

        // close
        barrierMeta.setDisplayName("§eClose");
        barrierLore.add("§7Click to close");
        barrierMeta.setLore(barrierLore);
        barrier.setItemMeta(barrierMeta);
        inv.setItem(49, barrier);

        // settings
        wBookMeta.setDisplayName("§eSettings");
        wBookLore.add("§7Click to open the settings");
        wBookMeta.setLore(wBookLore);
        wBook.setItemMeta(wBookMeta);
        inv.setItem(45, wBook);

        if (getHomes(target) != null && getHomes(target).size() > 0) {

            // ItemStacks
            ItemStack arrow = new ItemStack(Material.ARROW);
            ItemStack hopper = new ItemStack(Material.HOPPER);

            // ItemMetas
            ItemMeta arrowMeta = arrow.getItemMeta();
            ItemMeta hopperMeta = hopper.getItemMeta();

            // Lores
            ArrayList<String> arrowLore = new ArrayList<>();
            ArrayList<String> hopperLore = new ArrayList<>();

            // next page
            if (getHomes(target).size() > page * 45) {
                arrowMeta.setDisplayName("§eNext page");
                arrowLore.add("§7Click to go to the next page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(50, arrow);
            }

            // previous page
            if (page > 1) {
                arrowMeta.setDisplayName("§ePrevious page");
                arrowLore.clear();
                arrowLore.add("§7Click to go to the previous page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(48, arrow);
            }

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction");
            hopperMeta.setDisplayName("§eSorting");
            hopperLore.add("§7Current: " + sorting + " | " + direction);
            hopperLore.add("§7Left-click to change type");
            hopperLore.add("§7Right-click to change direction");
            hopperMeta.setLore(hopperLore);
            hopper.setItemMeta(hopperMeta);
            inv.setItem(46, hopper);

            // homes
            ArrayList<String> homes = sortHomeList(getHomes(target), p);

            for (int i = (page - 1) * 45; i < page * 45; i++) {
                if (homes.size() > i) {
                    Home home = new Home(homes.get(i), target);
                    ItemStack icon = home.getIcon();
                    ItemMeta iconMeta = icon.getItemMeta();
                    ArrayList<String> iconLore = new ArrayList<>();

                    Location loc = home.getLocation();
                    double x = (double) Math.round(loc.getX() * 100) / 100;
                    double y = (double) Math.round(loc.getY() * 100) / 100;
                    double z = (double) Math.round(loc.getZ() * 100) / 100;

                    if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.ShowInformation")) {
                        iconLore.add("§7World: " + loc.getWorld().getName());
                        iconLore.add("§7X: " + x + " Y: " + y + " Z: " + z);
                        iconLore.add("§7--------------------");
                    }
                    if (p == target) {
                        iconLore.add("§7Left-click to visit");
                        iconLore.add("§7Right-click to edit");
                        iconLore.add("§7Mid-click to delete");
                    } else if (cfg.getBoolean("Players." + target.getUniqueId() + ".Settings.Visitors.Enabled")
                            || cfg.getStringList("Players." + target.getUniqueId() + ".Settings.Visitors.Whitelist").contains(p.getName())
                            || !cfg.getStringList("Players." + target.getUniqueId() + ".Settings.Visitors.BlackList").contains(p.getName()))
                        iconLore.add("§7Click to visit");
                    else iconLore.add("§cYou can't visit the homes of this player");

                    iconMeta.setDisplayName("§a" + home.getName());
                    iconMeta.setLore(iconLore);
                    icon.setItemMeta(iconMeta);

                    if (i < page * 45)
                        inv.addItem(icon);
                } else break;
            }
        }

        // no homes
        else {
            ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta redGlassPaneMeta = redGlassPane.getItemMeta();
            ArrayList<String> redGlassPaneLore = new ArrayList<>();

            redGlassPaneMeta.setDisplayName("§cNo homes");
            redGlassPaneLore.add("§7You can create a home with /home create <name>");
            redGlassPaneMeta.setLore(redGlassPaneLore);
            redGlassPane.setItemMeta(redGlassPaneMeta);
            inv.setItem(22, redGlassPane);
        }
        p.openInventory(inv);
    }

    public static void openIconList(Player p) {

        Inventory inv = Bukkit.createInventory(p, 6 * 9, "Choose icon");
        int page = Main.page.get(p);

        // ItemStacks
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemStack hopper = new ItemStack(Material.HOPPER);

        // ItemMetas
        ItemMeta arrowMeta = arrow.getItemMeta();
        ItemMeta barrierMeta = barrier.getItemMeta();
        ItemMeta hopperMeta = hopper.getItemMeta();

        // Lores
        ArrayList<String> arrowLore = new ArrayList<>();
        ArrayList<String> barrierLore = new ArrayList<>();
        ArrayList<String> hopperLore = new ArrayList<>();

        // next page
        if (getIcons().size() > page * 45) {
            arrowMeta.setDisplayName("§eNext page");
            arrowLore.add("§7Click to go to the next page");
            arrowMeta.setLore(arrowLore);
            arrow.setItemMeta(arrowMeta);
            inv.setItem(50, arrow);
        }
        // previous page
        if (page > 1) {
            arrowMeta.setDisplayName("§ePrevious page");
            arrowLore.clear();
            arrowLore.add("§7Click to go to the previous page");
            arrowMeta.setLore(arrowLore);
            arrow.setItemMeta(arrowMeta);
            inv.setItem(48, arrow);
        }

        // sorting
        String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type");
        String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction");
        hopperMeta.setDisplayName("§eSorting");
        hopperLore.add("§7Current: " + sorting + " | " + direction);
        hopperLore.add("§7--------------------");
        hopperLore.add("§7Left-click to change type");
        hopperLore.add("§7Right-click to change direction");
        hopperMeta.setLore(hopperLore);
        hopper.setItemMeta(hopperMeta);
        inv.setItem(46, hopper);

        if (Main.lastGui.get(p) != null) {
            // back
            barrierMeta.setDisplayName("§eBack");
            barrierLore.add("§7Click to go back");
        } else {
            // close
            barrierMeta.setDisplayName("§eClose");
            barrierLore.add("§7Click to close");
        }
        barrierMeta.setLore(barrierLore);
        barrier.setItemMeta(barrierMeta);
        inv.setItem(49, barrier);

        // icons
        ArrayList<String> icons = sortIconList(p);

        for (int i = (page - 1) * 45; i < page * 45; i++) {
            if (icons.size() > i) {
                ItemStack icon = new ItemStack(Material.getMaterial(icons.get(i)));
                ItemMeta iconMeta = icon.getItemMeta();
                ArrayList<String> iconLore = new ArrayList<>();

                iconMeta.setDisplayName("§a" + icon.getType().toString().toLowerCase().replaceAll("_", " "));
                iconLore.add("§7Click to choose");
                iconMeta.setLore(iconLore);
                icon.setItemMeta(iconMeta);

                if (i < page * 45) inv.addItem(icon);
            } else break;
        }
        p.openInventory(inv);
    }

    public static void openWhitelist(Player p) {

        Inventory inv = Bukkit.createInventory(p, 6 * 9, "Whitelist");
        int page = Main.page.get(p);
        ArrayList<String> whitelist = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist"));

        // ItemStacks
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemStack limeConcrete = new ItemStack(Material.LIME_CONCRETE);

        // ItemMetas
        ItemMeta barrierMeta = barrier.getItemMeta();
        ItemMeta limeConcreteMeta = limeConcrete.getItemMeta();

        // Lores
        ArrayList<String> barrierLore = new ArrayList<>();
        ArrayList<String> limeConcreteLore = new ArrayList<>();

        if (Main.lastGui.get(p) != null) {
            // back
            barrierMeta.setDisplayName("§eBack");
            barrierLore.add("§7Click to go back");
        } else {
            // close
            barrierMeta.setDisplayName("§eClose");
            barrierLore.add("§7Click to close");
        }
        barrierMeta.setLore(barrierLore);
        barrier.setItemMeta(barrierMeta);
        inv.setItem(49, barrier);

        // add
        limeConcreteMeta.setDisplayName("§eAdd");
        limeConcreteLore.add("§7Click to add a player to the whitelist");
        limeConcreteMeta.setLore(limeConcreteLore);
        limeConcrete.setItemMeta(limeConcreteMeta);
        inv.setItem(53, limeConcrete);

        if (!whitelist.isEmpty()) {

            // ItemStacks
            ItemStack arrow = new ItemStack(Material.ARROW);
            ItemStack hopper = new ItemStack(Material.HOPPER);

            // ItemMetas
            ItemMeta arrowMeta = arrow.getItemMeta();
            ItemMeta hopperMeta = hopper.getItemMeta();

            // Lores
            ArrayList<String> arrowLore = new ArrayList<>();
            ArrayList<String> hopperLore = new ArrayList<>();

            // next page
            if (whitelist.size() > page * 45) {
                arrowMeta.setDisplayName("§eNext page");
                arrowLore.add("§7Click to go to the next page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(50, arrow);
            }

            // previous page
            if (page > 1) {
                arrowMeta.setDisplayName("§ePrevious page");
                arrowLore.clear();
                arrowLore.add("§7Click to go to the previous page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(48, arrow);
            }

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction");
            hopperMeta.setDisplayName("§eSorting");
            hopperLore.add("§7Current: " + sorting + " | " + direction);
            hopperLore.add("§7Left-click to change type");
            hopperLore.add("§7Right-click to change direction");
            hopperMeta.setLore(hopperLore);
            hopper.setItemMeta(hopperMeta);
            inv.setItem(46, hopper);

            // players
            ArrayList<String> players = sortPlayerList(whitelist, p);

            for (int i = (page - 1) * 45; i < page * 45; i++) {
                if (players.size() > i) {

                    OfflinePlayer player;
                    String name = players.get(i);
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(name))) player = Bukkit.getPlayer(name);
                    else player = Bukkit.getOfflinePlayer(name);

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta headMeta = (SkullMeta) head.getItemMeta();
                    ArrayList<String> headLore = new ArrayList<>();

                    headMeta.setDisplayName("§a" + player.getName());
                    headMeta.setOwningPlayer(player);
                    if (Bukkit.getOnlinePlayers().contains(player))
                        headLore.add("§7State: §aOnline");
                    else headLore.add("§7State: §cOffline");
                    headLore.add("§7Click to remove");
                    headMeta.setLore(headLore);
                    head.setItemMeta(headMeta);

                    if (i < page * 45) inv.addItem(head);
                } else break;
            }
        }

        // no homes
        else {
            ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta redGlassPaneMeta = redGlassPane.getItemMeta();
            ArrayList<String> redGlassPaneLore = new ArrayList<>();

            redGlassPaneMeta.setDisplayName("§cNo players on the whitelist");
            redGlassPaneLore.add("§7You can add a player with the add-button");
            redGlassPaneMeta.setLore(redGlassPaneLore);
            redGlassPane.setItemMeta(redGlassPaneMeta);
            inv.setItem(22, redGlassPane);
        }
        p.openInventory(inv);
    }

    public static void openBlacklist(Player p) {

        Inventory inv = Bukkit.createInventory(p, 6 * 9, "Blacklist");
        int page = Main.page.get(p);
        ArrayList<String> blacklist = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist"));

        // ItemStacks
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemStack limeConcrete = new ItemStack(Material.LIME_CONCRETE);

        // ItemMetas
        ItemMeta barrierMeta = barrier.getItemMeta();
        ItemMeta limeConcreteMeta = limeConcrete.getItemMeta();

        // Lores
        ArrayList<String> barrierLore = new ArrayList<>();
        ArrayList<String> limeConcreteLore = new ArrayList<>();

        if (Main.lastGui.get(p) != null) {
            // back
            barrierMeta.setDisplayName("§eBack");
            barrierLore.add("§7Click to go back");
        } else {
            // close
            barrierMeta.setDisplayName("§eClose");
            barrierLore.add("§7Click to close");
        }
        barrierMeta.setLore(barrierLore);
        barrier.setItemMeta(barrierMeta);
        inv.setItem(49, barrier);

        // add
        limeConcreteMeta.setDisplayName("§eAdd");
        limeConcreteLore.add("§7Click to add a player to the blacklist");
        limeConcreteMeta.setLore(limeConcreteLore);
        limeConcrete.setItemMeta(limeConcreteMeta);
        inv.setItem(53, limeConcrete);

        if (!blacklist.isEmpty()) {

            // ItemStacks
            ItemStack arrow = new ItemStack(Material.ARROW);
            ItemStack hopper = new ItemStack(Material.HOPPER);

            // ItemMetas
            ItemMeta arrowMeta = arrow.getItemMeta();
            ItemMeta hopperMeta = hopper.getItemMeta();

            // Lores
            ArrayList<String> arrowLore = new ArrayList<>();
            ArrayList<String> hopperLore = new ArrayList<>();

            // next page
            if (blacklist.size() > page * 45) {
                arrowMeta.setDisplayName("§eNext page");
                arrowLore.add("§7Click to go to the next page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(50, arrow);
            }

            // previous page
            if (page > 1) {
                arrowMeta.setDisplayName("§ePrevious page");
                arrowLore.clear();
                arrowLore.add("§7Click to go to the previous page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(48, arrow);
            }

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction");
            hopperMeta.setDisplayName("§eSorting");
            hopperLore.add("§7Current: " + sorting + " | " + direction);
            hopperLore.add("§7Left-click to change type");
            hopperLore.add("§7Right-click to change direction");
            hopperMeta.setLore(hopperLore);
            hopper.setItemMeta(hopperMeta);
            inv.setItem(46, hopper);

            // players
            ArrayList<String> players = sortPlayerList(blacklist, p);

            for (int i = (page - 1) * 45; i < page * 45; i++) {
                if (players.size() > i) {

                    OfflinePlayer player;
                    String name = players.get(i);
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(name))) player = Bukkit.getPlayer(name);
                    else player = Bukkit.getOfflinePlayer(name);

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta headMeta = (SkullMeta) head.getItemMeta();
                    ArrayList<String> headLore = new ArrayList<>();

                    headMeta.setDisplayName("§a" + player.getName());
                    headMeta.setOwningPlayer(player);
                    if (Bukkit.getOnlinePlayers().contains(player))
                        headLore.add("§7State: §aOnline");
                    else headLore.add("§7State: §cOffline");
                    headLore.add("§7Click to remove");
                    headMeta.setLore(headLore);
                    head.setItemMeta(headMeta);

                    if (i < page * 45) inv.addItem(head);
                } else break;
            }
        }

        // no homes
        else {
            ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta redGlassPaneMeta = redGlassPane.getItemMeta();
            ArrayList<String> redGlassPaneLore = new ArrayList<>();

            redGlassPaneMeta.setDisplayName("§cNo players on the blacklist");
            redGlassPaneLore.add("§7You can add a player with the add-button");
            redGlassPaneMeta.setLore(redGlassPaneLore);
            redGlassPane.setItemMeta(redGlassPaneMeta);
            inv.setItem(22, redGlassPane);
        }
        p.openInventory(inv);
    }

    public static void openPlayerList(Player p) {

        Inventory inv = Bukkit.createInventory(p, 6 * 9, "Choose player");
        int page = Main.page.get(p);
        ArrayList<String> players = new ArrayList<>(getPlayers());
        ArrayList<String> trusting_players = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist"));
        players.remove(p.getName());
        players.removeAll(trusting_players);

        // ItemStacks
        ItemStack barrier = new ItemStack(Material.BARRIER);

        // ItemMetas
        ItemMeta barrierMeta = barrier.getItemMeta();

        // Lores
        ArrayList<String> barrierLore = new ArrayList<>();

        if (Main.lastGui.get(p) != null) {
            // back
            barrierMeta.setDisplayName("§eBack");
            barrierLore.add("§7Click to go back");
        } else {
            // close
            barrierMeta.setDisplayName("§eClose");
            barrierLore.add("§7Click to close");
        }
        barrierMeta.setLore(barrierLore);
        barrier.setItemMeta(barrierMeta);
        inv.setItem(49, barrier);

        if (!players.isEmpty()) {

            // ItemStacks
            ItemStack arrow = new ItemStack(Material.ARROW);

            // ItemMetas
            ItemMeta arrowMeta = arrow.getItemMeta();

            // Lores
            ArrayList<String> arrowLore = new ArrayList<>();

            // next page
            if (players.size() > page * 45) {
                arrowMeta.setDisplayName("§eNext page");
                arrowLore.add("§7Click to go to the next page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(50, arrow);
            }

            // previous page
            if (page > 1) {
                arrowMeta.setDisplayName("§ePrevious page");
                arrowLore.clear();
                arrowLore.add("§7Click to go to the previous page");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inv.setItem(48, arrow);
            }

            for (int i = (page - 1) * 45; i < page * 45; i++) {
                if (players.size() > i) {
                    Player player = Bukkit.getPlayer(players.get(i));
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta headMeta = (SkullMeta) head.getItemMeta();
                    ArrayList<String> headLore = new ArrayList<>();

                    headMeta.setDisplayName("§a" + player.getName());
                    headMeta.setOwningPlayer(p);
                    headLore.add("§7Click to choose");
                    headMeta.setLore(headLore);
                    head.setItemMeta(headMeta);

                    if (i < page * 45) inv.addItem(head);
                } else break;
            }
        }

        // no player
        else {
            ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta redGlassPaneMeta = redGlassPane.getItemMeta();
            ArrayList<String> redGlassPaneLore = new ArrayList<>();

            redGlassPaneMeta.setDisplayName("§cNo players online");
            redGlassPaneLore.add("§7Currently are no other players online");
            redGlassPaneMeta.setLore(redGlassPaneLore);
            redGlassPane.setItemMeta(redGlassPaneMeta);
            inv.setItem(22, redGlassPane);
        }
        p.openInventory(inv);
    }

    public static void openInventory(InventoryType invtype, Player p) {

        if (invtype == InventoryType.DELETE) {

            Inventory inv = Bukkit.createInventory(p, 3 * 9, "Are you sure?");

            ItemStack limeGlassPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);

            ItemMeta limeGlassPaneMeta = limeGlassPane.getItemMeta();
            ItemMeta redGlassPaneMeta = redGlassPane.getItemMeta();

            ArrayList<String> limeGlassPaneLore = new ArrayList<>();
            ArrayList<String> redGlassPaneLore = new ArrayList<>();

            limeGlassPaneMeta.setDisplayName("§aYes");
            limeGlassPaneLore.add("§7Click to confirm");
            limeGlassPaneMeta.setLore(limeGlassPaneLore);
            limeGlassPane.setItemMeta(limeGlassPaneMeta);
            inv.setItem(12, limeGlassPane);

            redGlassPaneMeta.setDisplayName("§cNo");
            redGlassPaneLore.add("§7Click to cancel");
            redGlassPaneMeta.setLore(redGlassPaneLore);
            redGlassPane.setItemMeta(redGlassPaneMeta);
            inv.setItem(14, redGlassPane);

            p.openInventory(inv);
        }

        // settings
        else if (invtype == InventoryType.SETTINGS) {

            Inventory inv = Bukkit.createInventory(p, 3 * 9, "Settings");

            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemStack redDye = new ItemStack(Material.RED_DYE);
            ItemStack limeDye = new ItemStack(Material.LIME_DYE);

            ItemMeta barrierMeta = barrier.getItemMeta();
            ItemMeta redDyeMeta = redDye.getItemMeta();
            ItemMeta limeDyeMeta = limeDye.getItemMeta();

            ArrayList<String> barrierLore = new ArrayList<>();
            ArrayList<String> redDyeLore = new ArrayList<>();
            ArrayList<String> limeDyeLore = new ArrayList<>();

            // close
            if (Main.lastGui.get(p) != null) {
                barrierMeta.setDisplayName("§eBack");
                barrierLore.add("§7Click to go back");
            } else {
                barrierMeta.setDisplayName("§eClose");
                barrierLore.add("§7Click to close");
            }
            barrierMeta.setLore(barrierLore);
            barrier.setItemMeta(barrierMeta);
            inv.setItem(22, barrier);

            // information
            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.ShowInformation")) {
                limeDyeMeta.setDisplayName("§eShow information");
                limeDyeLore.add("§7Show information about the home in \"Your homes\"");
                limeDyeLore.add("§7State: §aOn");
                limeDyeLore.add("§7--------------------");
                limeDyeLore.add("§7Click to disable");
                limeDyeMeta.setLore(limeDyeLore);
                limeDye.setItemMeta(limeDyeMeta);
                inv.setItem(10, limeDye);
            } else {
                redDyeMeta.setDisplayName("§eShow information");
                redDyeLore.add("§7Show information about the home in \"Your homes\"");
                redDyeLore.add("§7State: §cOff");
                redDyeLore.add("§7--------------------");
                redDyeLore.add("§7Click to enable");
                redDyeMeta.setLore(redDyeLore);
                redDye.setItemMeta(redDyeMeta);
                inv.setItem(10, redDye);
            }

            // delete protection
            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                limeDyeLore.clear();
                limeDyeMeta.setDisplayName("§eDelete protection");
                limeDyeLore.add("§7Open inventory to confirm delete");
                limeDyeLore.add("§7State: §aOn");
                limeDyeLore.add("§7--------------------");
                limeDyeLore.add("§7Click to disable");
                limeDyeMeta.setLore(limeDyeLore);
                limeDye.setItemMeta(limeDyeMeta);
                inv.setItem(11, limeDye);
            } else {
                redDyeLore.clear();
                redDyeMeta.setDisplayName("§eDelete protection");
                redDyeLore.add("§7Open inventory to corfirm delete");
                redDyeLore.add("§7State: §cOff");
                redDyeLore.add("§7--------------------");
                redDyeLore.add("§7Click to enable");
                redDyeMeta.setLore(redDyeLore);
                redDye.setItemMeta(redDyeMeta);
                inv.setItem(11, redDye);
            }

            // orientation
            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Orientation")) {
                limeDyeLore.clear();
                limeDyeMeta.setDisplayName("§eOrientation");
                limeDyeLore.add("§7Use yaw and pitch for homes");
                limeDyeLore.add("§7State: §aOn");
                limeDyeLore.add("§7--------------------");
                limeDyeLore.add("§7Click to disable");
                limeDyeMeta.setLore(limeDyeLore);
                limeDye.setItemMeta(limeDyeMeta);
                inv.setItem(12, limeDye);
            } else {
                redDyeLore.clear();
                redDyeMeta.setDisplayName("§eOrientation");
                redDyeLore.add("§7Use yaw and pitch for homes");
                redDyeLore.add("§7State: §cOff");
                redDyeLore.add("§7--------------------");
                redDyeLore.add("§7Click to enable");
                redDyeMeta.setLore(redDyeLore);
                redDye.setItemMeta(redDyeMeta);
                inv.setItem(12, redDye);
            }

            // visitors
            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled")) {
                limeDyeLore.clear();
                limeDyeMeta.setDisplayName("§eVisitors");
                limeDyeLore.add("§7Player can visit your homes");
                limeDyeLore.add("§7State: §aOn");
                limeDyeLore.add("§7--------------------");
                limeDyeLore.add("§7Left-click to disable");
                limeDyeLore.add("§7Right-click to manage blacklist");
                limeDyeMeta.setLore(limeDyeLore);
                limeDye.setItemMeta(limeDyeMeta);
                inv.setItem(13, limeDye);
            } else {
                redDyeLore.clear();
                redDyeMeta.setDisplayName("§eVisitors");
                redDyeLore.add("§7Players can visit your homes");
                redDyeLore.add("§7State: §cOff");
                redDyeLore.add("§7--------------------");
                redDyeLore.add("§7Left-click to enable");
                redDyeLore.add("§7Right-click to manage whitelist");
                redDyeMeta.setLore(redDyeLore);
                redDye.setItemMeta(redDyeMeta);
                inv.setItem(13, redDye);
            }
            p.openInventory(inv);
        }

        // home
        else if (invtype == InventoryType.HOME) {

            Home home = Main.currentHome.get(p);
            String name = home.getName();

            Inventory inv = Bukkit.createInventory(p, 9 * 3, name);

            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemStack nametag = new ItemStack(Material.NAME_TAG);
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemStack icon = home.getIcon();
            ItemStack redConcrete = new ItemStack(Material.RED_CONCRETE);

            ItemMeta barrierMeta = barrier.getItemMeta();
            ItemMeta nametagMeta = nametag.getItemMeta();
            ItemMeta compassMeta = compass.getItemMeta();
            ItemMeta iconMeta = icon.getItemMeta();
            ItemMeta redConcreteMeta = redConcrete.getItemMeta();

            ArrayList<String> barrierLore = new ArrayList<>();
            ArrayList<String> nametagLore = new ArrayList<>();
            ArrayList<String> compassLore = new ArrayList<>();
            ArrayList<String> iconLore = new ArrayList<>();
            ArrayList<String> redConcreteLore = new ArrayList<>();

            // back
            barrierMeta.setDisplayName("§eBack");
            barrierLore.add("§7Click to go back");
            barrierMeta.setLore(barrierLore);
            barrier.setItemMeta(barrierMeta);
            inv.setItem(22, barrier);

            // name
            nametagMeta.setDisplayName("§eName");
            nametagLore.add("§7" + name);
            nametagLore.add("§7--------------------");
            nametagLore.add("§7Click to change");
            nametagLore.add("§6§lComming Soon");
            nametagMeta.setLore(nametagLore);
            nametag.setItemMeta(nametagMeta);
            inv.setItem(10, nametag);

            // location
            double x = (double) Math.round(home.getLocation().getX() * 100) / 100;
            double y = (double) Math.round(home.getLocation().getY() * 100) / 100;
            double z = (double) Math.round(home.getLocation().getZ() * 100) / 100;
            String w = home.getLocation().getWorld().getName();
            compassMeta.setDisplayName("§eLocation");
            compassLore.add("§7X: " + x);
            compassLore.add("§7Y: " + y);
            compassLore.add("§7Z: " + z);
            compassLore.add("§7World: " + w);
            compassLore.add("§7--------------------");
            compassLore.add("§7Click to change");
            compassLore.add("§6§lComming Soon");
            compassMeta.setLore(compassLore);
            compass.setItemMeta(compassMeta);
            inv.setItem(11, compass);

            // icon
            iconMeta.setDisplayName("§eIcon");
            iconLore.add("§7" + icon.getType().toString().toLowerCase().replaceAll("_", " "));
            iconLore.add("§7--------------------");
            iconLore.add("§7Click to change");
            iconMeta.setLore(iconLore);
            icon.setItemMeta(iconMeta);
            inv.setItem(12, icon);

            // delete
            redConcreteMeta.setDisplayName("§cDelete");
            redConcreteLore.add("§7Click to delete");
            redConcreteMeta.setLore(redConcreteLore);
            redConcrete.setItemMeta(redConcreteMeta);
            inv.setItem(13, redConcrete);

            p.openInventory(inv);
        }
    }

    private static ArrayList<String> getHomes(Player owner) {

        if (cfg.contains("Players." + owner.getUniqueId() + ".Homes")) {
            ArrayList<String> homes = new ArrayList<>();
            for (String id : cfg.getConfigurationSection("Players." + owner.getUniqueId() + ".Homes").getKeys(false)) {
                String name = cfg.getString("Players." + owner.getUniqueId() + ".Homes." + id + ".Name");
                homes.add(name);
            }
            return homes;
        } else
            return null;
    }

    private static ArrayList<String> getPlayers() {

        ArrayList<String> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String name = p.getName();
            players.add(name);
        }
        return players;
    }

    private static ArrayList<String> getIcons() {
        ArrayList<Material> icons = new ArrayList<>();
        Collections.addAll(icons, Material.values());

        // wall torches
        icons.remove(Material.WALL_TORCH);
        icons.remove(Material.REDSTONE_WALL_TORCH);
        icons.remove(Material.SOUL_WALL_TORCH);

        // wall signs
        icons.remove(Material.ACACIA_WALL_SIGN);
        icons.remove(Material.BIRCH_WALL_SIGN);
        icons.remove(Material.WARPED_WALL_SIGN);
        icons.remove(Material.CRIMSON_WALL_SIGN);
        icons.remove(Material.DARK_OAK_WALL_SIGN);
        icons.remove(Material.JUNGLE_WALL_SIGN);
        icons.remove(Material.OAK_WALL_SIGN);
        icons.remove(Material.SPRUCE_WALL_SIGN);

        // wall heads / skulls
        icons.remove(Material.CREEPER_WALL_HEAD);
        icons.remove(Material.DRAGON_WALL_HEAD);
        icons.remove(Material.PLAYER_WALL_HEAD);
        icons.remove(Material.SKELETON_WALL_SKULL);
        icons.remove(Material.WITHER_SKELETON_WALL_SKULL);
        icons.remove(Material.ZOMBIE_WALL_HEAD);

        // wall banners
        icons.remove(Material.BLACK_WALL_BANNER);
        icons.remove(Material.BLUE_WALL_BANNER);
        icons.remove(Material.BROWN_WALL_BANNER);
        icons.remove(Material.CYAN_WALL_BANNER);
        icons.remove(Material.GRAY_WALL_BANNER);
        icons.remove(Material.GREEN_WALL_BANNER);
        icons.remove(Material.LIGHT_BLUE_WALL_BANNER);
        icons.remove(Material.LIGHT_GRAY_WALL_BANNER);
        icons.remove(Material.LIME_WALL_BANNER);
        icons.remove(Material.MAGENTA_WALL_BANNER);
        icons.remove(Material.ORANGE_WALL_BANNER);
        icons.remove(Material.PINK_WALL_BANNER);
        icons.remove(Material.PURPLE_WALL_BANNER);
        icons.remove(Material.RED_WALL_BANNER);
        icons.remove(Material.WHITE_WALL_BANNER);
        icons.remove(Material.YELLOW_WALL_BANNER);

        // stems
        icons.remove(Material.MELON_STEM);
        icons.remove(Material.PUMPKIN_STEM);
        icons.remove(Material.ATTACHED_MELON_STEM);
        icons.remove(Material.ATTACHED_PUMPKIN_STEM);

        // portals
        icons.remove(Material.NETHER_PORTAL);
        icons.remove(Material.END_PORTAL);
        icons.remove(Material.END_GATEWAY);

        // potted
        icons.remove(Material.POTTED_ACACIA_SAPLING);
        icons.remove(Material.POTTED_ALLIUM);
        icons.remove(Material.POTTED_AZURE_BLUET);
        icons.remove(Material.POTTED_BAMBOO);
        icons.remove(Material.POTTED_BIRCH_SAPLING);
        icons.remove(Material.POTTED_BLUE_ORCHID);
        icons.remove(Material.POTTED_BROWN_MUSHROOM);
        icons.remove(Material.POTTED_CACTUS);
        icons.remove(Material.POTTED_CORNFLOWER);
        icons.remove(Material.POTTED_CRIMSON_FUNGUS);
        icons.remove(Material.POTTED_CRIMSON_ROOTS);
        icons.remove(Material.POTTED_DANDELION);
        icons.remove(Material.POTTED_DARK_OAK_SAPLING);
        icons.remove(Material.POTTED_DEAD_BUSH);
        icons.remove(Material.POTTED_FERN);
        icons.remove(Material.POTTED_JUNGLE_SAPLING);
        icons.remove(Material.POTTED_LILY_OF_THE_VALLEY);
        icons.remove(Material.POTTED_OAK_SAPLING);
        icons.remove(Material.POTTED_ORANGE_TULIP);
        icons.remove(Material.POTTED_OXEYE_DAISY);
        icons.remove(Material.POTTED_PINK_TULIP);
        icons.remove(Material.POTTED_POPPY);
        icons.remove(Material.POTTED_RED_MUSHROOM);
        icons.remove(Material.POTTED_RED_TULIP);
        icons.remove(Material.POTTED_SPRUCE_SAPLING);
        icons.remove(Material.POTTED_WARPED_FUNGUS);
        icons.remove(Material.POTTED_WARPED_ROOTS);
        icons.remove(Material.POTTED_WHITE_TULIP);
        icons.remove(Material.POTTED_WITHER_ROSE);

        // wall fans
        icons.remove(Material.BRAIN_CORAL_WALL_FAN);
        icons.remove(Material.BUBBLE_CORAL_WALL_FAN);
        icons.remove(Material.DEAD_BRAIN_CORAL_WALL_FAN);
        icons.remove(Material.DEAD_BUBBLE_CORAL_WALL_FAN);
        icons.remove(Material.DEAD_FIRE_CORAL_WALL_FAN);
        icons.remove(Material.DEAD_HORN_CORAL_WALL_FAN);
        icons.remove(Material.DEAD_TUBE_CORAL_WALL_FAN);
        icons.remove(Material.FIRE_CORAL_WALL_FAN);
        icons.remove(Material.HORN_CORAL_WALL_FAN);
        icons.remove(Material.TUBE_CORAL_WALL_FAN);

        // plants
        icons.remove(Material.BAMBOO_SAPLING);
        icons.remove(Material.BEETROOTS);
        icons.remove(Material.CARROTS);
        icons.remove(Material.COCOA);
        icons.remove(Material.KELP_PLANT);
        icons.remove(Material.POTATOES);
        icons.remove(Material.SWEET_BERRY_BUSH);
        icons.remove(Material.TALL_SEAGRASS);
        icons.remove(Material.TWISTING_VINES_PLANT);
        icons.remove(Material.WEEPING_VINES_PLANT);

        // others
        icons.remove(Material.AIR);
        icons.remove(Material.BUBBLE_COLUMN);
        icons.remove(Material.CAVE_AIR);
        icons.remove(Material.FIRE);
        icons.remove(Material.FROSTED_ICE);
        icons.remove(Material.LAVA);
        icons.remove(Material.MOVING_PISTON);
        icons.remove(Material.PISTON_HEAD);
        icons.remove(Material.REDSTONE_WIRE);
        icons.remove(Material.SOUL_FIRE);
        icons.remove(Material.TRIPWIRE);
        icons.remove(Material.VOID_AIR);
        icons.remove(Material.WATER);

        ArrayList<String> iconsName = new ArrayList<>();
        for (Material icon : icons) iconsName.add(icon.toString());

        return iconsName;
    }

    private static ArrayList<String> sortHomeList(ArrayList<String> unsortedList, Player p) {
        switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type")) {
            case "date":
                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
                break;
            case "name":
                Collections.sort(unsortedList);
                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
                break;
        }
        return unsortedList;
    }

    private static ArrayList<String> sortIconList(Player p) {
        ArrayList<String> unsortedList = getIcons();
        switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type")) {
            case "default":
                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
                break;
            case "name":
                Collections.sort(unsortedList);
                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
                break;
        }
        return unsortedList;
    }

    private static ArrayList<String> sortPlayerList(ArrayList<String> unsortedList, Player p) {
        switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
            case "state":

                ArrayList<String> sortedList = new ArrayList<>();
                for (String player : unsortedList) {
                    if (Bukkit.getOnlinePlayers().contains(player)) {
                        sortedList.add(player);
                    }
                }
                unsortedList.removeAll(sortedList);
                sortedList.addAll(unsortedList);
                unsortedList = sortedList;

                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
                break;
            case "name":
                Collections.sort(unsortedList);
                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
                break;
        }
        return unsortedList;
    }
}
