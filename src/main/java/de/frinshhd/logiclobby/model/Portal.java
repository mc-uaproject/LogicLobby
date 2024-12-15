package de.frinshhd.logiclobby.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Portal {
    private String destinationServer;
    private Location pointOneLocation;
    private Location pointTwoLocation;

    public Portal(
            @JsonProperty("destinationServer") String destinationServer,
            @JsonProperty("pointOne") String pointOne,
            @JsonProperty("pointTwo") String pointTwo
    ) {
        this.destinationServer = destinationServer;
        pointOneLocation = parsePoint(pointOne);
        pointTwoLocation = parsePoint(pointTwo);
    }

    @JsonIgnore
    private Location parsePoint(String point) {
        String[] parts = point.split(",");
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    public String getDestinationServer() { return destinationServer; }

    @JsonIgnore
    public boolean isInRange(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double x1 = Math.min(pointOneLocation.getX(), pointTwoLocation.getX());
        double x2 = Math.max(pointOneLocation.getX(), pointTwoLocation.getX());
        double y1 = Math.min(pointOneLocation.getY(), pointTwoLocation.getY());
        double y2 = Math.max(pointOneLocation.getY(), pointTwoLocation.getY());
        double z1 = Math.min(pointOneLocation.getZ(), pointTwoLocation.getZ());
        double z2 = Math.max(pointOneLocation.getZ(), pointTwoLocation.getZ());
        return (
                location.getWorld().equals(pointOneLocation.getWorld())
                && x >= x1 && x <= x2
                && y >= y1 && y <= y2
                && z >= z1 && z <= z2
        );
    }
}
