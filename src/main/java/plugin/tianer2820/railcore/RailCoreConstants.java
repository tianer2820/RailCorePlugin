package plugin.tianer2820.railcore;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class RailCoreConstants {
    public static final String STRUCTURE_DB_FILE = "resource_points.yml";
    public static final long RESOURCE_DROP_INTERVAL_TICKS = 20 * 10; // 10 seconds
    public static final int RESOURCE_POINT_RADIUS = 4;
    public static final double GENERATE_CHANCE = 0.03;
    public static final double MIN_DISTANCE_SQ = 50 * 50;
    public static final long RESOURCE_POINT_SAVE_INTERVAL_MILLIS = 1000 * 60 * 2; // 2 min
    public static final Material RESOURCE_POINT_RING_MATERIAL = Material.IRON_BLOCK;
    public static final Material RESOURCE_POINT_CENTER_MATERIAL = Material.GLOWSTONE;

    public static final Map<Material, Double> RESOURCE_TYPES = new HashMap<>() {{
        put(Material.COAL, 0.2);
        put(Material.RAW_IRON, 0.2);
        put(Material.OAK_LOG, 0.2);

        put(Material.RAW_GOLD, 0.1);
        put(Material.RAW_COPPER, 0.1);
        put(Material.REDSTONE, 0.1);

        put(Material.ROTTEN_FLESH, 0.1);  // not really rotten flesh, it will drop all kinds of monster loots
        put(Material.WHEAT, 0.1);  // not really wheat, it will drop all kinds of crops
        

        put(Material.EMERALD, 0.05);
        put(Material.LAPIS_LAZULI, 0.05);
        put(Material.DIAMOND, 0.05);

        put(Material.ANCIENT_DEBRIS, 0.005);
    }};

    public static final Map<Material, Double> MONSTER_LOOTS = new HashMap<>() {{
        // zombie
        put(Material.ROTTEN_FLESH, 0.2);
        // skeleton
        put(Material.BONE, 0.1);
        put(Material.ARROW, 0.1);
        // creeper
        put(Material.GUNPOWDER, 0.2);
        // spider
        put(Material.STRING, 0.2);
        put(Material.SPIDER_EYE, 0.1);

        // enderman
        put(Material.ENDER_EYE, 0.01);

        // slime
        put(Material.SLIME_BALL, 0.01);
    }};


    public static final Map<Material, Double> CROP_LOOTS = new HashMap<>() {{
        put(Material.CARROT, 0.1);
        put(Material.POTATO, 0.1);
        put(Material.BEETROOT, 0.1);
        put(Material.WHEAT, 0.1);
        put(Material.SUGAR_CANE, 0.1);

        put(Material.MELON, 0.1);
        put(Material.PUMPKIN, 0.1);
    }};

    public static final double totalMonsterLootWeight = MONSTER_LOOTS.values().stream().mapToDouble(Double::doubleValue).sum();
    public static final double totalCropLootWeight = CROP_LOOTS.values().stream().mapToDouble(Double::doubleValue).sum();
    public static final double totalResourceTypeWeight = RESOURCE_TYPES.values().stream().mapToDouble(Double::doubleValue).sum();
}
