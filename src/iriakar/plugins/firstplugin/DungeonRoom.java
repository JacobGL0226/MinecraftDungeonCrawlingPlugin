package iriakar.plugins.firstplugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import iriakar.plugins.firstplugin.Direction.Directions;

public class DungeonRoom {
	
	Plugin plugin;
	
	RoomCubicBlockData blockData;
	Block[][][] roomBlocks;
	Material[][][] oldBlocks;
	Location startPos;
	Location entranceDoorLocation;
	Location exitDoorLocation;
	ArrayList<Block> doorBlocks;
	
	// DATA FOR HANDLING ENTITIES
	ArrayList<LivingEntity> roomEnemies;
	
	
	
	// TO-DO: REMOVE THIS AND REFACTOR METHODS
	ArrayList<Block> tempDoorBlocks;
	
	Objective roomObjective;
	
	DungeonRoom(Plugin plugin, Location location, RoomCubicBlockData blockData) {
		
		location = location.getBlock().getLocation();
		
		this.plugin = plugin;
		startPos = location;
		this.blockData = blockData;
		this.roomBlocks = new Block[blockData.getLength()][blockData.getHeight()][blockData.getWidth()];
		this.oldBlocks = new Material[blockData.getLength()][blockData.getHeight()][blockData.getWidth()];
		
		entranceDoorLocation = getRealLocationFromRelativeLocation(blockData.getRelativeEntranceDoorLocation());
		exitDoorLocation = getRealLocationFromRelativeLocation(blockData.getRelativeExitDoorLocation());
		
		roomEnemies = new ArrayList<LivingEntity>();
		doorBlocks = new ArrayList<Block>();
		
		loadRoom();
		
		new BukkitRunnable() {
			public void run() {
				spawnEnemies();
			}
		}.runTaskLater(plugin, 2);
		
		switch(blockData.objectiveType) {
		case KILLENEMIES:
			roomObjective = new ObjectiveKillEnemy(roomEnemies);
			break;
		default:
			roomObjective = new ObjectiveComplete();
			break;
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		
		string += String.format("Entrance door location: (%f, %f, %f)", entranceDoorLocation.getX(), entranceDoorLocation.getY(), entranceDoorLocation.getZ());
		string += String.format("\nExit door location: (%f, %f, %f)", exitDoorLocation.getX(), exitDoorLocation.getY(), exitDoorLocation.getZ());
		string += "\nEntrance direction: " + blockData.getEntranceDoorDirection();
		string += "\nExit direction: " + blockData.getExitDoorDirection();
		
		return string;
	}
	
	private Location getRealLocationFromRelativeLocation(Location relativeDoorLocation) {
		
		Location location;
		
		double newX = startPos.getX() + relativeDoorLocation.getX();
		double newY = startPos.getY() + relativeDoorLocation.getY();
		double newZ = startPos.getZ() + relativeDoorLocation.getZ();
		
		location = new Location(startPos.getWorld(), newX, newY, newZ);
		
		return location;
	}
	
	private void spawnEnemies() {
		for (int i = 0; i < blockData.getLength(); i++) {
			for (int j = 0; j < blockData.getHeight(); j++) {
				for (int k = 0; k < blockData.getWidth(); k++) {
					
					Block block = roomBlocks[i][j][k];
					
					if (block.getType() == Material.ZOMBIE_HEAD) {
						spawnEnemy(block.getLocation(), EntityType.ZOMBIE);
					}
					
					else if (block.getType() == Material.SKELETON_SKULL) {
						spawnEnemy(block.getLocation(), EntityType.SKELETON);
					}
				}
			}
		}
	}
	
	private void spawnEnemy(Location location, EntityType entityType) {
		new BukkitRunnable() {
			public void run() {
				location.getBlock().setType(Material.AIR);
				roomEnemies.add((LivingEntity) location.getWorld().spawnEntity(location.getBlock().getLocation(), entityType));
			}
		}.runTask(plugin);
	}
	
	private boolean isDungeonDeath(LivingEntity entity) {
		
		boolean isDungeonDeath = false;
		
		for (int i = 0; i < roomEnemies.size(); i++) {
			
			if (roomEnemies.get(i) == null) {
				continue;
			}
			
			if (roomEnemies.get(i) == entity) {
				isDungeonDeath = true;
				break;
			}
		}
		
		return isDungeonDeath;
	}
	
	public void handleEntityDeath(LivingEntity entity) {
		
		if (!isDungeonDeath(entity)) {
			return;
		}
		
		roomEnemies.remove(entity);
		Bukkit.broadcastMessage("There are " + roomEnemies.size() + " mobs left!");
		
	}
	
	private void loadRoom() {
		
		buildExitDoor();
		
		for (int i = 0; i < blockData.getLength(); i++) {
			for (int j = 0; j < blockData.getHeight(); j++) {
				for (int k = 0; k < blockData.getWidth(); k++) {
					Location blockLocation = new Location(
							startPos.getWorld(),
							startPos.getX() + i, 
							startPos.getY() + j,
							startPos.getZ() + k);
					
					oldBlocks[i][j][k] = blockLocation.getBlock().getType();
					setBlock(blockLocation, blockData.getBlockData()[i][j][k]);
					roomBlocks[i][j][k] = blockLocation.getBlock();
				}
			}
		}
	}
	
	private void setBlock(Location location, Material material) {
		new BukkitRunnable() {
			public void run() {
				location.getBlock().setType(material);
			}
		}.runTask(plugin);
	}
	
	private void rebuildArea() {
		for (int i = 0; i < blockData.getLength(); i++) {
			for (int j = 0; j < blockData.getHeight(); j++) {
				for (int k = 0; k < blockData.getWidth(); k++) {
					Location blockLocation = new Location(startPos.getWorld(), 
							(int)startPos.getX() + i,
							(int)startPos.getY() + j, 
							(int)startPos.getZ() + k);
					Material blockType = oldBlocks[i][j][k];
					
					setBlock(blockLocation, blockType);
				}
			}
		}
	}

	private void buildStem(Location location) {
		
		while (location.getBlockY() < blockData.getHeight()) {
			
			if (blockData.getRelativeBlockAt(location) != Material.AIR) {
				break;
			}
			
			Location realLocation = getRealLocationFromRelativeLocation(location);
			
			setBlock(realLocation, blockData.doorMaterial);
			
			blockData.setRelativeBlockAt(location, blockData.getDoorMaterial());
			location = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ());
			
			tempDoorBlocks.add(realLocation.getBlock());
		}
	}
	
	private void buildStems(Location location, Direction.Directions direction) {
		
		Location newLocation;
		
		switch(direction) {
		case NORTH:
			newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()-1);
			break;
		case EAST:
			newLocation = new Location(location.getWorld(), location.getX()+1, location.getY(), location.getZ());
			break;
		case SOUTH:
			newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()+1);
			break;
		default:
			newLocation = new Location(location.getWorld(), location.getX()-1, location.getY(), location.getZ());
			break;
		}
		
		if (!isRelativelyInBounds(newLocation)) {
			return;
		}
		
		else if (blockData.getRelativeBlockAt(newLocation) != Material.AIR) {
			return;
		}
		
		buildStem(newLocation);
		buildStems(newLocation, direction);
		
	}
	
	private boolean isRelativelyInBounds(int x, int y, int z) {
		
		boolean returnValue = true;
		
		if (x < 0 || y < 0 || z < 0) {
			returnValue = false;
		}
		
		else if (x >= blockData.getLength() || y >= blockData.getHeight() || x >= blockData.getWidth()) {
			returnValue = false;
		}
		
		return returnValue;
	}
	
	private boolean isRelativelyInBounds(Location location) {
		return isRelativelyInBounds(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public void buildExitDoor() {
		
		tempDoorBlocks = new ArrayList<Block>();
		
		Direction.Directions direction = blockData.exitDoorDirection.getDirection();
		
		buildStem(blockData.getRelativeExitDoorLocation());
		
		if (direction == Directions.NORTH || direction == Directions.SOUTH) {
			buildStems(blockData.getRelativeExitDoorLocation(), Direction.Directions.EAST);
			buildStems(blockData.getRelativeExitDoorLocation(), Direction.Directions.WEST);
		}
		
		else {
			buildStems(blockData.getRelativeExitDoorLocation(), Direction.Directions.NORTH);
			buildStems(blockData.getRelativeExitDoorLocation(), Direction.Directions.SOUTH);
		}
		
		for (int i = 0; i < tempDoorBlocks.size(); i++) {
			doorBlocks.add(tempDoorBlocks.get(i));
		}
	}
	
	public void buildEntranceDoor() {
		
		tempDoorBlocks = new ArrayList<Block>();
		
		Direction.Directions direction = blockData.entranceDoorDirection.getDirection();
		
		buildStem(blockData.getRelativeEntranceDoorLocation());
		
		if (direction == Directions.NORTH || direction == Directions.SOUTH) {
			buildStems(blockData.getRelativeEntranceDoorLocation(), Direction.Directions.EAST);
			buildStems(blockData.getRelativeEntranceDoorLocation(), Direction.Directions.WEST);
		}
		
		else {
			buildStems(blockData.getRelativeEntranceDoorLocation(), Direction.Directions.NORTH);
			buildStems(blockData.getRelativeEntranceDoorLocation(), Direction.Directions.SOUTH);
		}
	}
	
	public void removeRoom() {
		rebuildArea();
	}

	public void removeDoor() {
		for (int i = 0; i < doorBlocks.size(); i++) {
			doorBlocks.get(i).setType(Material.AIR);
		}
	}
}
