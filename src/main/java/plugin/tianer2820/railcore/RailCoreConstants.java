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

    public static final Material[] POSSIBLE_DROPS = {
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
}
