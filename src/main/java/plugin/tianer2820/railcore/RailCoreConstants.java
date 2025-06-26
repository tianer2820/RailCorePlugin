package plugin.tianer2820.railcore;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class RailCoreConstants {
    public static final String STRUCTURE_DB_FILE = "resource_points.yml";
    public static final long RESOURCE_DROP_COOLDOWN_MILLIS = 1000 * 15; // 10 seconds
    public static final long RESOURCE_DROP_CHECK_INTERVAL_TICKS = 20 * 5; // 1 second
    public static final int RESOURCE_POINT_RADIUS = 4;
    public static final double GENERATE_CHANCE = 0.01;
    public static final double MIN_DISTANCE_SQ = 50 * 50;
    public static final Material RESOURCE_POINT_RING_MATERIAL = Material.IRON_BLOCK;
    public static final Material RESOURCE_POINT_CENTER_MATERIAL = Material.GLOWSTONE;

    public static final Map<Material, Double> RESOURCE_TYPES = new HashMap<>() {{
        put(Material.COAL, 0.1);
        put(Material.RAW_IRON, 0.1);
        put(Material.RAW_GOLD, 0.1);
        put(Material.RAW_COPPER, 0.1);
        put(Material.REDSTONE, 0.1);
        put(Material.BONE, 0.1);
        put(Material.GUNPOWDER, 0.1);

        put(Material.EMERALD, 0.05);
        put(Material.LAPIS_LAZULI, 0.05);
        put(Material.DIAMOND, 0.05);
        put(Material.GOLDEN_CARROT, 0.05);

        put(Material.GOLDEN_APPLE, 0.005);
        put(Material.ANCIENT_DEBRIS, 0.005);
    }};



}
