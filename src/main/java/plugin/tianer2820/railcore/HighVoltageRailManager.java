package plugin.tianer2820.railcore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class HighVoltageRailManager implements Listener {
    private final RailCorePlugin plugin;


    public HighVoltageRailManager(RailCorePlugin plugin) {
        this.plugin = plugin;
        // Start periodic task
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!RailCoreConstants.ENABLE_HIGH_VOLTAGE_RAIL) return;
                for (World world : Bukkit.getWorlds()) {
                    for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class)) {
                        // Only affect mobs (animals and monsters), not items, vehicles, etc.
                        if (entity.isDead()) continue;
                        if (RailCoreConstants.HIGH_VOLTAGE_RAIL_EXCLUDE_PLAYER && entity instanceof Player) continue;
                        if (entity.getVehicle() != null) continue;
                        Location loc = entity.getLocation();
                        if (isOnPoweredRail(loc)) {
                            entity.damage(RailCoreConstants.HIGH_VOLTAGE_RAIL_DAMAGE, DamageSource.builder(DamageType.LIGHTNING_BOLT).withDamageLocation(loc).build());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, RailCoreConstants.HIGH_VOLTAGE_RAIL_INTERVAL_TICKS, RailCoreConstants.HIGH_VOLTAGE_RAIL_INTERVAL_TICKS);
    }

    private boolean isOnPoweredRail(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;
        // Check block below and at feet
        return (isPoweredRail(world, loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()) ||
                isPoweredRail(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    private boolean isPoweredRail(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        Material type = block.getType();
        if (type != Material.POWERED_RAIL) return false;
        // Check if rail is actually powered
        RedstoneRail redstoneRail = (RedstoneRail) block.getBlockData();
        return redstoneRail.isPowered();
    }
}

