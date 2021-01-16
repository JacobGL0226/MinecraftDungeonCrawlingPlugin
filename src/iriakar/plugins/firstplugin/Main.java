package iriakar.plugins.firstplugin;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	public DungeonGenerator dungeonGenerator;
	
	@Override
	public void onEnable() {
		dungeonGenerator = new DungeonGenerator(this);
		getServer().getPluginManager().registerEvents(new MyListener(this),  this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public void spawnEntity(Location location, EntityType entity) {
		location.getWorld().spawnEntity(location, entity);
	}
	
}
