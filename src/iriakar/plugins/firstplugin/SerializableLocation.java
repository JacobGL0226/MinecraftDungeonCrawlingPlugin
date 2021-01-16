package iriakar.plugins.firstplugin;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableLocation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	String worldName;
	double x, y, z;
	
	public SerializableLocation() {

	}
	
	public SerializableLocation(String worldName, double x, double y, double z) {
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getServer().getWorld(worldName), x, y, z);
	}
	
	public Location setLocation(Location location) {
		
		worldName = location.getWorld().getName();
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		
		return location;
	}
}
