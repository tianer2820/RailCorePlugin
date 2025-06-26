package plugin.tianer2820.railcore;

import org.bukkit.Material;

public class RailCoreConstants {
    public static final String STRUCTURE_DB_FILE = "resource_points.yml";
    public static final long RESOURCE_DROP_COOLDOWN_MILLIS = 1000 * 10; // 10 seconds
    public static final long RESOURCE_DROP_CHECK_INTERVAL_TICKS = 20; // 1 second
    public static final int RESOURCE_POINT_RADIUS = 4;
    public static final double GENERATE_CHANCE = 0.05;
    public static final double MIN_DISTANCE_SQ = 50 * 50;
    public static final Material RESOURCE_POINT_RING_MATERIAL = Material.IRON_BLOCK;
    public static final Material RESOURCE_POINT_CENTER_MATERIAL = Material.GLOWSTONE;

    public static final Material[] RESOURCE_TYPES = {
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

    // Must match RESOURCE_TYPES order
    public static final double[] RESOURCE_TYPE_WEIGHTS = {
            0.25, // IRON_INGOT
            0.15, // GOLD_INGOT
            0.05, // DIAMOND
            0.05, // EMERALD
            0.10, // LAPIS_LAZULI
            0.10, // REDSTONE
            0.10, // RAW_IRON
            0.05, // RAW_GOLD
            0.10, // COAL
            0.05  // COPPER_INGOT
    };

}
