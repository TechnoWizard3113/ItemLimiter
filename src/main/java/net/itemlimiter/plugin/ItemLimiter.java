package net.itemlimiter.plugin;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

public class ItemLimiter extends JavaPlugin {

    private final Map<Material, Integer> limits = new EnumMap<>(Material.class);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLimits();

        getServer().getPluginManager().registerEvents(
                new InventoryListener(this),
                this
        );

        getLogger().info("ItemLimiter enabled");
    }

    public void loadLimits() {
        limits.clear();

        if (!getConfig().isConfigurationSection("limits")) return;

        for (String key : getConfig().getConfigurationSection("limits").getKeys(false)) {
            try {
                Material mat = Material.valueOf(key.toUpperCase());
                int max = getConfig().getInt("limits." + key);
                limits.put(mat, max);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public Map<Material, Integer> getLimits() {
        return limits;
    }

    public boolean isLimited(Material material) {
        return limits.containsKey(material);
    }

    public int getLimit(Material material) {
        return limits.getOrDefault(material, Integer.MAX_VALUE);
    }
}