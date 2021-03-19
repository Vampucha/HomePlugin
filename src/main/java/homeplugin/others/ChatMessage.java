package homeplugin.others;

import homeplugin.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ChatMessage {

    private static String prefix = Main.prefix;

    // ONE PLAYER
    public static void sendMessage(Player player, String message, Boolean usePrefix) {
        if (!usePrefix) prefix = "";
        player.sendMessage(prefix + message);
    }

    // MULTIPLE PLAYERS
    public static void sendMessage(ArrayList<Player> players, String message, Boolean usePrefix) {
        if (!usePrefix) prefix = "";
        for (Player player : players) {
            player.sendMessage(prefix + message);
        }
    }

    // ALL PLAYERS
    public static void sendMessage(String message, Boolean usePrefix) {
        if (!usePrefix) prefix = "";
        Bukkit.broadcastMessage(prefix + message);
    }
}
