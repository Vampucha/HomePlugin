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

    public enum HomeType {
        PRIVATE, PASSWORD, PUBLIC
    }

    public Home(String homeName, Player homeOwner) {
        name = homeName;
        owner = homeOwner;
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
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + id + ".Type", getType().toString().toLowerCase());
        Main.getPlugin().saveConfig();
    }

    public void delete() {
        ArrayList<String> favorites = new ArrayList<>(cfg.getStringList("Players." + owner.getUniqueId() + ".FavoriteHomes"));
        favorites.remove(name);
        cfg.set("Players." + owner.getUniqueId() + ".FavoriteHomes", favorites);
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID(), null);
        Main.getPlugin().saveConfig();
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


    public void setIcon(ItemStack icon) {
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Icon", icon);
        Main.getPlugin().saveConfig();
    }

    public void setLocation(Location location) {
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Location", location);
        Main.getPlugin().saveConfig();
    }

    public void setName(String name) {
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Name", name);
        ArrayList<String> favorites = new ArrayList<>(cfg.getStringList("Players." + owner.getUniqueId() + ".FavoriteHomes"));
        if (favorites.contains(this.name)) {
            favorites.remove(this.name);
            favorites.add(name);
            cfg.set("Players." + owner.getUniqueId() + ".FavoriteHomes", favorites);
        }
        Main.getPlugin().saveConfig();
    }

    public void setType(HomeType type) {
        String homeType = type.toString().toLowerCase();
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Type", homeType);
        Main.getPlugin().saveConfig();
    }

    public void setPassword(String password) {
        cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Password", password);
        Main.getPlugin().saveConfig();
    }

    public void addJoinAction(JoinAction action) {
        if (!getJoinActions().contains(action.getName())) {
            ArrayList<String> joinActions = getJoinActions();
            joinActions.add(action.getName());
            cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".JoinActions", joinActions);
            Main.getPlugin().saveConfig();
        }
    }

    public void removeJoinAction(JoinAction action) {
        if (getJoinActions().contains(action.getName())) {
            ArrayList<String> joinActions = getJoinActions();
            joinActions.remove(action.getName());
            cfg.set("Players." + owner.getUniqueId() + ".Homes." + getID() + ".JoinActions", joinActions);
            Main.getPlugin().saveConfig();
        }
    }


    public ItemStack getIcon() {

        ItemStack icon = new ItemStack(Material.GRASS_BLOCK);

        try {
            icon = cfg.getItemStack("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Icon");
        } catch (Exception ignored) {
        }
        return icon;
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

    public ArrayList<String> getJoinActions() {
        return new ArrayList<>(cfg.getStringList("Players." + owner.getUniqueId() + ".Homes." + getID() + ".JoinActions"));
    }

    public Location getLocation() {
        return cfg.getLocation("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Location");
    }

    public String getName() {
        return name;
    }

    public Player getOwner() {
        return owner;
    }

    public String getPassword() {
        return cfg.getString("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Password");
    }

    public HomeType getType() {
        HomeType type = HomeType.PUBLIC;
        if (cfg.getString("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Type") != null) {
            switch (cfg.getString("Players." + owner.getUniqueId() + ".Homes." + getID() + ".Type")) {
                case "private" -> type = HomeType.PRIVATE;
                case "password" -> type = HomeType.PASSWORD;
                case "public" -> type = HomeType.PUBLIC;
            }
        }
        return type;
    }


    public static class JoinAction {

        String name;
        ItemStack icon;

        public JoinAction(String name) {
            this.name = name;
        }

        public void setIcon(ItemStack icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public ItemStack getIcon() {
            return icon;
        }
    }
}
