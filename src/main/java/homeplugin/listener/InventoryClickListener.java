package homeplugin.listener;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import homeplugin.main.Main;
import homeplugin.others.Home;
import homeplugin.others.Inventories;
import homeplugin.others.Inventories.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class InventoryClickListener implements Listener {

    FileConfiguration cfg = Main.getPlugin().getConfig();

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        try {
            if (e.getInventory().getHolder() == p) {
                if (e.getCurrentItem() != null) {

                    // close
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eClose")) {
                        p.closeInventory();
                    }

                    // settings
                    else if (e.getView().getTitle().equals("Settings")) {

                        // back
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eBack")) {
                            Inventories.openHomeList(p, p);
                        }

                        // show information
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eShow information")) {

                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.ShowInformation"))
                                cfg.set("Players." + p.getUniqueId() + ".Settings.ShowInformation", false);
                            else cfg.set("Players." + p.getUniqueId() + ".Settings.ShowInformation", true);
                            Main.getPlugin().saveConfig();
                            Inventories.openInventory(InventoryType.SETTINGS, p);
                        }

                        // delete protection
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eDelete protection")) {

                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection"))
                                cfg.set("Players." + p.getUniqueId() + ".Settings.DeleteProtection", false);
                            else cfg.set("Players." + p.getUniqueId() + ".Settings.DeleteProtection", true);
                            Main.getPlugin().saveConfig();
                            Inventories.openInventory(InventoryType.SETTINGS, p);
                        }

                        // orientation
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eOrientation")) {

                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Orientation"))
                                cfg.set("Players." + p.getUniqueId() + ".Settings.Orientation", false);
                            else cfg.set("Players." + p.getUniqueId() + ".Settings.Orientation", true);
                            Main.getPlugin().saveConfig();
                            Inventories.openInventory(InventoryType.SETTINGS, p);
                        }

                        // visitors
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eVisitors")) {

                            // left
                            if (e.getClick() == ClickType.LEFT) {

                                if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled"))
                                    cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled", false);
                                else cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled", true);
                                Main.getPlugin().saveConfig();
                                Inventories.openInventory(InventoryType.SETTINGS, p);
                            }

                            // right
                            else if (e.getClick() == ClickType.RIGHT) {
                                Main.page.put(p, 1);
                                Main.lastGui.put(p, e.getView().getTitle());
                                if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled"))
                                    Inventories.openBlacklist(p);
                                else Inventories.openWhitelist(p);
                            }

                            // others
                            else e.setCancelled(true);
                        }
                    }

                    // delete
                    else if (e.getView().getTitle().equals("Are you sure?")) {

                        // whitelist
                        if (Main.lastGui.get(p).equals("Whitelist")) {

                            // no
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo"))
                                Inventories.openWhitelist(p);

                        }

                        // blacklist
                        else if (Main.lastGui.get(p).equals("Blacklist")) {

                            // no
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo"))
                                Inventories.openWhitelist(p);

                        }

                        // yes
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aYes")) {
                            Home home = Main.currentHome.get(p);
                            home.delete();

                            if (Main.lastGui.get(p) == null) {
                                p.closeInventory();
                                p.sendMessage(Main.prefix + "§aYou successfully deleted the home §6" + home.getName());
                            } else if (Main.lastGui.get(p).equals("Homes of " + p.getName()) || Main.lastGui.get(p).equals(Main.currentHome.get(p).getName())) {
                                Inventories.openHomeList(p, p);
                            }
                        }

                        // no
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo")) {

                            if (Main.lastGui.get(p) == null)
                                p.closeInventory();
                            else if (Main.lastGui.get(p).equals("Homes of " + p.getName()))
                                Inventories.openHomeList(p, p);
                            else if (Main.lastGui.get(p).equals(Main.currentHome.get(p).getName()))
                                Inventories.openInventory(InventoryType.HOME, p);
                        }
                    }

                    // icon
                    else if (e.getView().getTitle().equals("Choose icon")) {

                        // next page
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eNext page")) {
                            Main.page.put(p, Main.page.get(p) + 1);
                            Inventories.openIconList(p);
                        }

                        // previous page
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§ePrevious page")) {
                            Main.page.put(p, Main.page.get(p) - 1);
                            Inventories.openIconList(p);
                        }

                        // back
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eBack")) {
                            Main.page.put(p, 1);
                            if (Main.lastGui.get(p).equals("Homes of " + p.getName())) Inventories.openHomeList(p, p);
                            else Inventories.openInventory(InventoryType.HOME, p);
                        }

                        // sorting
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSorting")) {

                            // type
                            if (e.getClick() == ClickType.LEFT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type")) {
                                    case "default" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type", "name");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openIconList(p);
                                    }
                                    case "name" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type", "default");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openIconList(p);
                                    }
                                }
                            }

                            // direction
                            else if (e.getClick() == ClickType.RIGHT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction")) {
                                    case "rising" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction", "falling");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openIconList(p);
                                    }
                                    case "falling" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction", "rising");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openIconList(p);
                                    }
                                }
                            } else e.setCancelled(true);
                        }

                        // icon
                        else {
                            Home home = Main.currentHome.get(p);
                            home.setIcon(e.getCurrentItem());

                            // close inventory
                            if (Main.lastGui.get(p) == null) {
                                p.closeInventory();
                                p.sendMessage(Main.prefix + "§aYou successfully changed the icon to §6"
                                        + home.getIcon().getType().toString().toLowerCase().replaceAll("_", " "));
                            }

                            // home settings
                            else if (Main.lastGui.get(p).equals(home.getName())) {
                                Inventories.openInventory(InventoryType.HOME, p);
                            }

                            // home list
                            else if (Main.lastGui.get(p).equals("Homes of " + p.getName())) {
                                Inventories.openHomeList(p, p);
                            }
                        }

                    }

                    // whitelist
                    else if (e.getView().getTitle().equals("Whitelist")) {

                        // back
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eBack")) {
                            Main.page.put(p, 1);
                            Inventories.openInventory(InventoryType.SETTINGS, p);
                        }

                        // add
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eAdd")) {
                            Main.page.put(p, 1);
                            Main.lastGui.put(p, e.getView().getTitle());
                            Inventories.openPlayerList(p);
                        }

                        // sorting
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSorting")) {

                            // type
                            if (e.getClick() == ClickType.LEFT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
                                    case "state" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "name");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openWhitelist(p);
                                    }
                                    case "name" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "state");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openWhitelist(p);
                                    }
                                }
                            }

                            // direction
                            else if (e.getClick() == ClickType.RIGHT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction")) {
                                    case "rising" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "falling");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openWhitelist(p);
                                    }
                                    case "falling" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "rising");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openWhitelist(p);
                                    }
                                }
                            } else e.setCancelled(true);
                        }

                        // no players
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo players on the whitelist")) {
                            e.setCancelled(true);
                        }

                        // remove
                        else {
                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                                Main.lastGui.put(p, e.getView().getTitle());
                                Inventories.openInventory(InventoryType.DELETE, p);
                            } else {
                                ArrayList<String> whitelist = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist"));
                                whitelist.remove(e.getCurrentItem().getItemMeta().getDisplayName().replace("§a", ""));
                                cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist", whitelist);
                                Main.getPlugin().saveConfig();
                                Inventories.openWhitelist(p);
                            }
                        }
                    }

                    // blacklist
                    else if (e.getView().getTitle().equals("Blacklist")) {

                        // back
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eBack")) {
                            Main.page.put(p, 1);
                            Inventories.openInventory(InventoryType.SETTINGS, p);
                        }

                        // add
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eAdd")) {
                            Main.page.put(p, 1);
                            Main.lastGui.put(p, e.getView().getTitle());
                            Inventories.openPlayerList(p);
                        }

                        // sorting
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSorting")) {

                            // type
                            if (e.getClick() == ClickType.LEFT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
                                    case "state" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "name");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openBlacklist(p);
                                    }
                                    case "name" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "state");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openBlacklist(p);
                                    }
                                }
                            }

                            // direction
                            else if (e.getClick() == ClickType.RIGHT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction")) {
                                    case "rising" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "falling");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openBlacklist(p);
                                    }
                                    case "falling" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "rising");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openBlacklist(p);
                                    }
                                }
                            } else e.setCancelled(true);
                        }

                        // no players
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo players on the blacklist")) {
                            e.setCancelled(true);
                        }

                        // remove
                        else {
                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                                Main.lastGui.put(p, e.getView().getTitle());
                                Inventories.openInventory(InventoryType.DELETE, p);
                            } else {
                                ArrayList<String> blacklist = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist"));
                                blacklist.remove(e.getCurrentItem().getItemMeta().getDisplayName().replace("§a", ""));
                                cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist", blacklist);
                                Main.getPlugin().saveConfig();
                                Inventories.openBlacklist(p);
                            }
                        }
                    }

                    // choose player
                    else if (e.getView().getTitle().equals("Choose player")) {

                        // back
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eBack")) {
                            Main.page.put(p, 1);
                            if (Main.lastGui.get(p).equals("Whitelist")) Inventories.openWhitelist(p);
                            else if (Main.lastGui.get(p).equals("Blacklist")) Inventories.openBlacklist(p);
                        }

                        // no players
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo players online")) {
                            e.setCancelled(true);
                        }

                        // player
                        else {
                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§a", "");

                            // blacklist
                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled")) {

                                ArrayList<String> blacklist = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist"));
                                blacklist.add(name);
                                cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist", blacklist);
                                Main.getPlugin().saveConfig();
                                if (Main.lastGui.get(p) == null) {
                                    p.closeInventory();
                                    p.sendMessage(Main.prefix + "§aYou successfully added §6" + name);
                                } else if (Main.lastGui.get(p).equals("Blacklist")) {
                                    Inventories.openBlacklist(p);
                                }

                            }

                            // whitelist
                            else {
                                ArrayList<String> whitelist = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist"));
                                whitelist.add(name);
                                cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist", whitelist);
                                Main.getPlugin().saveConfig();
                                if (Main.lastGui.get(p) == null) {
                                    p.closeInventory();
                                    p.sendMessage(Main.prefix + "§aYou successfully added §6" + name);
                                } else if (Main.lastGui.get(p).equals("Whitelist")) {
                                    Inventories.openWhitelist(p);
                                }
                            }
                        }

                    } else {

                        // back
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eBack")) {
                            Inventories.openHomeList(p, p);
                        }

                        // delete
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cDelete")) {

                            String name = e.getView().getTitle();
                            Home home = new Home(name, p);

                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                                Main.currentHome.put(p, home);
                                Main.lastGui.put(p, e.getView().getTitle());
                                Inventories.openInventory(InventoryType.DELETE, p);
                            } else {
                                home.delete();
                                Inventories.openHomeList(p, p);
                            }
                        }

                        // create
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eCreate home")) {

                            AnvilGUI.Builder builder = new AnvilGUI.Builder();
                            builder.plugin(Main.getPlugin());

                            ItemStack icon = findIcon(p);
                            ItemMeta iconMeta = icon.getItemMeta();
                            iconMeta.setDisplayName("§aNew Home");
                            icon.setItemMeta(iconMeta);

                            builder.onComplete((player, text) -> {
                                Home home = new Home(text, player);
                                home.create();
                                Main.currentHome.put(player, home);
                                Main.lastGui.put(player, "Homes of " + player.getName());
                                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openIconList(p), 2);
                                return AnvilGUI.Response.close();
                            });
                            builder.onClose(player -> Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openHomeList(p, p), 1));
                            builder.onLeftInputClick(player -> Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openHomeList(p, p), 1));

                            builder.title("Enter name");

                            builder.itemLeft(icon);
                            builder.text(icon.getItemMeta().getDisplayName());

                            builder.open(p);
                        }

                        // name
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eName")) {

                            Home home = Main.currentHome.get(p);

                            ItemStack icon = home.getIcon();
                            ItemMeta iconMeta = icon.getItemMeta();
                            iconMeta.setDisplayName("§a" + home.getName());
                            iconMeta.setLore(new ArrayList<>());
                            icon.setItemMeta(iconMeta);

                            AnvilGUI.Builder builder = new AnvilGUI.Builder();
                            builder.plugin(Main.getPlugin());

                            builder.onComplete((player, text) -> {
                                home.setName(text);
                                return AnvilGUI.Response.close();
                            });
                            builder.onClose(player -> Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openInventory(InventoryType.HOME, p), 1));
                            builder.onLeftInputClick(player -> Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openInventory(InventoryType.HOME, p), 1));

                            builder.title("Rename");

                            builder.itemLeft(icon);
                            builder.text(home.getName());

                            builder.open(p);
                        }

                        // icon
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eIcon")) {
                            Main.page.put(p, 1);
                            Inventories.openIconList(p);
                        }

                        // next page
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eNext page")) {
                            Main.page.put(p, Main.page.get(p) + 1);
                            Inventories.openHomeList(p, p);
                        }

                        // previous page
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§ePrevious page")) {
                            Main.page.put(p, Main.page.get(p) - 1);
                            Inventories.openHomeList(p, p);
                        }

                        // settings
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSettings")) {
                            Main.lastGui.put(p, e.getView().getTitle());
                            Inventories.openInventory(InventoryType.SETTINGS, p);
                        }

                        // sorting
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSorting")) {

                            // type
                            if (e.getClick() == ClickType.LEFT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type")) {
                                    case "date" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "name");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openHomeList(p, p);
                                    }
                                    case "name" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "favorite");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openHomeList(p, p);
                                    }
                                    case "favorite" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "date");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openHomeList(p, p);
                                    }
                                }
                            }

                            // direction
                            else if (e.getClick() == ClickType.RIGHT) {
                                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction")) {
                                    case "rising" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction", "falling");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openHomeList(p, p);
                                    }
                                    case "falling" -> {
                                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction", "rising");
                                        Main.getPlugin().saveConfig();
                                        Inventories.openHomeList(p, p);
                                    }
                                }
                            } else e.setCancelled(true);
                        }

                        // search
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSearch")) {

                            // search
                            if (Main.search.get(p).isEmpty() || e.getClick() == ClickType.LEFT) {

                                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                                builder.plugin(Main.getPlugin());

                                ItemStack paper = new ItemStack(Material.PAPER);
                                ItemMeta paperMeta = paper.getItemMeta();
                                paperMeta.setDisplayName("§akeyword");
                                if (!Main.search.get(p).isEmpty()) paperMeta.setDisplayName("§a" + Main.search.get(p));
                                paper.setItemMeta(paperMeta);

                                builder.onComplete((player, text) -> {
                                    ArrayList<String> keywords = Main.search.get(p);
                                    if (!keywords.contains(text)) {
                                        keywords.add(text);
                                        Main.search.put(player, keywords);
                                        return AnvilGUI.Response.close();
                                    }
                                    return null;
                                });
                                builder.onClose(player -> Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openHomeList(p, p), 1));
                                builder.onLeftInputClick(player -> Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openHomeList(p, p), 1));

                                builder.itemLeft(paper);
                                builder.text("keyword");
                                if (!Main.search.get(p).isEmpty()) builder.text(Main.search.get(p).get(0));

                                builder.title("Enter keyword");

                                builder.open(p);
                            }

                            // remove search
                            else if (!Main.search.get(p).isEmpty() && e.getClick() == ClickType.RIGHT) {
                                Main.search.put(p, new ArrayList<>());
                                Inventories.openHomeList(p, p);
                            }
                        }

                        // no homes
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo homes")) {

                            e.setCancelled(true);
                        }

                        // left-click
                        else if (e.getClick() == ClickType.LEFT) {

                            String visit_success = cfg.getString("Settings.ChatMessages.Visit.Success");

                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replace("§a", "").replace(" §e⭐", "");
                            Home home = new Home(name, p);

                            if (home.isExisting()) {
                                home.visit(p);
                                p.sendMessage(Main.prefix + visit_success.replaceAll("%name%", name));
                            }
                        }

                        // right-click
                        else if (e.getClick() == ClickType.RIGHT) {

                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replace("§a", "").replace(" §e⭐", "");
                            Home home = new Home(name, p);
                            Main.lastGui.put(p, e.getCurrentItem().getItemMeta().getDisplayName().replace("§a", "").replace(" §e⭐", ""));
                            Main.currentHome.put(p, home);
                            Inventories.openInventory(InventoryType.HOME, p);
                        }

                        // middle-click
                        else if (e.getClick() == ClickType.MIDDLE) {

                            ArrayList<String> favoriteHomes = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".FavoriteHomes"));
                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replace("§a", "").replace(" §e⭐", "");
                            Home home = new Home(name, p);

                            if (!favoriteHomes.contains(home.getName())) favoriteHomes.add(home.getName());
                            else favoriteHomes.remove(home.getName());
                            cfg.set("Players." + p.getUniqueId() + ".FavoriteHomes", favoriteHomes);
                            Main.getPlugin().saveConfig();

                            Player target = (Player) e.getInventory().getHolder();
                            Inventories.openHomeList(p, target);
                        }
                    }
                }
            } else {

                if (e.getCurrentItem() != null) {

                    Player target = (Player) e.getInventory().getHolder();

                    // next page
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eNext page")) {
                        Main.page.put(p, Main.page.get(p) + 1);
                        Inventories.openHomeList(p, p);
                    }

                    // previous page
                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§ePrevious page")) {
                        Main.page.put(p, Main.page.get(p) + 1);
                        Inventories.openHomeList(p, p);
                    }

                    // settings
                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSettings")) {

                        Inventories.openInventory(InventoryType.SETTINGS, p);
                    }

                    // no homes
                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo homes")) {
                        e.setCancelled(true);
                    }

                    // left-click
                    else {
                        if (cfg.getBoolean("Players." + target.getUniqueId() + ".Settings.Visitors.Enabled")
                                || cfg.getStringList("Players." + target.getUniqueId() + ".Settings.Visitors.Whitelist").contains(p.getName())
                                || !cfg.getStringList("Players." + target.getUniqueId() + ".Settings.Visitors.Blacklist").contains(p.getName())) {
                            String visit_success = cfg.getString("Settings.ChatMessages.Visit.Success");

                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§a", "");
                            Home home = new Home(name, target);

                            if (home.isExisting()) {
                                home.visit(p);
                                p.sendMessage(Main.prefix + visit_success.replaceAll("%name%", name));
                            }
                        } else e.setCancelled(true);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public ItemStack findIcon(Player p) {
        ItemStack icon = new ItemStack(Material.GRASS_BLOCK);

        // normal
        if (p.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            icon = new ItemStack(Material.GRASS_BLOCK);
        }

        // nether
        else if (p.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            icon = new ItemStack(Material.NETHERRACK);
        }

        // the_end
        else if (p.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            icon = new ItemStack(Material.END_STONE);
        }
        return icon;
    }
}
