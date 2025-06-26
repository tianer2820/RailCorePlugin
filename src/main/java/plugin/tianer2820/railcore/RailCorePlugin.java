package plugin.tianer2820.railcore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class RailCorePlugin extends JavaPlugin implements Listener {

    private Map<UUID, Map<Location, ResourcePoint>> resourcePoints = new HashMap<>();
    private File customStructuresFile;
    private FileConfiguration customStructuresConfig;

    private ResourcePointManager resourcePointManager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Initialize custom structures data
        setupCustomStructuresFile();
        loadCustomStructures();

        // Initialize managers
        resourcePointManager = new ResourcePointManager(this, resourcePoints);
        getServer().getPluginManager().registerEvents(resourcePointManager, this);
        RecipeManager.registerRecipes(this);

        // Start the periodic resource drop task
        resourcePointManager.startResourceDropTask();

        getLogger().info("RailCorePlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        saveCustomStructures();
        getLogger().info("RailCorePlugin has been disabled!");
    }

    /**
     * Sets up the custom_structures.yml file for persistent storage.
     */
    private void setupCustomStructuresFile() {
        customStructuresFile = new File(getDataFolder(), RailCoreConstants.STRUCTURE_DB_FILE);
        if (!customStructuresFile.exists()) {
            customStructuresFile.getParentFile().mkdirs();
            try {
                customStructuresFile.createNewFile();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not create " + RailCoreConstants.STRUCTURE_DB_FILE + " file!", e);
            }
        }
        customStructuresConfig = YamlConfiguration.loadConfiguration(customStructuresFile);
    }

    /**
     * Loads custom structure locations and their last drop times from custom_structures.yml.
     */
    private void loadCustomStructures() {
        if (customStructuresConfig.isConfigurationSection("resource_points")) {
            Set<String> worldKeys = customStructuresConfig.getConfigurationSection("resource_points").getKeys(false);
            for (String worldKey : worldKeys) {
                try {
                    UUID worldUUID = UUID.fromString(worldKey);
                    World world = Bukkit.getWorld(worldUUID);
                    if (world == null) {
                        getLogger().warning("World with UUID " + worldUUID + " not found during loading. Skipping structures for this world.");
                        continue;
                    }

                    Map<Location, ResourcePoint> worldStructures = new HashMap<>();
                    resourcePoints.put(worldUUID, worldStructures);

                    Set<String> locationKeys = customStructuresConfig.getConfigurationSection("resource_points." + worldKey).getKeys(false);
                    for (String locKey : locationKeys) {
                        try {
                            String[] parts = locKey.split("_");
                            if (parts.length == 3) {
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                int z = Integer.parseInt(parts[2]);
                                Location location = new Location(world, x, y, z);
                                long lastDropTime = customStructuresConfig.getLong("resource_points." + worldKey + "." + locKey + ".lastDropTime", 0L);
                                String resourceTypeStr = customStructuresConfig.getString("resource_points." + worldKey + "." + locKey + ".resourceType", "STONE");
                                String lastItemUUIDStr = customStructuresConfig.getString("resource_points." + worldKey + "." + locKey + ".lastItemEntityUUID", null);
                                Material resourceType = Material.getMaterial(resourceTypeStr);
                                ResourcePoint rp = new ResourcePoint(location, lastDropTime, resourceType);
                                if (lastItemUUIDStr != null) {
                                    try {
                                        rp.setLastItemEntityUUID(UUID.fromString(lastItemUUIDStr));
                                    } catch (IllegalArgumentException ignored) {}
                                }
                                worldStructures.put(location, rp);
                            } else {
                                getLogger().warning("Malformed location key: " + locKey + " in " + worldKey);
                            }
                        } catch (NumberFormatException e) {
                            getLogger().warning("Invalid number format in location key: " + locKey + " in " + worldKey);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID format for world key: " + worldKey);
                }
            }
        }
        getLogger().info("Loaded " + resourcePoints.values().stream().mapToLong(Map::size).sum() + " custom structures.");
    }


    /**
     * Saves custom structure locations and their last drop times to custom_structures.yml.
     */
    public void saveCustomStructures() {
        customStructuresConfig = new YamlConfiguration(); // Clear previous config to prevent old data
        for (Map.Entry<UUID, Map<Location, ResourcePoint>> worldEntry : resourcePoints.entrySet()) {
            UUID worldUUID = worldEntry.getKey();
            Map<Location, ResourcePoint> locations = worldEntry.getValue();

            for (Map.Entry<Location, ResourcePoint> locEntry : locations.entrySet()) {
                Location loc = locEntry.getKey();
                ResourcePoint rp = locEntry.getValue();
                // Store location as X_Y_Z string
                String locKey = loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
                customStructuresConfig.set("resource_points." + worldUUID.toString() + "." + locKey + ".lastDropTime", rp.getLastDropTime());
                customStructuresConfig.set("resource_points." + worldUUID.toString() + "." + locKey + ".resourceType", rp.getResourceType().name());
                if (rp.getLastItemEntityUUID() != null) {
                    customStructuresConfig.set("resource_points." + worldUUID.toString() + "." + locKey + ".lastItemEntityUUID", rp.getLastItemEntityUUID().toString());
                } else {
                    customStructuresConfig.set("resource_points." + worldUUID.toString() + "." + locKey + ".lastItemEntityUUID", null);
                }
            }
        }

        try {
            customStructuresConfig.save(customStructuresFile);
            getLogger().info("Saved " + resourcePoints.values().stream().mapToLong(Map::size).sum() + " custom structures.");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save custom_structures.yml file!", e);
        }
    }

}
