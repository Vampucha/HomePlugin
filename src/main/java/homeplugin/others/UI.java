package homeplugin.others;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.function.Consumer;

public class UI {

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final int size;
    private final ArrayList<Item> items;
    private final Events events = new Events();
    private final boolean preventClose;

    private Inventory inv;

    public UI(Plugin plugin, Player player, String title, int size, ArrayList<Item> items, boolean preventClose) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = size;
        this.items = items;
        this.preventClose = preventClose;
        openInventory();
    }

    private class Events implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory() == inv) {

                if (e.getCurrentItem() != null) {
                    for (Item item : items) {

                        Player p = (Player) e.getWhoClicked();
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals(item.getName())) {

                            // left
                            if (e.getClick() == ClickType.LEFT && item.getLeftClick() != null) {
                                item.getLeftClick().accept(p);
                            }

                            // right
                            else if (e.getClick() == ClickType.RIGHT && item.getRightClick() != null) {
                                item.getRightClick().accept(p);
                            }

                            // middle
                            else if (e.getClick() == ClickType.MIDDLE && item.getMiddleClick() != null) {
                                item.getMiddleClick().accept(p);
                            }

                            // other clicks
                            else {
                                if (item.getClickAction() != null) item.getClickAction().accept(p);
                                else e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (e.getInventory() == inv) {
                closeInventory();
                if (UI.this.preventClose) Bukkit.getScheduler().runTask(UI.this.plugin, UI.this::openInventory);
            }
        }
    }

    public static class Page {

        private Plugin plugin;
        private String title;
        private int size;
        private final ArrayList<Item> items = new ArrayList<>();
        private boolean preventClose;
        private Player holder;

        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void addItem(Item item) {
            items.add(item);
        }

        public void preventClose() {
            this.preventClose = true;
        }

        public void setHolder(Player holder) {
            this.holder = holder;
        }

        public void open(Player p) {
            new UI(plugin, p, title, size, items, preventClose);
        }


        public ArrayList<Item> getItems() {
            return items;
        }

        public String getTitle() {
            return title;
        }

        public Player getHolder() {
            return holder;
        }
    }

    public static class Item {

        String name;
        ArrayList<String> lore;
        Material material;
        Consumer<Player> leftClickAction;
        Consumer<Player> rightClickAction;
        Consumer<Player> middleClickAction;
        Consumer<Player> clickAction;
        int slot;

        public void setName(String name) {
            this.name = name;
        }

        public void setLore(ArrayList<String> lore) {
            this.lore = lore;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public void onLeftClick(Consumer<Player> leftClickAction) {
            this.leftClickAction = leftClickAction;
        }

        public void onRightClick(Consumer<Player> rightClickAction) {
            this.rightClickAction = rightClickAction;
        }

        public void onMiddleClick(Consumer<Player> middleClickAction) {
            this.middleClickAction = middleClickAction;
        }

        public void onClick(Consumer<Player> clickAction) {
            this.clickAction = clickAction;
        }


        public String getName() {
            return name;
        }

        public ArrayList<String> getLore() {
            return lore;
        }

        public Material getMaterial() {
            return material;
        }

        public int getSlot() {
            return slot;
        }

        public Consumer<Player> getLeftClick() {
            return leftClickAction;
        }

        public Consumer<Player> getRightClick() {
            return rightClickAction;
        }

        public Consumer<Player> getMiddleClick() {
            return middleClickAction;
        }

        public Consumer<Player> getClickAction() {
            return clickAction;
        }
    }

    private void openInventory() {

        inv = Bukkit.createInventory(player, size, title);

        for (Item item : items) {

            ItemStack itemStack = new ItemStack(item.getMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(item.getName());
            itemMeta.setLore(item.getLore());
            itemStack.setItemMeta(itemMeta);

            inv.setItem(item.getSlot(), itemStack);
        }

        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(events, plugin);
    }

    private void closeInventory() {
        HandlerList.unregisterAll(UI.this.events);
    }
}
