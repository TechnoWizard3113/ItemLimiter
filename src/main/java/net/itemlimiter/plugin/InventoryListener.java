package net.itemlimiter.plugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final ItemLimiter plugin;

    public InventoryListener(ItemLimiter plugin) {
        this.plugin = plugin;
    }

    /* ---------------------------
       CORE COUNTING
    ---------------------------- */

    private int count(Player player, Material mat) {
        int total = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private int spaceLeft(Player player, Material mat) {
        return plugin.getLimit(mat) - count(player, mat);
    }

    private boolean bypass(Player p) {
        return p.hasPermission("itemlimiter.bypass");
    }

    /* ---------------------------
       PICKUP (GROUND)
    ---------------------------- */

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;
        if (bypass(player)) return;

        ItemStack stack = event.getItem().getItemStack();
        Material mat = stack.getType();

        if (!plugin.isLimited(mat)) return;

        int space = spaceLeft(player, mat);
        if (space <= 0) {
            event.setCancelled(true);
            return;
        }

        if (stack.getAmount() > space) {
            stack.setAmount(stack.getAmount() - space);
            event.getItem().setItemStack(stack);
        }
    }

    /* ---------------------------
       INVENTORY CLICK (CHESTS, SHIFT CLICK, NUMBER KEYS)
    ---------------------------- */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (bypass(player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        handleItem(player, cursor);
        handleItem(player, current);
    }

    /* ---------------------------
       DRAGGING ITEMS
    ---------------------------- */

    @EventHandler
    public void onDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (bypass(player)) return;

        ItemStack item = event.getOldCursor();
        handleItem(player, item);
    }

    /* ---------------------------
       CRAFTING OUTPUT
    ---------------------------- */

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {

        ItemStack result = event.getInventory().getResult();
        if (result == null) return;

        Material mat = result.getType();
        if (!plugin.isLimited(mat)) return;

        Player player = (Player) event.getView().getPlayer();
        if (bypass(player)) return;

        int space = spaceLeft(player, mat);

        if (space <= 0) {
            event.getInventory().setResult(null);
            return;
        }

        if (result.getAmount() > space) {
            result.setAmount(space);
            event.getInventory().setResult(result);
        }
    }

    /* ---------------------------
       HOPPER INTO PLAYER INVENTORY
    ---------------------------- */

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {

        if (!(event.getDestination().getHolder() instanceof Player player)) return;
        if (bypass(player)) return;

        ItemStack item = event.getItem();
        handleItem(player, item);
    }

    /* ---------------------------
       LOGIN CLEANUP SAFETY
    ---------------------------- */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (bypass(player)) return;

        for (ItemStack item : player.getInventory().getContents()) {
            handleItem(player, item);
        }
    }

    /* ---------------------------
       CORE LIMIT ENFORCER
    ---------------------------- */

    private void handleItem(Player player, ItemStack item) {

        if (item == null) return;

        Material mat = item.getType();

        if (!plugin.isLimited(mat)) return;

        int limit = plugin.getLimit(mat);
        int current = count(player, mat);

        if (current >= limit) {
            item.setAmount(0);
            return;
        }

        int space = limit - current;

        if (item.getAmount() > space) {
            item.setAmount(space);
        }
    }
}