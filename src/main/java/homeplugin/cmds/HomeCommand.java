package homeplugin.cmds;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import homeplugin.main.Main;
import homeplugin.others.Home;
import homeplugin.others.Inventories;

public class HomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command c, String arg2, String[] args) {

        FileConfiguration cfg = Main.getPlugin().getConfig();

        String create_success = cfg.getString("Settings.ChatMessages.Create.Success");
        String create_isexisting = cfg.getString("Settings.ChatMessages.Create.IsExisting");
        String delete_success = cfg.getString("Settings.ChatMessages.Delete.Success");
        String delete_notexisting = cfg.getString("Settings.ChatMessages.Delete.NotExisting");
        String modify_name_success = cfg.getString("Settings.ChatMessages.Modify.Name.Success");
        String modify_notexisting = cfg.getString("Settings.ChatMessages.Modify.NotExisting");
        String visit_success = cfg.getString("Settings.ChatMessages.Visit.Success");
        String visit_notexisting = cfg.getString("Settings.ChatMessages.Visit.NotExisting");

        if (c.getName().equalsIgnoreCase("home")) {
            if (s instanceof Player) {
                Player p = (Player) s;
                if (p.hasPermission("hp.home")) {

                    // args 1
                    if (args.length == 1) {

                        String name = args[0];
                        Home home = new Home(name, p);

                        if (home.isExisting()) {
                            home.visit(p);
                            p.sendMessage(Main.prefix + visit_success.replaceAll("%name%", name));
                        } else
                            p.sendMessage(Main.prefix + visit_notexisting.replaceAll("%name%", name));
                    }

                    // args 2
                    else if (args.length == 2) {

                        // create
                        if (args[0].equalsIgnoreCase("create")) {
                            String name = args[1];
                            Home home = new Home(name, p);
                            if (!home.isExisting()) {
                                home.create();
                                p.sendMessage(Main.prefix + create_success.replaceAll("%name%", name));
                            } else
                                p.sendMessage(Main.prefix + create_isexisting.replaceAll("%name%", name));
                        }

                        // delete
                        else if (args[0].equalsIgnoreCase("delete")) {
                            String name = args[1];
                            Home home = new Home(name, p);
                            if (home.isExisting()) {
                                if (cfg.getBoolean("Players." + p.getUniqueId() + ".Settings.DeleteProtection")) {
                                    Inventories.openConfirmation(p, player -> {
                                        home.delete();
                                        p.closeInventory();
                                    });
                                    Main.lastGui.put(p, null);
                                    Main.currentHome.put(p, home);
                                } else {
                                    home.delete();
                                    p.sendMessage(Main.prefix + delete_success.replaceAll("%name%", name));
                                }
                            } else p.sendMessage(Main.prefix + delete_notexisting.replaceAll("%name%", name));
                        }
                    }

                    // args 3
                    else if (args.length == 3) {

                        // modify
                        if (args[0].equalsIgnoreCase("modify")) {

                            String homeName = args[1];
                            Home home = new Home(homeName, p);
                            if (home.isExisting()) {

                                // icon
                                if (args[2].equalsIgnoreCase("icon")) {
                                    Main.lastGui.put(p, null);
                                    Main.currentHome.put(p, home);
                                    Inventories.openIconList(p);
                                }

                                // location
                                else if (args[2].equalsIgnoreCase("location")) {

                                    Main.lastGui.put(p, null);
                                    Main.currentHome.put(p, home);
                                    Inventories.openLocationGui(p, home);
                                }

                            } else p.sendMessage(Main.prefix + modify_notexisting.replaceAll("%name%", homeName));
                        }
                    }

                    // args 4
                    else if (args.length == 4) {

                        // modify
                        if (args[0].equalsIgnoreCase("modify")) {

                            String homeName = args[1];
                            Home home = new Home(homeName, p);
                            if (home.isExisting()) {

                                // name
                                if (args[2].equalsIgnoreCase("name")) {
                                    String name = args[3];
                                    home.setName(name);
                                    p.sendMessage(Main.prefix + modify_name_success.replaceAll("%name%", name));
                                }

                                // icon
                                else if (args[2].equalsIgnoreCase("icon")) {
                                    String name = args[3];
                                    try {
                                        ItemStack icon = new ItemStack(Material.getMaterial(name.toUpperCase()));
                                        home.setIcon(icon);
                                        p.sendMessage(Main.prefix);
                                    } catch (Exception e) {
                                        p.sendMessage(Main.prefix);
                                    }
                                }

                            } else p.sendMessage(Main.prefix + modify_notexisting.replaceAll("%name%", homeName));
                        }
                    }

                } else
                    p.sendMessage(Main.no_perm);
            }
        }

        return false;
    }
}
