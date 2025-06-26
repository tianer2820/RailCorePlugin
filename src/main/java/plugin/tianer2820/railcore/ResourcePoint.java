package plugin.tianer2820.railcore;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.UUID;

public class ResourcePoint {
    private final Location center;
    private long lastDropTime;
    private final Material resourceType;
    private UUID lastItemEntityUUID; // UUID of the last dropped item entity

    public ResourcePoint(Location center, long lastDropTime, Material resourceType) {
        this.center = center;
        this.lastDropTime = lastDropTime;
        this.resourceType = resourceType;
    }

    public Location getCenter() { return center; }
    public long getLastDropTime() { return lastDropTime; }
    public void setLastDropTime(long lastDropTime) { this.lastDropTime = lastDropTime; }
    public Material getResourceType() { return resourceType; }
    public UUID getLastItemEntityUUID() { return lastItemEntityUUID; }
    public void setLastItemEntityUUID(UUID uuid) { this.lastItemEntityUUID = uuid; }
}
