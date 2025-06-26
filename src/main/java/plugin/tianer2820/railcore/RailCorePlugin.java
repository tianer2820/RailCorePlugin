package plugin.tianer2820.railcore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class RailCorePlugin extends JavaPlugin implements Listener {

    // Store custom structure locations and their last resource drop time
    // Key: World UUID, Value: Map of Location (block coords) to Long (last drop time in milliseconds)
    private Map<UUID, Map<Location, Long>> customStructures = new HashMap<>();
    private Random random = new Random();

    // Configuration file for persistent storage of structure locations
    private File customStructuresFile;
    private FileConfiguration customStructuresConfig;

    @Override
    public void onEnable() {
        // Register event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Initialize custom structures data
        setupCustomStructuresFile();
        loadCustomStructures();

        // Register custom recipes
        registerRecipes();

        // Start the periodic resource drop task
        startResourceDropTask();

        getLogger().info("RailSystemPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save custom structures data before disabling
        saveCustomStructures();
        getLogger().info("RailSystemPlugin has been disabled!");
    }

    /**
     * Sets up the custom_structures.yml file for persistent storage.
     */
    private void setupCustomStructuresFile() {
        customStructuresFile = new File(getDataFolder(), "custom_structures.yml");
        if (!customStructuresFile.exists()) {
            customStructuresFile.getParentFile().mkdirs();
            try {
                customStructuresFile.createNewFile();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not create custom_structures.yml file!", e);
            }
        }
        customStructuresConfig = YamlConfiguration.loadConfiguration(customStructuresFile);
    }

    /**
     * Loads custom structure locations and their last drop times from custom_structures.yml.
     */
    private void loadCustomStructures() {
        if (customStructuresConfig.isConfigurationSection("structures")) {
            Set<String> worldKeys = customStructuresConfig.getConfigurationSection("structures").getKeys(false);
            for (String worldKey : worldKeys) {
                try {
                    UUID worldUUID = UUID.fromString(worldKey);
                    World world = Bukkit.getWorld(worldUUID);
                    if (world == null) {
                        getLogger().warning("World with UUID " + worldUUID + " not found during loading. Skipping structures for this world.");
                        continue;
                    }

                    Map<Location, Long> worldStructures = new HashMap<>();
                    customStructures.put(worldUUID, worldStructures);

                    Set<String> locationKeys = customStructuresConfig.getConfigurationSection("structures." + worldKey).getKeys(false);
                    for (String locKey : locationKeys) {
                        try {
                            String[] parts = locKey.split("_");
                            if (parts.length == 3) {
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                int z = Integer.parseInt(parts[2]);
                                Location location = new Location(world, x, y, z);
                                long lastDropTime = customStructuresConfig.getLong("structures." + worldKey + "." + locKey);
                                worldStructures.put(location, lastDropTime);
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
        getLogger().info("Loaded " + customStructures.values().stream().mapToLong(Map::size).sum() + " custom structures.");
    }


    /**
     * Saves custom structure locations and their last drop times to custom_structures.yml.
     */
    private void saveCustomStructures() {
        customStructuresConfig = new YamlConfiguration(); // Clear previous config to prevent old data
        for (Map.Entry<UUID, Map<Location, Long>> worldEntry : customStructures.entrySet()) {
            UUID worldUUID = worldEntry.getKey();
            Map<Location, Long> locations = worldEntry.getValue();

            for (Map.Entry<Location, Long> locEntry : locations.entrySet()) {
                Location loc = locEntry.getKey();
                Long lastDropTime = locEntry.getValue();
                // Store location as X_Y_Z string
                String locKey = loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
                customStructuresConfig.set("structures." + worldUUID.toString() + "." + locKey, lastDropTime);
            }
        }

        try {
            customStructuresConfig.save(customStructuresFile);
            getLogger().info("Saved " + customStructures.values().stream().mapToLong(Map::size).sum() + " custom structures.");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save custom_structures.yml file!", e);
        }
    }


    private void registerRecipes() {
        /**
         * Rail Dup Recipes
         */
        Material[] railTypes = {
                Material.RAIL,
                Material.POWERED_RAIL,
                Material.DETECTOR_RAIL,
                Material.ACTIVATOR_RAIL
        };

        for (Material railType : railTypes) {
            // Create the result item stack (2 rails of the same type)
            ItemStack result = new ItemStack(railType, 2);

            // Create a unique NamespacedKey for the recipe
            // NamespacedKey requires a plugin instance and a unique string identifier
            NamespacedKey key = new NamespacedKey(this, railType.name().toLowerCase() + "_duplication");

            // Create a ShapedRecipe
            ShapedRecipe recipe = new ShapedRecipe(key, result);

            // Define the crafting pattern (a single item in the center)
            recipe.shape(" ", "R", " "); // R represents the rail item
            recipe.setIngredient('R', railType);

            // Add the recipe to the server
            Bukkit.addRecipe(recipe);
            getLogger().info("Registered recipe for " + railType.name() + " duplication.");
        }

        // Tool Enchantment Recipes

        Material[] tools = {
                Material.WOODEN_PICKAXE,
                Material.STONE_PICKAXE,
                Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE,
                Material.DIAMOND_PICKAXE,
                Material.NETHERITE_PICKAXE,

                Material.WOODEN_AXE,
                Material.STONE_AXE,
                Material.IRON_AXE,
                Material.GOLDEN_AXE,
                Material.DIAMOND_AXE,
                Material.NETHERITE_AXE,

                Material.WOODEN_SHOVEL,
                Material.STONE_SHOVEL,
                Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL,
                Material.DIAMOND_SHOVEL,
                Material.NETHERITE_SHOVEL,

                Material.WOODEN_HOE,
                Material.STONE_HOE,
                Material.IRON_HOE,
                Material.GOLDEN_HOE,
                Material.DIAMOND_HOE,
                Material.NETHERITE_HOE
        };


        // Helper function to register recipes for a given array of materials
        for (Material toolMaterial : tools) {
            // Create the result item stack (1 enchanted tool)
            ItemStack result = new ItemStack(toolMaterial);
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true); // Enchant with Efficiency
                result.setItemMeta(meta);
            }

            // Create a unique NamespacedKey for the recipe
            NamespacedKey key = new NamespacedKey(this, toolMaterial.name().toLowerCase() + "_efficiency_crafting");

            // Create a ShapedRecipe
            ShapedRecipe recipe = new ShapedRecipe(key, result);

            // Define the crafting pattern (2x2 grid of the same tool)
            recipe.shape("TT", "TT"); // T represents the tool item
            recipe.setIngredient('T', toolMaterial);

            // Add the recipe to the server
            Bukkit.addRecipe(recipe);
            getLogger().info("Registered recipe for " + toolMaterial.name() + " efficiency crafting.");
        }
    }





    /**
     * Listens for chunk population events to generate custom structures.
     */
    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        // Only generate in the overworld
        if (event.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        // Adjust this probability as needed (e.g., 0.001 means 1 in 1000 chunks)
        double generationChance = 0.05;

        if (random.nextDouble() < generationChance) {
            // Get a random position within the chunk
            int x = event.getChunk().getX() * 16 + random.nextInt(16);
            int z = event.getChunk().getZ() * 16 + random.nextInt(16);

            // Find the highest solid block at this X, Z coordinate
            World world = event.getWorld();
            int y = world.getHighestBlockYAt(x, z);

            // Ensure there's enough space above and it's not water/lava
            if (y > world.getMinHeight() && y + 5 < world.getMaxHeight() &&
                !world.getBlockAt(x, y, z).isLiquid()) {
                
                Location structureBaseLocation = new Location(world, x, y + 1, z); // Place above ground

                // Only generate if there isn't already a structure nearby
                if (!isStructureTooClose(structureBaseLocation)) {
                    buildCustomStructure(structureBaseLocation);
                    // Store the structure location and initial last drop time (now)
                    customStructures.computeIfAbsent(world.getUID(), k -> new HashMap<>())
                                    .put(structureBaseLocation, System.currentTimeMillis());
                    getLogger().info("Generated custom structure at X:" + x + ", Y:" + (y + 1) + ", Z:" + z + " in world " + world.getName());
                }
            }
        }
    }

    /**
     * Checks if there's already a custom structure too close to the given location.
     * This prevents structures from spawning on top of each other.
     *
     * @param newLocation The proposed location for a new structure.
     * @return true if a structure is too close, false otherwise.
     */
    private boolean isStructureTooClose(Location newLocation) {
        // Define the minimum distance between structures
        double minDistanceSquared = 50 * 50;

        Map<Location, Long> worldStructures = customStructures.get(newLocation.getWorld().getUID());
        if (worldStructures != null) {
            for (Location existingLoc : worldStructures.keySet()) {
                if (existingLoc.distanceSquared(newLocation) < minDistanceSquared) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Builds a simple custom structure at the given base location.
     * For this example: a 3x3 base of stone bricks, a pillar of stone, and a chest on top.
     *
     * @param baseLocation The bottom-center location for the structure.
     */
    private void buildCustomStructure(Location baseLocation) {
        World world = baseLocation.getWorld();
        int x = baseLocation.getBlockX();
        int y = baseLocation.getBlockY();
        int z = baseLocation.getBlockZ();

        // Build a 3x3 stone brick base
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                world.getBlockAt(x + i, y - 1, z + j).setType(Material.STONE_BRICKS);
            }
        }

        // Build a simple pillar upwards
        for (int i = 0; i < 3; i++) {
            world.getBlockAt(x, y + i, z).setType(Material.STONE);
        }

        // Place a chest on top
        Block chestBlock = world.getBlockAt(x, y + 3, z);
        chestBlock.setType(Material.CHEST);

        // Optionally, add some initial items to the chest
        if (chestBlock.getState() instanceof Chest) {
            Chest chest = (Chest) chestBlock.getState();
            chest.getInventory().addItem(new ItemStack(Material.IRON_INGOT, random.nextInt(3) + 1));
            chest.getInventory().addItem(new ItemStack(Material.COAL, random.nextInt(5) + 2));
        }
    }


    /**
     * Starts a BukkitRunnable task that periodically checks custom structures
     * and drops random resources if enough time has passed.
     */
    private void startResourceDropTask() {
        long checkIntervalTicks = 20; // Check every sec
        long dropCooldownMillis = 1000 * 10; // Drop resources every 10 secs

        // Define a list of random resources to drop
        Material[] possibleDrops = {
                Material.IRON_INGOT,
                Material.GOLD_INGOT,
                Material.DIAMOND,
                Material.EMERALD,
                Material.LAPIS_LAZULI,
                Material.REDSTONE,
                Material.RAW_IRON,
                Material.RAW_GOLD,
                Material.COAL,
                Material.COPPER_INGOT
        };

        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                getLogger().info("Checking custom structures for resource drops...");

                // Iterate over a copy to avoid ConcurrentModificationException if a structure is removed
                // (though not handled in this example, good practice for iterators)
                Map<UUID, Map<Location, Long>> structuresCopy = new HashMap<>(customStructures);

                structuresCopy.forEach((worldUUID, worldStructures) -> {
                    World world = Bukkit.getWorld(worldUUID);
                    if (world == null) {
                        // World might have been unloaded, skip
                        getLogger().warning("World with UUID " + worldUUID + " not loaded. Skipping structure checks.");
                        return;
                    }

                    // Iterate over a copy of locations for current world
                    new HashMap<>(worldStructures).forEach((location, lastDropTime) -> {
                        if (currentTime - lastDropTime >= dropCooldownMillis) {
                            // Time to drop resources!
                            Block block = location.getBlock();
                            if (block.getType() == Material.CHEST) {
                                if (block.getState() instanceof Chest) {
                                    Chest chest = (Chest) block.getState();

                                    // Drop 1-3 random items into the chest
                                    int numItemsToDrop = random.nextInt(3) + 1;
                                    for (int i = 0; i < numItemsToDrop; i++) {
                                        Material dropMaterial = possibleDrops[random.nextInt(possibleDrops.length)];
                                        int amount = random.nextInt(3) + 1; // 1 to 3 items of that type
                                        chest.getInventory().addItem(new ItemStack(dropMaterial, amount));
                                    }
                                    getLogger().info("Dropped resources at custom structure: " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());

                                    // Update the last drop time
                                    worldStructures.put(location, currentTime);
                                } else {
                                    getLogger().warning("Block at " + location + " is not a chest, but registered as a structure. Removing.");
                                    worldStructures.remove(location); // Remove invalid structure
                                }
                            } else {
                                getLogger().warning("Block at " + location + " is not a chest, but registered as a structure. Removing.");
                                worldStructures.remove(location); // Remove invalid structure
                            }
                        }
                    });
                });

                // Save structures after each check (can be optimized to save less frequently)
                saveCustomStructures();
            }
        }.runTaskTimer(this, checkIntervalTicks, checkIntervalTicks); // Delay, then repeat every interval
    }
}
