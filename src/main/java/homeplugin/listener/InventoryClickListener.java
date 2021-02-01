package homeplugin.listener;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
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
                            Inventories.openInventory(InventoryType.HOME, p);
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
                            if (Main.lastGui.get(p) == null) {
                                p.closeInventory();
                                p.sendMessage(Main.prefix + "§aYou successfully changed the icon to §6"
                                        + home.getIcon().getType().toString().toLowerCase().replaceAll("_", " "));
                            } else if (Main.lastGui.get(p).equals(home.getName())) {
                                Inventories.openInventory(InventoryType.HOME, p);
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

                        // name
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eName")) {

                            AnvilGUI.Builder builder = new AnvilGUI.Builder();

                            ItemStack paper = new ItemStack(Material.PAPER);
                            ItemMeta paperMeta = paper.getItemMeta();
                            paperMeta.setDisplayName("§aRename me!");
                            paper.setItemMeta(paperMeta);

                            builder.itemLeft(paper);
                            builder.onLeftInputClick(player -> e.setCancelled(true));

                            builder.title("Rename");
                            builder.text("");
                            builder.open(p);
                            builder.preventClose();

                            builder.onComplete((player, text) -> {
                                Home home = Main.currentHome.get(player);
                                home.setName(text);
                                return AnvilGUI.Response.close();
                            });
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

                        // no homes
                        else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cNo homes")) {

                            e.setCancelled(true);
                        }

                        // left-click
                        else if (e.getClick() == ClickType.LEFT) {

                            String visit_success = cfg.getString("Settings.ChatMessages.Visit.Success");

                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§a", "");
                            Home home = new Home(name, p);

                            if (home.isExisting()) {
                                home.visit(p);
                                p.sendMessage(Main.prefix + visit_success.replaceAll("%name%", name));
                            }
                        }

                        // right-click
                        else if (e.getClick() == ClickType.RIGHT) {

                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§a", "");
                            Home home = new Home(name, p);
                            Main.lastGui.put(p, e.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§a", ""));
                            Main.currentHome.put(p, home);
                            Inventories.openInventory(InventoryType.HOME, p);
                        }

                        // middle-click
                        else if (e.getClick() == ClickType.MIDDLE) {

                            String name = e.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§a", "");
                            Home home = new Home(name, p);

                            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                                Main.lastGui.put(p, e.getView().getTitle());
                                Main.currentHome.put(p, home);
                                Inventories.openInventory(InventoryType.DELETE, p);
                            } else {
                                home.delete();
                                Inventories.openHomeList(p, p);
                            }
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

                    // sorting
                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eSorting")) {

                        // type
                        if (e.getClick() == ClickType.LEFT) {
                            switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.Type")) {
                                case "date" -> {
                                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.Type", "name");
                                    Main.getPlugin().saveConfig();
                                    Inventories.openHomeList(p, target);
                                }
                                case "name" -> {
                                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.Type", "date");
                                    Main.getPlugin().saveConfig();
                                    Inventories.openHomeList(p, target);
                                }
                            }
                        }

                        // direction
                        else if (e.getClick() == ClickType.RIGHT) {
                            switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.Direction")) {
                                case "rising" -> {
                                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.Direction", "falling");
                                    Main.getPlugin().saveConfig();
                                    Inventories.openHomeList(p, target);
                                }
                                case "falling" -> {
                                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.Direction", "rising");
                                    Main.getPlugin().saveConfig();
                                    Inventories.openHomeList(p, target);
                                }
                            }
                        } else e.setCancelled(true);
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
}
