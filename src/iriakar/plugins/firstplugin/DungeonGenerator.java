package iriakar.plugins.firstplugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class DungeonGenerator {
	
	final static String startingRoomName = "startRoom.ser";
	static final String PLUGIN_DIR = "plugins\\CubicBlockData\\";
	final private Random random = new Random();
	
	private Plugin plugin;
	
	// DATA FOR HANDLING ROOMS
	ArrayList<DungeonRoom> dungeonRooms;
	
	// MISC DATA
	private boolean dungeonStarted = false;
	private int roomNumber;
	
	public DungeonGenerator(Plugin plugin) {
		this.plugin = plugin;
		dungeonRooms = new ArrayList<DungeonRoom>();
	}
	
	public void startDungeon(Location location) {
		
		if (dungeonStarted) {
			Bukkit.broadcastMessage("There is already an existing dungeon being played!");
			return;
		}
		
		RoomCubicBlockData blockData = RoomCubicBlockData.readData(startingRoomName.replace(".ser", ""));
		roomNumber = 0;
		
		dungeonStarted = true;
		Bukkit.broadcastMessage("Dungeon started!");
		
		Location center = new Location(location.getWorld(), location.getX()-blockData.length/2, location.getY()-1, location.getZ()-blockData.width/2);
		
		dungeonRooms.add(new DungeonRoom(plugin, center, blockData));
		dungeonRooms.get(0).buildEntranceDoor();
		
	}
	
	public void endDungeon() {
		dungeonStarted = false;
		
		while (dungeonRooms.size() > 0) {
			dungeonRooms.get(0).removeRoom();
			dungeonRooms.remove(0);
		}
		
		Bukkit.broadcastMessage("You have beaten the dungeon!");
	}
	
	public void handleEntityDeath(LivingEntity entity) {
		
		if (dungeonRooms.size() == 0) {
			return;
		}
		
		if (dungeonRooms.get(0).roomEnemies.contains(entity)) {
			dungeonRooms.get(0).handleEntityDeath(entity);
		}
	}
	
	public void handleBlockInteraction(PlayerInteractEvent event) {
		
		if (dungeonRooms.size() == 0) {
			return;
		}
		
		if (dungeonRooms.get(0).doorBlocks.contains(event.getClickedBlock())) {
			Bukkit.broadcastMessage("Attempting right click event...");
			
			if (dungeonRooms.get(0).roomObjective.isCompleted()) {
				dungeonRooms.get(0).removeDoor();
				
				String[] roomNames = new File(PLUGIN_DIR).list();
				
				String outputString = "";
				
				for (int i = 0; i < roomNames.length; i++) {
					outputString += roomNames[i] + ", ";
				}
				
				Bukkit.broadcastMessage(outputString);
				
				String roomName = startingRoomName;

				while (roomName.equals(startingRoomName)) {
					roomName = roomNames[Math.abs((random.nextInt()) % roomNames.length)];
					Bukkit.broadcastMessage("Current room: " + roomName);
				}
				
				buildNextRoom(RoomCubicBlockData.readData(roomName.replace(".ser", "")));
			}
			
			else {
				Bukkit.broadcastMessage(dungeonRooms.get(0).roomObjective.toString());
			}
		}
		
	}
	
	public DungeonRoom buildNextRoom(RoomCubicBlockData blockData) {
		
		Location startPos;
		double[] offset = new double[3];
		
		DungeonRoom currentRoom = dungeonRooms.get(0);
		
		// Rotate/flip the new room so that the directions are connected
		while (blockData.entranceDoorDirection.getDirection() != currentRoom.blockData.getExitDoorDirection().flipped()) {
			blockData.rotateClockwise();
		}
		
		// Make sure that the dungeon is still moving North
		if (blockData.exitDoorDirection.getDirection() == Direction.Directions.SOUTH) {
			blockData.flipZ();
		}
		
		Bukkit.broadcastMessage(String.format("Current relative exit: %s\nCurrent next entrance: %s", currentRoom.blockData.relativeExitDoorLocation.getLocation(), blockData.getRelativeEntranceDoorLocation()));
		
		// Set the exit/entrance to be on the same location
		offset[0] = currentRoom.blockData.getRelativeExitDoorLocation().getX() - blockData.getRelativeEntranceDoorLocation().getX();
		offset[1] = currentRoom.blockData.getRelativeExitDoorLocation().getY() - blockData.getRelativeEntranceDoorLocation().getY();
		offset[2] = currentRoom.blockData.getRelativeExitDoorLocation().getZ() - blockData.getRelativeEntranceDoorLocation().getZ();
		
		Bukkit.broadcastMessage(String.format("Offset: %f, %f, %f", offset[0], offset[1], offset[2]));
		
		if (currentRoom.blockData.getExitDoorDirection().getDirection() == Direction.Directions.EAST) {
			offset[0] += 1;
		}
		
		else if (currentRoom.blockData.getExitDoorDirection().getDirection() == Direction.Directions.WEST) {
			offset[0] -= 1;
		}
		
		else if (currentRoom.blockData.getExitDoorDirection().getDirection() == Direction.Directions.NORTH) {
			offset[2] -= 1;
		}
		
		else {
			offset[2] +=1;
		}
		
		startPos = new Location(currentRoom.startPos.getWorld(), currentRoom.startPos.getX() + offset[0], currentRoom.startPos.getY() + offset[1], currentRoom.startPos.getZ() + offset[2]);
		
		if (dungeonRooms.size() > 2) {
			DungeonRoom room = dungeonRooms.get(2);
			room.removeRoom();
			dungeonRooms.remove(2);
			dungeonRooms.get(1).buildEntranceDoor();
		}
		
		return addRoom(startPos, blockData);
	}
	
	public DungeonRoom addRoom(Location location, RoomCubicBlockData blockData) {
		
		DungeonRoom room = new DungeonRoom(plugin, location, blockData);
		
		dungeonRooms.add(0, room);
		roomNumber++;
		
		Bukkit.broadcastMessage("You are on room: " + roomNumber);
		
		return room;
		
	}
}
