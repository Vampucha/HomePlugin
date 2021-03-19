package homeplugin.cmds;

import homeplugin.others.ChatMessage;
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

                        Main.search.get(p).clear();
                        Main.pageHistory.get(p).clear();
                        Main.pageHistory.get(p).add(null);
                        Main.page.get(p).clear();
                        Inventories.openHomeList(p, p);

                    } else p.sendMessage(Main.no_perm);
                } else if (args.length == 1) {

                    // reload
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (p.hasPermission("hp.reload")) {
                            Main.getPlugin().reloadConfig();
                            ChatMessage.sendMessage(p, "§aYou successfully reloaded the HomePlugin", true);
                        } else ChatMessage.sendMessage(p, Main.no_perm, false);
                    }

                    // settings
                    else if (args[0].equalsIgnoreCase("settings")) {
                        Main.pageHistory.get(p).clear();
                        Main.pageHistory.get(p).add(null);
                        Inventories.openSettings(p);

                    } else {
                        if (p.hasPermission("hp.homes.others")) {
                            Player target = Bukkit.getPlayer(args[0]);
                            if (Bukkit.getOnlinePlayers().contains(target)) {

                                Main.search.get(p).clear();
                                Main.pageHistory.get(p).clear();
                                Main.pageHistory.get(p).add(null);
                                Main.page.get(p).clear();
                                Inventories.openHomeList(p, target);

                            } else ChatMessage.sendMessage(p, "§6" + args[0] + " §cis not online!", true);
                        } else ChatMessage.sendMessage(p, Main.no_perm, false);
                    }
                }
            }
        }

        return false;

    }
}
