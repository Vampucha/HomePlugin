package homeplugin.cmds;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import homeplugin.main.Main;
import homeplugin.others.Inventories;

public class HomesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command c, String arg2, String[] args) {

        if (c.getName().equalsIgnoreCase("homes")) {
            if (s instanceof Player) {
                Player p = (Player) s;

                if (args.length == 0) {

                    if (p.hasPermission("hp.homes")) {

                        Main.page.put(p, 1);
                        Main.previousPage.get(p).clear();
                        Main.previousPage.get(p).add(null);
                        Inventories.openHomeList(p, p);

                    } else p.sendMessage(Main.no_perm);
                } else if (args.length == 1) {

                    // reload
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (p.hasPermission("hp.reload")) {
                            Main.getPlugin().reloadConfig();
                            p.sendMessage(Main.prefix + "§aYou successfully reloaded the HomePlugin");
                        } else p.sendMessage(Main.no_perm);
                    }

                    // settings
                    else if (args[0].equalsIgnoreCase("settings")) {
                        Main.lastGui.put(p, null);
                        Inventories.openSettings(p);

                    } else {
                        if (p.hasPermission("hp.homes.others")) {
                            Player target = Bukkit.getPlayer(args[0]);
                            if (Bukkit.getOnlinePlayers().contains(target)) {

                                Main.page.put(p, 1);
                                Main.previousPage.get(p).clear();
                                Main.previousPage.get(p).add(null);
                                Inventories.openHomeList(p, target);

                            } else p.sendMessage(Main.prefix + "§6" + args[0] + " §cis not online!");
                        } else p.sendMessage(Main.no_perm);
                    }
                }
            }
        }

        return false;

    }
}
