package plugin.tianer2820.railcore;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class ResourcePointManager implements Listener {
    private final Map<UUID, Map<Location, Long>> customStructures;
    private final Random random = new Random();
    private final RailCorePlugin plugin;

    public ResourcePointManager(RailCorePlugin plugin, Map<UUID, Map<Location, Long>> customStructures) {
        this.plugin = plugin;
        this.customStructures = customStructures;
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

        if (random.nextDouble() < RailCoreConstants.GENERATE_CHANCE) {
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
                    Location centerLocation = buildCustomStructure(structureBaseLocation);
                    // Store the structure location and initial last drop time (now)
                    customStructures.computeIfAbsent(world.getUID(), k -> new HashMap<>())
                                    .put(centerLocation, System.currentTimeMillis());
                    plugin.getLogger().info("Generated custom structure at X:" + x + ", Y:" + (y + 1) + ", Z:" + z + " in world " + world.getName());
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
        Map<Location, Long> worldStructures = customStructures.get(newLocation.getWorld().getUID());
        if (worldStructures != null) {
            for (Location existingLoc : worldStructures.keySet()) {
                if (existingLoc.distanceSquared(newLocation) < RailCoreConstants.MIN_DISTANCE_SQ) {
                    return true;
                }
            }
        }
        return false;
    }


    public Location buildCustomStructure(Location baseLocation) {
        World world = baseLocation.getWorld();
        int x = baseLocation.getBlockX();
        int y = baseLocation.getBlockY();
        int z = baseLocation.getBlockZ();

        // Build a 5x5 stone brick base
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                world.getBlockAt(x + i, y, z + j).setType(Material.STONE_BRICKS);
            }
        }

        Location centerLocation = new Location(world, x, y + RailCoreConstants.RESOURCE_POINT_RADIUS, z);
        x = centerLocation.getBlockX();
        y = centerLocation.getBlockY();
        z = centerLocation.getBlockZ();

        // Build three intersecting rings (X, Y, Z axes)
        int radius = RailCoreConstants.RESOURCE_POINT_RADIUS;
        Material ringMaterial = RailCoreConstants.RESOURCE_POINT_RING_MATERIAL;
        // X ring (YZ plane)
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dy * dy + dz * dz - radius * radius) <= 1.5) {
                    world.getBlockAt(x, y + dy, z + dz).setType(ringMaterial);
                }
            }
        }
        // Y ring (XZ plane)
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx * dx + dz * dz - radius * radius) <= 1.5) {
                    world.getBlockAt(x + dx, y, z + dz).setType(ringMaterial);
                }
            }
        }
        // Z ring (XY plane)
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (Math.abs(dx * dx + dy * dy - radius * radius) <= 1.5) {
                    world.getBlockAt(x + dx, y + dy, z).setType(ringMaterial);
                }
            }
        }
        // Place the center block (fluorite)
        world.getBlockAt(x, y, z).setType(RailCoreConstants.RESOURCE_POINT_CENTER_MATERIAL);
        
        return centerLocation;
    }

    public void startResourceDropTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                plugin.getLogger().info("Checking custom structures for resource drops...");
                Map<UUID, Map<Location, Long>> structuresCopy = new HashMap<>(customStructures);
                structuresCopy.forEach((worldUUID, worldStructures) -> {
                    World world = Bukkit.getWorld(worldUUID);
                    if (world == null) {
                        plugin.getLogger().warning("World with UUID " + worldUUID + " not loaded. Skipping structure checks.");
                        return;
                    }
                    new HashMap<>(worldStructures).forEach((location, lastDropTime) -> {
                        if (currentTime - lastDropTime >= RailCoreConstants.RESOURCE_DROP_COOLDOWN_MILLIS) {
                            Block block = location.getBlock();
                            if (block.getType() == RailCoreConstants.RESOURCE_POINT_CENTER_MATERIAL) {
                                if (block.getChunk().isLoaded()) {
                                    int numItemsToDrop = random.nextInt(3) + 1;
                                    for (int i = 0; i < numItemsToDrop; i++) {
                                        Material dropMaterial = RailCoreConstants.POSSIBLE_DROPS[random.nextInt(RailCoreConstants.POSSIBLE_DROPS.length)];
                                        int amount = random.nextInt(3) + 1;
                                        Location dropLoc = block.getLocation().clone().add(0, -1, 0);
                                        block.getWorld().dropItemNaturally(dropLoc, new ItemStack(dropMaterial, amount));
                                    }
                                    plugin.getLogger().info("Dropped resources at custom structure: " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                                    worldStructures.put(location, currentTime);
                                }
                            } else {
                                plugin.getLogger().warning("Fluorite block missing at " + location + ". Structure is now broken and will be removed.");
                                worldStructures.remove(location);
                            }
                        }
                    });
                });
                plugin.saveCustomStructures();
            }
        }.runTaskTimer(plugin, RailCoreConstants.RESOURCE_DROP_CHECK_INTERVAL_TICKS, RailCoreConstants.RESOURCE_DROP_CHECK_INTERVAL_TICKS);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != RailCoreConstants.RESOURCE_POINT_CENTER_MATERIAL) {
            return;
        }
        Location loc = block.getLocation();
        World world = loc.getWorld();
        UUID worldUUID = world.getUID();
        Map<Location, Long> worldStructures = customStructures.get(worldUUID);
        if (worldStructures != null && worldStructures.containsKey(loc)) {
            worldStructures.remove(loc);
            plugin.getLogger().info("Resource point at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + " was broken (fluorite destroyed).");
            plugin.saveCustomStructures();
        }
    }
}
