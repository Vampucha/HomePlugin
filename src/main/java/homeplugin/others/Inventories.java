package homeplugin.others;

import java.util.*;

import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.math3.util.Precision;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import homeplugin.main.Main;
import org.bukkit.util.Consumer;

public class Inventories {

    static FileConfiguration cfg = Main.getPlugin().getConfig();

    public static void openHomeList(Player p, Player target) {

        int currentPage = Main.page.get(p);

        UI.Page page = new UI.Page();

        page.setTitle("Homes of " + target.getName());
        page.setSize(54);
        page.setHolder(target);

        // settings
        UI.Item settings = new UI.Item();
        settings.setName("§eSettings");
        settings.setMaterial(Material.WRITABLE_BOOK);
        ArrayList<String> settingsLore = new ArrayList<>();
        settingsLore.add("§7Click to open the settings");
        settings.setLore(settingsLore);
        settings.setSlot(45);
        settings.onClick(player -> {
            Main.previousPage.get(p).add(page);
            openSettings(p);
        });
        page.addItem(settings);

        // create
        UI.Item create = new UI.Item();
        create.setName("§eCreate home");
        create.setMaterial(Material.LIME_CONCRETE);
        ArrayList<String> createLore = new ArrayList<>();
        createLore.add("§7Click to create a new home");
        create.setLore(createLore);
        create.setSlot(53);
        create.onClick(player -> {

            Main.previousPage.get(p).add(page);

            AnvilGUI.Builder builder = new AnvilGUI.Builder();
            builder.plugin(Main.getPlugin());

            ItemStack icon = findIcon(p);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.setDisplayName("§aNew Home");
            icon.setItemMeta(iconMeta);

            builder.onComplete((player2, text) -> {
                Home home = new Home(text, player);
                home.create();
                Main.currentHome.put(player, home);
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> Inventories.openIconList(p), 2);
                return AnvilGUI.Response.close();
            });
            builder.onClose(player2 -> openPreviousPage(p));
            builder.onLeftInputClick(player2 -> openPreviousPage(p));

            builder.title("Enter name");

            builder.itemLeft(icon);
            builder.text(icon.getItemMeta().getDisplayName());

            builder.open(p);
        });
        page.addItem(create);

        if (getHomes(target) != null && getHomes(target).size() > 0) {

            ArrayList<String> homes = sortHomeList(target);
            homes = searchResults(homes, p);

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction");

            UI.Item sort = new UI.Item();
            sort.setName("§eSorting");
            sort.setMaterial(Material.HOPPER);
            ArrayList<String> sortLore = new ArrayList<>();
            sortLore.add("§7Current: " + sorting + " | " + direction);
            sortLore.add("§7--------------------");
            sortLore.add("§7Left-click to change type");
            sortLore.add("§7Right-click to change direction");
            sort.setLore(sortLore);
            sort.setSlot(46);
            sort.onLeftClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type")) {
                    case "date" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "name");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "name" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "favorite");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "favorite" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type", "date");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            sort.onRightClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction")) {
                    case "rising" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction", "falling");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "falling" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction", "rising");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            page.addItem(sort);

            // search
            UI.Item search = new UI.Item();
            search.setName("§eSearch");
            search.setMaterial(Material.ANVIL);
            ArrayList<String> searchLore = new ArrayList<>();

            // click event / lore
            if (!Main.search.get(p).isEmpty()) {

                // add keyword
                search.onLeftClick(player -> {

                    Main.previousPage.get(p).add(page);

                    AnvilGUI.Builder builder = new AnvilGUI.Builder();
                    builder.plugin(Main.getPlugin());

                    ItemStack paper = new ItemStack(Material.PAPER);
                    ItemMeta paperMeta = paper.getItemMeta();
                    paperMeta.setDisplayName("§akeyword");
                    paper.setItemMeta(paperMeta);

                    builder.onComplete((player2, text) -> {
                        ArrayList<String> keywords = Main.search.get(p);
                        if (!keywords.contains(text)) {
                            keywords.add(text);
                            Main.search.put(p, keywords);
                            Main.previousPage.get(p).remove(Main.previousPage.get(p).size() - 1);
                            return AnvilGUI.Response.close();
                        }
                        return null;
                    });

                    builder.onClose(player2 -> openHomeList(p, target));
                    builder.onLeftInputClick(player2 -> openPreviousPage(p));

                    builder.itemLeft(paper);
                    builder.text("keyword");

                    builder.title("Enter keyword");

                    builder.open(p);
                });

                // manage keywords
                search.onRightClick(player -> {
                    Main.previousPage.get(p).add(page);
                    Inventories.openSearchKeys(p);
                });

                // clear
                search.onMiddleClick(player -> {
                    Main.search.put(p, new ArrayList<>());
                    reloadInventory(p);
                });

                // lore-keywords
                if (Main.search.get(p).size() == 1) searchLore.add("§7Keyword: " + Main.search.get(p).get(0));
                else {

                    StringBuilder keywords = new StringBuilder("§7Keywords: ");
                    for (int i = 0; i < Main.search.get(p).size(); i++) {
                        if (i <= 2) {
                            if (i != 0) keywords.append(", ");
                            keywords.append(Main.search.get(p).get(i));
                        } else break;
                    }

                    if (Main.search.get(p).size() > 3)
                        keywords.append(" (+").append(Main.search.get(p).size() - 3).append(")");

                    searchLore.add(keywords.toString());
                }

                // lore-keybinds
                searchLore.add("§7Left-click to add a keyword");
                searchLore.add("§7Right-click to remove the search");

            } else {
                search.onClick(player -> {

                    Main.previousPage.get(p).add(page);

                    AnvilGUI.Builder builder = new AnvilGUI.Builder();
                    builder.plugin(Main.getPlugin());

                    ItemStack paper = new ItemStack(Material.PAPER);
                    ItemMeta paperMeta = paper.getItemMeta();
                    paperMeta.setDisplayName("§akeyword");
                    if (!Main.search.get(p).isEmpty()) paperMeta.setDisplayName("§a" + Main.search.get(p));
                    paper.setItemMeta(paperMeta);

                    builder.onComplete((player2, text) -> {
                        ArrayList<String> keywords = Main.search.get(p);
                        if (!keywords.contains(text)) {
                            keywords.add(text);
                            Main.search.put(p, keywords);
                            Main.previousPage.get(p).remove(Main.previousPage.get(p).size() - 1);
                            return AnvilGUI.Response.close();
                        }
                        return null;
                    });

                    builder.onClose(player2 -> openHomeList(p, target));
                    builder.onLeftInputClick(player2 -> openPreviousPage(p));

                    builder.itemLeft(paper);
                    builder.text("keyword");
                    if (!Main.search.get(p).isEmpty()) builder.text(Main.search.get(p).get(0));

                    builder.title("Enter keyword");

                    builder.open(p);
                });
                searchLore.add("§7Click to search for a home");
            }
            search.setLore(searchLore);
            search.setSlot(47);
            page.addItem(search);

            // previous page
            if (currentPage > 1) {
                UI.Item pp = new UI.Item();
                ArrayList<String> ppLore = new ArrayList<>();
                ppLore.add("§7Click to go to the previous page");
                pp.setName("§ePrevious page");
                pp.setMaterial(Material.ARROW);
                pp.setLore(ppLore);
                pp.setSlot(48);
                pp.onClick(player -> {
                    Main.page.put(p, currentPage - 1);
                    reloadInventory(p);
                });
                page.addItem(pp);
            }

            // close / back
            UI.Item close = new UI.Item();
            ArrayList<String> closeLore = new ArrayList<>();
            if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
                close.setName("§eBack");
                closeLore.add("§7Click to go back");
                close.onClick(player -> openPreviousPage(p));
            } else {
                close.setName("§eClose");
                closeLore.add("§7Click to close");
                close.onClick(player -> p.closeInventory());
            }
            close.setMaterial(Material.BARRIER);
            close.setLore(closeLore);
            close.setSlot(49);
            page.addItem(close);

            // next page
            if (homes.size() > currentPage * 45) {
                UI.Item np = new UI.Item();
                ArrayList<String> npLore = new ArrayList<>();
                npLore.add("§7Click to go to the next page");
                np.setName("§eNext page");
                np.setMaterial(Material.ARROW);
                np.setLore(npLore);
                np.setSlot(50);
                np.onClick(player -> {
                    Main.page.put(p, currentPage + 1);
                    reloadInventory(p);
                });
                page.addItem(np);
            }

            // groups
            UI.Item groups = new UI.Item();
            groups.setName("§eGroups");
            groups.setMaterial(Material.BOOK);
            ArrayList<String> groupsLore = new ArrayList<>();
            groupsLore.add("§7Click to show the groups");
            groupsLore.add("§6§lComming Soon");
            groups.setLore(groupsLore);
            groups.setSlot(52);
            page.addItem(groups);

            // homes
            for (int i = (currentPage - 1) * 45; i < currentPage * 45; i++) {
                if (homes.size() > i) {
                    Home home = new Home(homes.get(i), target);

                    UI.Item icon = new UI.Item();
                    icon.setName("§a" + home.getName());
                    icon.setMaterial(home.getIcon().getType());
                    ArrayList<String> iconLore = new ArrayList<>();

                    if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.ShowInformation")) {
                        try {
                            Location loc = home.getLocation();
                            double x = Precision.round(loc.getX(), 2);
                            double y = Precision.round(loc.getY(), 2);
                            double z = Precision.round(loc.getZ(), 2);

                            iconLore.add("§7World: " + loc.getWorld().getName());
                            iconLore.add("§7X: " + x + " Y: " + y + " Z: " + z);
                            iconLore.add("§7--------------------");
                        } catch (Exception ignored) {
                        }
                    }

                    if (p == target) {

                        ArrayList<String> favoriteHomes = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".FavoriteHomes"));
                        iconLore.add("§7Left-click to visit");
                        iconLore.add("§7Right-click to edit");
                        if (favoriteHomes.contains(home.getName())) {
                            icon.setName("§a" + home.getName() + " §e⭐");
                            iconLore.add("§7Middle-click remove favorite");
                        } else {
                            icon.setName("§a" + home.getName());
                            iconLore.add("§7Middle-click add favorite");
                        }

                        icon.onRightClick(player -> {
                            Main.previousPage.get(p).add(page);
                            Main.currentHome.put(p, home);
                            openHomeGui(p, home);
                        });
                        icon.onLeftClick(player -> p.teleport(home.getLocation()));
                        icon.onMiddleClick(player -> {
                            if (favoriteHomes.contains(home.getName())) favoriteHomes.remove(home.getName());
                            else favoriteHomes.add(home.getName());
                            cfg.set("Players." + p.getUniqueId() + ".FavoriteHomes", favoriteHomes);
                            Main.getPlugin().saveConfig();
                            reloadInventory(p);
                        });

                    } else if (cfg.getBoolean("Players." + target.getUniqueId() + ".Settings.Visitors.Enabled")
                            || cfg.getStringList("Players." + target.getUniqueId() + ".Settings.Visitors.Whitelist").contains(p.getName())
                            || !cfg.getStringList("Players." + target.getUniqueId() + ".Settings.Visitors.BlackList").contains(p.getName())) {
                        iconLore.add("§7Click to visit");
                        icon.onClick(player -> p.teleport(home.getLocation()));
                    } else iconLore.add("§cYou can't visit the homes of this player");

                    icon.setLore(iconLore);
                    icon.setSlot(i - (currentPage - 1) * 45);

                    if (i < currentPage * 45)
                        page.addItem(icon);
                } else break;
            }
        }

        // no homes
        else {

            UI.Item nh = new UI.Item();
            nh.setName("§cNo homes");
            nh.setMaterial(Material.RED_STAINED_GLASS_PANE);
            ArrayList<String> nhLore = new ArrayList<>();
            nhLore.add("§7You can create a home by:");
            nhLore.add("§7- press the \"Create home\" button");
            nhLore.add("§7- execute the command \"/home create <name>\"");
            nh.setLore(nhLore);
            nh.setSlot(22);
            page.addItem(nh);
        }

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openIconList(Player p) {

        int currentPage = Main.page.get(p);

        UI.Page page = new UI.Page();

        page.setTitle("Choose icon");
        page.setSize(54);

        ArrayList<String> icons = sortIconList(p);
        icons = searchResults(icons, p);

        // previous page
        if (currentPage > 1) {
            UI.Item pp = new UI.Item();
            ArrayList<String> ppLore = new ArrayList<>();
            ppLore.add("§7Click to go to the previous page");
            pp.setName("§ePrevious page");
            pp.setMaterial(Material.ARROW);
            pp.setLore(ppLore);
            pp.setSlot(48);
            pp.onClick(player -> {
                Main.page.put(p, currentPage - 1);
                reloadInventory(p);
            });
            page.addItem(pp);
        }

        // next page
        if (icons.size() > currentPage * 45) {
            UI.Item np = new UI.Item();
            ArrayList<String> npLore = new ArrayList<>();
            npLore.add("§7Click to go to the next page");
            np.setName("§eNext page");
            np.setMaterial(Material.ARROW);
            np.setLore(npLore);
            np.setSlot(50);
            np.onClick(player -> {
                Main.page.put(p, currentPage + 1);
                reloadInventory(p);
            });
            page.addItem(np);
        }

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(49);
        page.addItem(close);

        // search
        UI.Item search = new UI.Item();
        search.setName("§eSearch");
        search.setMaterial(Material.ANVIL);
        ArrayList<String> searchLore = new ArrayList<>();

        // click event / lore
        if (!Main.search.get(p).isEmpty()) {

            // add keyword
            search.onLeftClick(player -> {

                Main.previousPage.get(p).add(page);

                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(Main.getPlugin());

                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setDisplayName("§akeyword");
                paper.setItemMeta(paperMeta);

                builder.onComplete((player2, text) -> {
                    ArrayList<String> keywords = Main.search.get(p);
                    if (!keywords.contains(text)) {
                        keywords.add(text);
                        Main.search.put(p, keywords);
                        Main.previousPage.get(p).remove(Main.previousPage.get(p).size() - 1);
                        return AnvilGUI.Response.close();
                    }
                    return null;
                });

                builder.onClose(player2 -> openIconList(p));
                builder.onLeftInputClick(player2 -> openPreviousPage(p));

                builder.itemLeft(paper);
                builder.text("keyword");

                builder.title("Enter keyword");

                builder.open(p);
            });

            // manage keywords
            search.onRightClick(player -> {
                Main.previousPage.get(p).add(page);
                Inventories.openSearchKeys(p);
            });

            // clear
            search.onMiddleClick(player -> {
                Main.search.put(p, new ArrayList<>());
                reloadInventory(p);
            });

            // lore-keywords
            if (Main.search.get(p).size() == 1) searchLore.add("§7Keyword: " + Main.search.get(p).get(0));
            else {

                StringBuilder keywords = new StringBuilder("§7Keywords: ");
                for (int i = 0; i < Main.search.get(p).size(); i++) {
                    if (i <= 2) {
                        if (i != 0) keywords.append(", ");
                        keywords.append(Main.search.get(p).get(i));
                    } else break;
                }

                if (Main.search.get(p).size() > 3)
                    keywords.append(" (+").append(Main.search.get(p).size() - 3).append(")");

                searchLore.add(keywords.toString());
            }

            // lore-keybinds
            searchLore.add("§7Left-click to add a keyword");
            searchLore.add("§7Right-click to remove the search");

        } else {
            search.onClick(player -> {

                Main.previousPage.get(p).add(page);

                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(Main.getPlugin());

                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setDisplayName("§akeyword");
                if (!Main.search.get(p).isEmpty()) paperMeta.setDisplayName("§a" + Main.search.get(p));
                paper.setItemMeta(paperMeta);

                builder.onComplete((player2, text) -> {
                    ArrayList<String> keywords = Main.search.get(p);
                    if (!keywords.contains(text)) {
                        keywords.add(text);
                        Main.search.put(p, keywords);
                        Main.previousPage.get(p).remove(Main.previousPage.get(p).size() - 1);
                        return AnvilGUI.Response.close();
                    }
                    return null;
                });

                builder.onClose(player2 -> openIconList(p));
                builder.onLeftInputClick(player2 -> openPreviousPage(p));

                builder.itemLeft(paper);
                builder.text("keyword");
                if (!Main.search.get(p).isEmpty()) builder.text(Main.search.get(p).get(0));

                builder.title("Enter keyword");

                builder.open(p);
            });
            searchLore.add("§7Click to search for a home");
        }
        search.setLore(searchLore);
        search.setSlot(47);
        page.addItem(search);

        // sorting
        String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type");
        String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction");

        UI.Item sort = new UI.Item();
        sort.setName("§eSorting");
        sort.setMaterial(Material.HOPPER);
        ArrayList<String> sortLore = new ArrayList<>();
        sortLore.add("§7Current: " + sorting + " | " + direction);
        sortLore.add("§7--------------------");
        sortLore.add("§7Left-click to change type");
        sortLore.add("§7Right-click to change direction");
        sort.setLore(sortLore);
        sort.setSlot(46);
        sort.onLeftClick(player -> {
            switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type")) {
                case "default" -> {
                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type", "name");
                    Main.getPlugin().saveConfig();
                    Inventories.reloadInventory(p);
                }
                case "name" -> {
                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type", "default");
                    Main.getPlugin().saveConfig();
                    Inventories.reloadInventory(p);
                }
            }
        });
        sort.onRightClick(player -> {
            switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction")) {
                case "rising" -> {
                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction", "falling");
                    Main.getPlugin().saveConfig();
                    Inventories.reloadInventory(p);
                }
                case "falling" -> {
                    cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction", "rising");
                    Main.getPlugin().saveConfig();
                    Inventories.reloadInventory(p);
                }
            }
        });
        page.addItem(sort);

        // icons
        for (int i = (currentPage - 1) * 45; i < currentPage * 45; i++) {
            if (icons.size() > i) {

                UI.Item icon = new UI.Item();
                icon.setName(ChatColor.GREEN + icons.get(i).toLowerCase().replaceAll("_", " "));
                icon.setMaterial(Material.getMaterial(icons.get(i)));
                ArrayList<String> iconLore = new ArrayList<>();
                iconLore.add("§7Click to choose");
                icon.setLore(iconLore);
                icon.setSlot(i - (currentPage - 1) * 45);
                icon.onClick(player -> {
                    Main.currentHome.get(p).setIcon(new ItemStack(icon.getMaterial()));
                    openPreviousPage(p);
                });

                if (i < currentPage * 45) page.addItem(icon);
            } else break;
        }

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openWhitelist(Player p) {

        UI.Page page = new UI.Page();

        page.setTitle("Whitelist");
        page.setSize(54);

        int currentPage = Main.page.get(p);
        ArrayList<String> whitelist = getWhiteList(p);

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            // back
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            // close
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(49);
        page.addItem(close);

        // add
        UI.Item create = new UI.Item();
        create.setName("§eAdd");
        create.setMaterial(Material.LIME_CONCRETE);
        ArrayList<String> createLore = new ArrayList<>();
        createLore.add("§7Click to add a player to the whitelist");
        create.setLore(createLore);
        create.setSlot(53);
        create.onClick(player -> {
            Main.previousPage.get(p).add(page);
            openPlayerList(p);
        });
        page.addItem(create);

        if (!whitelist.isEmpty()) {

            ArrayList<String> players = sortPlayerList(whitelist, p);
            players = searchResults(players, p);

            // next page
            if (players.size() > currentPage * 45) {
                UI.Item np = new UI.Item();
                np.setName("§eNext page");
                np.setMaterial(Material.ARROW);
                ArrayList<String> npLore = new ArrayList<>();
                npLore.add("§7Click to go to the next page");
                np.setLore(npLore);
                np.setSlot(50);
                np.onClick(player -> {
                    Main.page.put(p, Main.page.get(p) + 1);
                    reloadInventory(p);
                });
                page.addItem(np);
            }

            // previous page
            if (currentPage > 1) {
                UI.Item pp = new UI.Item();
                pp.setName("§ePrevious page");
                pp.setMaterial(Material.ARROW);
                ArrayList<String> ppLore = new ArrayList<>();
                ppLore.add("§7Click to go to the previous page");
                pp.setLore(ppLore);
                pp.setSlot(48);
                pp.onClick(player -> {
                    Main.page.put(p, Main.page.get(p) - 1);
                    reloadInventory(p);
                });
                page.addItem(pp);
            }

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction");

            UI.Item sort = new UI.Item();
            sort.setName("§eSorting");
            sort.setMaterial(Material.HOPPER);
            ArrayList<String> sortLore = new ArrayList<>();
            sortLore.add("§7Current: " + sorting + " | " + direction);
            sortLore.add("§7Left-click to change type");
            sortLore.add("§7Right-click to change direction");
            sort.setLore(sortLore);
            sort.setSlot(46);
            sort.onLeftClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
                    case "state" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "name");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "name" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "state");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            sort.onRightClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction")) {
                    case "rising" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "falling");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "falling" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "rising");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            page.addItem(sort);

            // players
            for (int i = (currentPage - 1) * 45; i < currentPage * 45; i++) {
                if (players.size() > i) {

                    OfflinePlayer player;
                    String name = players.get(i);
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(name))) player = Bukkit.getPlayer(name);
                    else player = Bukkit.getOfflinePlayer(name);

                    UI.Item head = new UI.Item();
                    head.setName(ChatColor.GREEN + player.getName());
                    head.setMaterial(Material.PLAYER_HEAD);
                    ArrayList<String> headLore = new ArrayList<>();
                    if (Bukkit.getOnlinePlayers().contains(player))
                        headLore.add("§7State: §aOnline");
                    else headLore.add("§7State: §cOffline");
                    headLore.add("§7Click to remove");
                    head.setLore(headLore);
                    head.setSlot(i - (currentPage - 1) * 45);

                    if (i < currentPage * 45) page.addItem(head);
                } else break;
            }
        }

        // no players
        else {
            UI.Item np = new UI.Item();
            np.setName("§cNo players on the whitelist");
            np.setMaterial(Material.RED_STAINED_GLASS_PANE);
            ArrayList<String> npLore = new ArrayList<>();
            npLore.add("§7You can add a player with the add-button");
            np.setLore(npLore);
            np.setSlot(22);
            page.addItem(np);
        }

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openBlacklist(Player p) {

        UI.Page page = new UI.Page();

        page.setTitle("Blacklist");
        page.setSize(54);

        int currentPage = Main.page.get(p);
        ArrayList<String> blacklist = getBlackList(p);

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            // back
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            // close
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(49);
        page.addItem(close);

        // add
        UI.Item create = new UI.Item();
        create.setName("§eAdd");
        create.setMaterial(Material.LIME_CONCRETE);
        ArrayList<String> createLore = new ArrayList<>();
        createLore.add("§7Click to add a player to the whitelist");
        create.setLore(createLore);
        create.setSlot(53);
        create.onClick(player -> {
            Main.previousPage.get(p).add(page);
            openPlayerList(p);
        });
        page.addItem(create);

        if (!blacklist.isEmpty()) {

            ArrayList<String> players = sortPlayerList(blacklist, p);
            players = searchResults(players, p);

            // next page
            if (players.size() > currentPage * 45) {
                UI.Item np = new UI.Item();
                np.setName("§eNext page");
                np.setMaterial(Material.ARROW);
                ArrayList<String> npLore = new ArrayList<>();
                npLore.add("§7Click to go to the next page");
                np.setLore(npLore);
                np.setSlot(50);
                np.onClick(player -> {
                    Main.page.put(p, Main.page.get(p) + 1);
                    reloadInventory(p);
                });
                page.addItem(np);
            }

            // previous page
            if (currentPage > 1) {
                UI.Item pp = new UI.Item();
                pp.setName("§ePrevious page");
                pp.setMaterial(Material.ARROW);
                ArrayList<String> ppLore = new ArrayList<>();
                ppLore.add("§7Click to go to the previous page");
                pp.setLore(ppLore);
                pp.setSlot(48);
                pp.onClick(player -> {
                    Main.page.put(p, Main.page.get(p) - 1);
                    reloadInventory(p);
                });
                page.addItem(pp);
            }

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction");

            UI.Item sort = new UI.Item();
            sort.setName("§eSorting");
            sort.setMaterial(Material.HOPPER);
            ArrayList<String> sortLore = new ArrayList<>();
            sortLore.add("§7Current: " + sorting + " | " + direction);
            sortLore.add("§7Left-click to change type");
            sortLore.add("§7Right-click to change direction");
            sort.setLore(sortLore);
            sort.setSlot(46);
            sort.onLeftClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
                    case "state" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "name");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "name" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "state");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            sort.onRightClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction")) {
                    case "rising" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "falling");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "falling" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "rising");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            page.addItem(sort);

            // players
            for (int i = (currentPage - 1) * 45; i < currentPage * 45; i++) {
                if (players.size() > i) {

                    OfflinePlayer player;
                    String name = players.get(i);
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(name))) player = Bukkit.getPlayer(name);
                    else player = Bukkit.getOfflinePlayer(name);

                    UI.Item head = new UI.Item();
                    head.setName(ChatColor.GREEN + player.getName());
                    head.setMaterial(Material.PLAYER_HEAD);
                    ArrayList<String> headLore = new ArrayList<>();
                    if (Bukkit.getOnlinePlayers().contains(player))
                        headLore.add("§7State: §aOnline");
                    else headLore.add("§7State: §cOffline");
                    headLore.add("§7Click to remove");
                    head.setLore(headLore);
                    head.setSlot(i - (currentPage - 1) * 45);

                    if (i < currentPage * 45) page.addItem(head);
                } else break;
            }
        }

        // no players
        else {
            UI.Item np = new UI.Item();
            np.setName("§cNo players on the whitelist");
            np.setMaterial(Material.RED_STAINED_GLASS_PANE);
            ArrayList<String> npLore = new ArrayList<>();
            npLore.add("§7You can add a player with the add-button");
            np.setLore(npLore);
            np.setSlot(22);
            page.addItem(np);
        }

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openPlayerList(Player p) {

        UI.Page page = new UI.Page();

        page.setTitle("Choose player");
        page.setSize(54);

        int currentPage = Main.page.get(p);
        ArrayList<String> players = new ArrayList<>(getPlayers());

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(22);
        page.addItem(close);

        close.setSlot(49);
        page.addItem(close);

        if (!players.isEmpty()) {

            // next page
            if (players.size() > currentPage * 45) {
                UI.Item np = new UI.Item();
                np.setName("§eNext page");
                np.setMaterial(Material.ARROW);
                ArrayList<String> npLore = new ArrayList<>();
                npLore.add("§7Click to go to the next page");
                np.setLore(npLore);
                np.setSlot(50);
                np.onClick(player -> {
                    Main.page.put(p, Main.page.get(p) + 1);
                    reloadInventory(p);
                });
                page.addItem(np);
            }

            // previous page
            if (currentPage > 1) {
                UI.Item pp = new UI.Item();
                pp.setName("§ePrevious page");
                pp.setMaterial(Material.ARROW);
                ArrayList<String> ppLore = new ArrayList<>();
                ppLore.add("§7Click to go to the previous page");
                pp.setLore(ppLore);
                pp.setSlot(48);
                pp.onClick(player -> {
                    Main.page.put(p, Main.page.get(p) - 1);
                    reloadInventory(p);
                });
                page.addItem(pp);
            }

            // sorting
            String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type");
            String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction");

            UI.Item sort = new UI.Item();
            sort.setName("§eSorting");
            sort.setMaterial(Material.HOPPER);
            ArrayList<String> sortLore = new ArrayList<>();
            sortLore.add("§7Current: " + sorting + " | " + direction);
            sortLore.add("§7Left-click to change type");
            sortLore.add("§7Right-click to change direction");
            sort.setLore(sortLore);
            sort.setSlot(46);
            sort.onLeftClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
                    case "state" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "name");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "name" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type", "state");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            sort.onRightClick(player -> {
                switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction")) {
                    case "rising" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "falling");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                    case "falling" -> {
                        cfg.set("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction", "rising");
                        Main.getPlugin().saveConfig();
                        Inventories.reloadInventory(p);
                    }
                }
            });
            page.addItem(sort);

            for (int i = (currentPage - 1) * 45; i < currentPage * 45; i++) {
                if (players.size() > i) {

                    Player player = Bukkit.getPlayer(players.get(i));

                    UI.Item head = new UI.Item();
                    head.setName(ChatColor.GREEN + player.getName());
                    head.setMaterial(Material.PLAYER_HEAD);
                    ArrayList<String> headLore = new ArrayList<>();
                    headLore.add("§7Click to choose");
                    head.setLore(headLore);
                    head.setSlot(i - (currentPage - 1) * 45);

                    if (i < currentPage * 45) page.addItem(head);
                } else break;
            }
        }

        // no players
        else {
            UI.Item np = new UI.Item();
            np.setName("§cNo players online");
            np.setMaterial(Material.RED_STAINED_GLASS_PANE);
            ArrayList<String> npLore = new ArrayList<>();
            npLore.add("§7There are currently no other players online");
            np.setLore(npLore);
            np.setSlot(22);
            page.addItem(np);
        }

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openLocationGui(Player p, Home home) {

        UI.Page page = new UI.Page();

        page.setTitle("Change location");
        page.setSize(27);

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(22);
        page.addItem(close);

        // x
        UI.Item x = new UI.Item();
        x.setName("§eX");
        x.setMaterial(Material.BLUE_CONCRETE);
        ArrayList<String> xLore = new ArrayList<>();
        xLore.add("§7Current: " + Precision.round(home.getLocation().getX(), 2));
        xLore.add("§7Click to change the x value");
        x.setLore(xLore);
        x.setSlot(10);
        page.addItem(x);

        // y
        UI.Item y = new UI.Item();
        y.setName("§eY");
        y.setMaterial(Material.LIME_CONCRETE);
        ArrayList<String> yLore = new ArrayList<>();
        yLore.add("§7Current: " + Precision.round(home.getLocation().getY(), 2));
        yLore.add("§7Click to change the y value");
        y.setLore(yLore);
        y.setSlot(11);
        page.addItem(y);

        // z
        UI.Item z = new UI.Item();
        z.setName("§eZ");
        z.setMaterial(Material.RED_CONCRETE);
        ArrayList<String> zLore = new ArrayList<>();
        zLore.add("§7Current: " + Precision.round(home.getLocation().getZ(), 2));
        zLore.add("§7Click to change the z value");
        z.setLore(zLore);
        z.setSlot(12);
        page.addItem(z);

        // yaw
        UI.Item yaw = new UI.Item();
        ArrayList<String> yawLore = new ArrayList<>();
        if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Orientation")) {
            yaw.setName("§eYaw");
            yaw.setMaterial(Material.YELLOW_CONCRETE);
            yawLore.add("§7Current: " + Precision.round(home.getLocation().getYaw(), 2));
            yawLore.add("§7Click to change the yaw value");
        } else {
            yaw.setName("§fYaw");
            yaw.setMaterial(Material.GRAY_CONCRETE);
            yawLore.add("§7You have disabled orientation in the settings");
            yawLore.add("§7Click here to enable orientation");
            yaw.onClick(player -> {
                Main.previousPage.get(p).add(page);
                openSettings(p);
            });
        }
        yaw.setLore(yawLore);
        yaw.setSlot(13);
        page.addItem(yaw);

        // pitch
        UI.Item pitch = new UI.Item();
        ArrayList<String> pitchLore = new ArrayList<>();
        if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Orientation")) {
            pitch.setName("§ePitch");
            pitch.setMaterial(Material.PINK_CONCRETE);
            pitchLore.add("§7Current: " + Precision.round(home.getLocation().getPitch(), 2));
            pitchLore.add("§7Click to change the pitch value");
        } else {
            pitch.setName("§fPitch");
            pitch.setMaterial(Material.GRAY_CONCRETE);
            pitchLore.add("§7You have disabled orientation in the settings");
            pitchLore.add("§7Click here to enable orientation");
            pitch.onClick(player -> {
                Main.previousPage.get(p).add(page);
                openSettings(p);
            });
        }
        pitch.setLore(pitchLore);
        pitch.setSlot(14);
        page.addItem(pitch);

        // world
        UI.Item world = new UI.Item();
        world.setName("§eWorld");
        world.setMaterial(Material.BOOK);
        ArrayList<String> worldLore = new ArrayList<>();
        worldLore.add("§7Current: " + home.getLocation().getWorld().getName());
        worldLore.add("§7Click to change the world");
        world.setLore(worldLore);
        world.setSlot(15);
        page.addItem(world);


        // preview
        UI.Item preview = new UI.Item();
        preview.setName("§ePreview");
        preview.setMaterial(Material.COMPASS);
        ArrayList<String> previewLore = new ArrayList<>();
        previewLore.add("§7Click to preview the current location");
        previewLore.add("§6§lComming Soon");
        preview.setLore(previewLore);
        preview.setSlot(16);
        page.addItem(preview);

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openConfirmation(Player p, Consumer<Player> yesAction) {

        UI.Page page = new UI.Page();

        page.setSize(27);
        page.setTitle("Are you sure?");

        UI.Item no = new UI.Item();
        ArrayList<String> noLore = new ArrayList<>();
        no.setMaterial(Material.RED_STAINED_GLASS_PANE);
        no.setName("§cNo");
        noLore.add("§7Click to cancel");
        no.setLore(noLore);
        no.setSlot(14);
        no.onClick(player -> openPreviousPage(p));

        UI.Item yes = new UI.Item();
        ArrayList<String> yesLore = new ArrayList<>();
        yes.setMaterial(Material.LIME_STAINED_GLASS_PANE);
        yes.setName("§aYes");
        yesLore.add("§7Click to confirm");
        yes.setLore(yesLore);
        yes.setSlot(12);
        yes.onClick(player -> yesAction.accept(p));

        page.addItem(no);
        page.addItem(yes);

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openSettings(Player p) {

        UI.Page page = new UI.Page();

        page.setSize(27);
        page.setTitle("Settings");

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(22);

        // show information
        UI.Item info = new UI.Item();
        ArrayList<String> infoLore = new ArrayList<>();
        if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.ShowInformation")) {
            info.setMaterial(Material.LIME_DYE);
            infoLore.add("§7Show information about the home in \"Your homes\"");
            infoLore.add("§7State: §aOn");
            infoLore.add("§7--------------------");
            infoLore.add("§7Click to disable");
            info.onClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.ShowInformation", false);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
        } else {
            info.setMaterial(Material.RED_DYE);
            infoLore.add("§7Show information about the home in \"Your homes\"");
            infoLore.add("§7State: §cOff");
            infoLore.add("§7--------------------");
            infoLore.add("§7Click to enable");
            info.setLore(infoLore);
            info.onClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.ShowInformation", true);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
        }
        info.setName("§eShow information");
        info.setSlot(10);
        info.setLore(infoLore);

        // delete protection
        UI.Item dp = new UI.Item();
        ArrayList<String> dpLore = new ArrayList<>();
        if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
            dp.setMaterial(Material.LIME_DYE);
            dpLore.add("§7Open inventory to confirm delete");
            dpLore.add("§7State: §aOn");
            dpLore.add("§7--------------------");
            dpLore.add("§7Click to disable");
            dp.onClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.DeleteProtection", false);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
        } else {
            dp.setMaterial(Material.RED_DYE);
            dpLore.add("§7Open inventory to corfirm delete");
            dpLore.add("§7State: §cOff");
            dpLore.add("§7--------------------");
            dpLore.add("§7Click to enable");
            dp.onClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.DeleteProtection", true);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
        }
        dp.setName("§eDelete protection");
        dp.setSlot(11);
        dp.setLore(dpLore);

        // orientation
        UI.Item or = new UI.Item();
        ArrayList<String> orLore = new ArrayList<>();
        if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Orientation")) {
            or.setMaterial(Material.LIME_DYE);
            orLore.add("§7Use yaw and pitch for homes");
            orLore.add("§7State: §aOn");
            orLore.add("§7--------------------");
            orLore.add("§7Click to disable");
            or.onClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.Orientation", false);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
        } else {
            or.setMaterial(Material.RED_DYE);
            orLore.add("§7Use yaw and pitch for homes");
            orLore.add("§7State: §cOff");
            orLore.add("§7--------------------");
            orLore.add("§7Click to enable");
            or.onClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.Orientation", true);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
        }
        or.setName("§eOrientation");
        or.setSlot(12);
        or.setLore(orLore);

        // visitors
        UI.Item vis = new UI.Item();
        ArrayList<String> visLore = new ArrayList<>();
        if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled")) {
            vis.setMaterial(Material.LIME_DYE);
            visLore.add("§7Player can visit your homes");
            visLore.add("§7State: §aOn");
            visLore.add("§7--------------------");
            visLore.add("§7Left-click to disable");
            visLore.add("§7Right-click to manage blacklist");
            vis.onLeftClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled", false);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
            vis.onRightClick(player -> {
                Main.previousPage.get(p).add(page);
                openBlacklist(p);
            });
        } else {
            vis.setMaterial(Material.RED_DYE);
            visLore.add("§7Players can visit your homes");
            visLore.add("§7State: §cOff");
            visLore.add("§7--------------------");
            visLore.add("§7Left-click to enable");
            visLore.add("§7Right-click to manage whitelist");
            vis.onLeftClick(player -> {
                cfg.set("Players." + p.getUniqueId() + ".Settings.Visitors.Enabled", true);
                Main.getPlugin().saveConfig();
                openSettings(p);
            });
            vis.onRightClick(player -> {
                Main.previousPage.get(p).add(page);
                openWhitelist(p);
            });
        }
        vis.setName("§eVisitors");
        vis.setSlot(13);
        vis.setLore(visLore);

        // add items
        page.addItem(close);
        page.addItem(info);
        page.addItem(dp);
        page.addItem(or);
        page.addItem(vis);

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openHomeGui(Player p, Home home) {

        UI.Page page = new UI.Page();

        page.setTitle(home.getName());
        page.setSize(27);

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(22);

        // name
        UI.Item name = new UI.Item();
        ArrayList<String> nameLore = new ArrayList<>();
        name.setName("§eName");
        name.setMaterial(Material.NAME_TAG);
        nameLore.add("§7" + home.getName());
        nameLore.add("§7--------------------");
        nameLore.add("§7Click to change");
        name.setLore(nameLore);
        name.setSlot(10);

        // location
        UI.Item loc = new UI.Item();
        ArrayList<String> locLore = new ArrayList<>();
        double x = Precision.round(home.getLocation().getX(), 2);
        double y = Precision.round(home.getLocation().getY(), 2);
        double z = Precision.round(home.getLocation().getZ(), 2);
        String w = home.getLocation().getWorld().getName();
        loc.setName("§eLocation");
        loc.setMaterial(Material.COMPASS);
        locLore.add("§7X: " + x);
        locLore.add("§7Y: " + y);
        locLore.add("§7Z: " + z);
        locLore.add("§7World: " + w);
        locLore.add("§7--------------------");
        locLore.add("§7Click to change");
        locLore.add("§6§lComming Soon");
        loc.setLore(locLore);
        loc.setSlot(11);
        loc.onClick(player -> {
            Main.previousPage.get(p).add(page);
            openLocationGui(p, home);
        });

        // icon
        UI.Item icon = new UI.Item();
        ArrayList<String> iconLore = new ArrayList<>();
        icon.setName("§eIcon");
        icon.setMaterial(home.getIcon().getType());
        iconLore.add("§7" + home.getIcon().getType().toString().toLowerCase().replaceAll("_", " "));
        iconLore.add("§7--------------------");
        iconLore.add("§7Click to change");
        icon.setLore(iconLore);
        icon.setSlot(12);
        icon.onClick(player -> {
            Main.previousPage.get(p).add(page);
            openIconList(p);
        });

        // groups
        UI.Item groups = new UI.Item();
        ArrayList<String> groupsLore = new ArrayList<>();
        groups.setName("§eGroups");
        groups.setMaterial(Material.BOOK);
        groupsLore.add("§7Groups: No groups");
        groupsLore.add("§7--------------------");
        groupsLore.add("§7Click to manage groups");
        groupsLore.add("§6§lComming Soon");
        groups.setLore(groupsLore);
        groups.setSlot(13);

        // type
        UI.Item type = new UI.Item();
        ArrayList<String> typeLore = new ArrayList<>();
        type.setName("§eType");

        switch (home.getType()) {
            case PUBLIC -> {
                type.setMaterial(Material.LIME_DYE);
                typeLore.add("§7Type: §aPublic");
                typeLore.add("§7Everyone can join");
                type.onClick(player -> {
                    home.setType(Home.HomeType.PASSWORD);
                    reloadInventory(p);
                });
            }
            case PASSWORD -> {
                type.setMaterial(Material.YELLOW_DYE);
                typeLore.add("§7Type: §ePassword");
                typeLore.add("§7Only players with the password can join");
                type.onLeftClick(player ->
                {
                    home.setType(Home.HomeType.PRIVATE);
                    reloadInventory(p);
                });
                type.onRightClick(player -> p.sendMessage(Main.prefix + "Change Password - §6§lComming Soon"));
            }
            case PRIVATE -> {
                type.setMaterial(Material.RED_DYE);
                typeLore.add("§7Type: §cPrivate");
                typeLore.add("§7Noone can join your home");
                type.onClick(player -> {
                    home.setType(Home.HomeType.PUBLIC);
                    reloadInventory(p);
                });
            }
        }

        typeLore.add("§7--------------------");
        typeLore.add("§7Click to change");
        typeLore.add("§6§lComming Soon");
        type.setLore(typeLore);
        type.setSlot(14);

        // delete
        UI.Item delete = new UI.Item();
        ArrayList<String> deleteLore = new ArrayList<>();
        delete.setName("§cDelete");
        delete.setMaterial(Material.RED_CONCRETE);
        deleteLore.add("§7Click to delete");
        delete.setLore(deleteLore);
        delete.setSlot(16);
        delete.onClick(player -> {
            if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                Main.previousPage.get(p).add(page);
                openConfirmation(p, player2 -> {
                    home.delete();
                    Inventories.openHomeList(p, p);
                });
            } else {
                delete.onClick(player2 -> {
                    home.delete();
                    Inventories.openHomeList(p, p);
                });
            }
        });
        page.addItem(delete);

        // add items
        page.addItem(close);
        page.addItem(name);
        page.addItem(loc);
        page.addItem(icon);
        page.addItem(groups);
        page.addItem(type);
        page.addItem(delete);

        page.setPlugin(Main.getPlugin());
        page.open(p);
    }

    public static void openSearchKeys(Player p) {

        UI.Page page = new UI.Page();

        page.setTitle("Keywords");
        page.setSize(54);
        int currentPage = Main.page.get(p);

        // create
        UI.Item create = new UI.Item();
        ArrayList<String> createLore = new ArrayList<>();
        createLore.add("§7Click to add a new keyword");
        create.setName("§eAdd Keyword");
        create.setMaterial(Material.LIME_CONCRETE);
        create.setLore(createLore);
        create.setSlot(53);
        page.addItem(create);

        // previous page
        if (currentPage > 1) {
            UI.Item pp = new UI.Item();
            ArrayList<String> ppLore = new ArrayList<>();
            ppLore.add("§7Click to go to the previous page");
            pp.setName("§ePrevious page");
            pp.setMaterial(Material.ARROW);
            pp.setLore(ppLore);
            pp.setSlot(48);
            pp.onClick(player -> {
                Main.page.put(p, currentPage - 1);
                reloadInventory(p);
            });
            page.addItem(pp);
        }

        // next page
        if (Main.search.get(p).size() > currentPage * 45) {
            UI.Item np = new UI.Item();
            ArrayList<String> npLore = new ArrayList<>();
            npLore.add("§7Click to go to the next page");
            np.setName("§eNext page");
            np.setMaterial(Material.ARROW);
            np.setLore(npLore);
            np.setSlot(50);
            np.onClick(player -> {
                Main.page.put(p, currentPage + 1);
                reloadInventory(p);
            });
            page.addItem(np);
        }

        // close / back
        UI.Item close = new UI.Item();
        ArrayList<String> closeLore = new ArrayList<>();
        if (Main.previousPage.get(p) != null) {
            close.setName("§eBack");
            closeLore.add("§7Click to go back");
            close.onClick(player -> openPreviousPage(p));
        } else {
            close.setName("§eClose");
            closeLore.add("§7Click to close");
            close.onClick(player -> p.closeInventory());
        }
        close.setMaterial(Material.BARRIER);
        close.setLore(closeLore);
        close.setSlot(49);
        page.addItem(close);

        // sorting
        String sorting = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.SearchKeys.Type");
        String direction = cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.SearchKeys.Direction");
        UI.Item sort = new UI.Item();
        sort.setName("§eSorting");
        ArrayList<String> sortLore = new ArrayList<>();
        sortLore.add("§7Current: " + sorting + " | " + direction);
        sortLore.add("§7--------------------");
        sortLore.add("§7Left-click to change type");
        sortLore.add("§7Right-click to change direction");
        sort.setMaterial(Material.HOPPER);
        sort.setLore(sortLore);
        sort.setSlot(46);
        page.addItem(sort);

        ArrayList<String> keywords = sortKeywords(p);
        for (int i = (currentPage - 1) * 45; i < currentPage * 45; i++) {
            if (keywords.size() > i) {

                UI.Item keyword = new UI.Item();
                keyword.setName(ChatColor.GREEN + keywords.get(i));
                ArrayList<String> keywordLore = new ArrayList<>();
                keywordLore.add("§7Left-click to edit");
                keywordLore.add("§7Right-click to remove");
                keyword.setMaterial(Material.PAPER);
                keyword.setLore(keywordLore);
                keyword.setSlot(i - (currentPage - 1) * 45);

                if (i < currentPage * 45) page.addItem(keyword);
            } else break;
        }

        page.setPlugin(Main.getPlugin());
        page.open(p);
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

    public static void reloadInventory(Player p) {

        Player target = (Player) p.getInventory().getHolder();

        if (Main.currentHome.get(p) != null && p.getOpenInventory().getTitle().equals(Main.currentHome.get(p).getName()))
            openHomeGui(p, Main.currentHome.get(p));
        else if (p.getOpenInventory().getTitle().equals("Homes of " + target.getName()))
            openHomeList(p, target);
        else {
            switch (p.getOpenInventory().getTitle()) {
                case "Settings" -> openSettings(p);
                case "Whitelist" -> openWhitelist(p);
                case "Blacklist" -> openBlacklist(p);
                case "Choose icon" -> openIconList(p);
                case "Choose player" -> openPlayerList(p);
                case "Keywords" -> openSearchKeys(p);
            }
        }
    }

    public static void openPreviousPage(Player p) {

        Home home = Main.currentHome.get(p);
        if (Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1) != null) {

            Player target = Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1).getHolder();
            String title = Main.previousPage.get(p).get(Main.previousPage.get(p).size() - 1).getTitle();
            if (title.equals("Choose icon")) openIconList(p);
            else if (title.equals("Whitelist")) openWhitelist(p);
            else if (title.equals("Blacklist")) openBlacklist(p);
            else if (title.equals("Change location")) openLocationGui(p, home);
            else if (title.equals("Settings")) openSettings(p);
            else if (title.equals("Keywords")) openSearchKeys(p);
            else if (Bukkit.getOnlinePlayers().contains(target)) {
                if (title.equals("Homes of " + target.getName())) openHomeList(p, target);
            } else if (home != null)
                if (title.equals(home.getName())) openHomeGui(p, home);

        } else p.closeInventory();

        Main.previousPage.get(p).remove(Main.previousPage.get(p).size() - 1);
    }

    public static ItemStack findIcon(Player p) {
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

    private static ArrayList<String> searchResults(ArrayList<String> list, Player p) {
        ArrayList<String> searchResults = new ArrayList<>();
        if (!Main.search.get(p).isEmpty()) {
            for (String name : list) {
                for (String keyword : Main.search.get(p)) {
                    if (name.toLowerCase().contains(keyword.toLowerCase())) {
                        if (!searchResults.contains(name)) searchResults.add(name);
                    }
                }
            }
        } else return list;
        return searchResults;
    }

    private static ArrayList<String> sortHomeList(Player p) {
        ArrayList<String> list = getHomes(p);
        switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Type")) {
            case "name" -> Collections.sort(list);
            case "favorite" -> {
                ArrayList<String> newList = new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".FavoriteHomes"));
                list.removeAll(newList);
                Collections.sort(list);
                Collections.sort(newList);
                newList.addAll(list);
                list = newList;
            }
        }

        if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.HomeList.Direction").equals("falling"))
            Collections.reverse(list);

        return list;
    }

    private static ArrayList<String> sortIconList(Player p) {
        ArrayList<String> list = getIcons();

        if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Type").equals("name"))
            Collections.sort(list);
        if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.IconList.Direction").equals("falling"))
            Collections.reverse(list);

        return list;
    }

    private static ArrayList<String> sortPlayerList(ArrayList<String> unsortedList, Player p) {
        switch (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Type")) {
            case "state" -> {
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
            }
            case "name" -> {
                Collections.sort(unsortedList);
                if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.PlayerList.Direction").equals("falling"))
                    Collections.reverse(unsortedList);
            }
        }
        return unsortedList;
    }

    private static ArrayList<String> sortKeywords(Player p) {
        ArrayList<String> list = Main.search.get(p);

        if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.SearchKeys.Type").equals("name"))
            Collections.sort(list);
        if (cfg.getString("Players." + p.getUniqueId() + ".Settings.Sorting.SearchKeys.Direction").equals("falling"))
            Collections.reverse(list);

        return list;
    }

    private static ArrayList<String> getWhiteList(Player p) {
        return new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Whitelist"));
    }

    private static ArrayList<String> getBlackList(Player p) {
        return new ArrayList<>(cfg.getStringList("Players." + p.getUniqueId() + ".Settings.Visitors.Blacklist"));
    }
}
