package homeplugin.others;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import homeplugin.main.Main;

public class Home {

    FileConfiguration cfg = Main.getPlugin().getConfig();

    String name;
    Player owner;

    public Home(String homeName, Player homeOwner) {
        name = homeName;
        owner = homeOwner;
    }

    public boolean isExisting() {

        boolean isExisting = false;
        if (cfg.contains("Players." + owner.getUniqueId() + ".Homes")) {
            ArrayList<String> names = new ArrayList<>();

            for (String id : cfg.getConfigurationSection("Players." + owner.getUniqueId() + ".Homes").getKeys(false)) {
                names.add(cfg.getString("Players." + owner.getUniqueId() + ".Homes." + id + ".Name"));
            }

            if (names.contains(name))
                isExisting = true;
        }
        return isExisting;
    }

    public void visit(Player visitor) {

        visitor.teleport(getLocation());
    }

    public void setName(String name) {
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Name", name);
        ArrayList<String> favorites = new ArrayList<>(cfg.getStringList("Players." + owner.getUniqueId() + ".FavoriteHomes"));
        if(favorites.contains(this.name)) {
            favorites.remove(this.name);
            favorites.add(name);
            cfg.set("Players." + owner.getUniqueId() + ".FavoriteHomes", favorites);
        }
        Main.getPlugin().saveConfig();
    }

    public String getName() {
        return name;
    }

    public void setIcon(ItemStack icon) {
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Icon", icon);
        Main.getPlugin().saveConfig();
    }

    public ItemStack getIcon() {

        ItemStack icon = new ItemStack(Material.GRASS_BLOCK);

        try {
            icon = cfg.getItemStack("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Icon");
        } catch (Exception ignored) {
        }
        return icon;
    }

    public void setLocation(Location loc) {
        cfg.set("Players." + owner.getLocation() + ".Homes." + getID() + ".Location", loc);
        Main.getPlugin().saveConfig();
    }

    public Location getLocation() {
        return cfg.getLocation("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Location");
    }

    private String findID() {

        String id = "";
        ArrayList<String> homes = new ArrayList<>();
        if (cfg.contains("Players." + owner.getUniqueId() + ".Homes"))
            homes.addAll(cfg.getConfigurationSection("Players." + owner.getUniqueId() + ".Homes").getKeys(false));

        for (int i = 0; i < homes.size() + 1; i++) {
            String home = "Home" + i;
            if (!homes.contains(home)) {
                id = home;
                break;
            }
        }
        return id;
    }

    private String getID() {

        String id = "";
        ArrayList<String> homes = new ArrayList<>();
        if (cfg.contains("Players." + owner.getUniqueId() + ".Homes"))
            homes.addAll(cfg.getConfigurationSection("Players." + owner.getUniqueId() + ".Homes").getKeys(false));

        for (String home : homes) {
            if (cfg.getString("Players." + owner.getUniqueId() + ".Homes." + home + ".Name").equals(name)) {
                id = home;
                break;
            }
        }
        return id;
    }

    public void create() {

        ItemStack icon = new ItemStack(Material.GRASS_BLOCK);

        // normal
        if (owner.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            icon = new ItemStack(Material.GRASS_BLOCK);
        }

        // nether
        else if (owner.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            icon = new ItemStack(Material.NETHERRACK);
        }

        // the_end
        else if (owner.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            icon = new ItemStack(Material.END_STONE);
        }

        Location loc = owner.getLocation();
        if (!cfg.getBoolean("Players." + owner.getUniqueId() + ".Settings.Orientation")) {
            loc.setYaw(0);
            loc.setPitch(0);
        }

        String id = findID();
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + id + ".Name", name);
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + id + ".Icon", icon);
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + id + ".Location", loc);
        Main.getPlugin().saveConfig();
    }

    public void delete() {
        ArrayList<String> favorites = new ArrayList<>(cfg.getStringList("Players." + owner.getUniqueId() + ".FavoriteHomes"));
        favorites.remove(name);
        cfg.set("Players." + owner.getUniqueId() + ".FavoriteHomes", favorites);
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID(), null);
        Main.getPlugin().saveConfig();
    }
}
