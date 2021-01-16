package iriakar.plugins.firstplugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class DungeonGenerator {
	
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
		
		RoomCubicBlockData blockData = RoomCubicBlockData.readData("testRoom");
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
			if (dungeonRooms.get(0).roomObjective.isCompleted()) {
				dungeonRooms.get(0).removeDoor();
				buildNextRoom(RoomCubicBlockData.readData("testRoom"));
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
		
		// Set the exit/entrance to be on the same location
		offset[0] = currentRoom.blockData.getRelativeExitDoorLocation().getX() - blockData.getRelativeEntranceDoorLocation().getX();
		offset[1] = currentRoom.blockData.getRelativeExitDoorLocation().getY() - blockData.getRelativeEntranceDoorLocation().getY();
		offset[2] = currentRoom.blockData.getRelativeExitDoorLocation().getZ() - blockData.getRelativeEntranceDoorLocation().getZ();
		
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
