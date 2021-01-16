package iriakar.plugins.firstplugin;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class RoomCubicBlockData extends CubicBlockData {

	private static final long serialVersionUID = 1L;
	
	SerializableLocation relativeEntranceDoorLocation;
	SerializableLocation relativeExitDoorLocation;
	Direction exitDoorDirection;
	Direction entranceDoorDirection;
	Material doorMaterial;
	ObjectiveType objectiveType;
	Objective roomObjective;
	
	public Material getDoorMaterial() {
		return doorMaterial;
	}
	
	public RoomCubicBlockData(ObjectiveType objectiveType) {
		relativeEntranceDoorLocation = new SerializableLocation();
		relativeExitDoorLocation = new SerializableLocation();
		exitDoorDirection = new Direction();
		entranceDoorDirection = new Direction();
		
		this.objectiveType = objectiveType;
	}
	
	public RoomCubicBlockData(Material[][][] blockData, ObjectiveType objectiveType) {
		this(objectiveType);
		this.blockData = blockData;
	}
	
	public RoomCubicBlockData(Location pos1, Location pos2, ObjectiveType objectiveType) {
		this(objectiveType);
		getArea(pos1, pos2);
	}
	
	@Override
	public String toString() {
		String string = "";
		
		Location entrance = getRelativeEntranceDoorLocation();
		Location exit = getRelativeExitDoorLocation();
		
		string += String.format("Relative entrance door location: (%f, %f, %f)", entrance.getX(), entrance.getY(), entrance.getZ());
		string += String.format("\nRelative exitdoor location: (%f, %f, %f)", exit.getX(), exit.getY(), exit.getZ());
		string += "\nEntrance direction: " + entranceDoorDirection;
		string += "\nExit direction: " + exitDoorDirection;
		
		return string;
	}
	
	public Location getRelativeEntranceDoorLocation() {
		return relativeEntranceDoorLocation.getLocation();
	}
	
	public Location getRelativeExitDoorLocation() {
		return relativeExitDoorLocation.getLocation();
	}
	
	public Direction getExitDoorDirection() {
		return exitDoorDirection;
	}
	
	public Direction setExitDoorDirection(Direction direction) {
		exitDoorDirection = direction;
		return exitDoorDirection;
	}
	
	public Direction getEntranceDoorDirection() {
		return entranceDoorDirection;
	}
	
	
	public Direction setEntranceDoorDirection(Direction direction) {
		entranceDoorDirection = direction;
		return entranceDoorDirection;
	}
	
	@Override
	public Material[][][] getArea(Location corner1, Location corner2) {
		
		length = Math.abs(corner1.getBlockX() - corner2.getBlockX()) + 1;
		width = Math.abs(corner1.getBlockZ() - corner2.getBlockZ()) + 1;
		height = Math.abs(corner1.getBlockY() - corner2.getBlockY()) + 1;
		
		int xLower = Math.min(corner1.getBlockX(), corner2.getBlockX());
		int yLower = Math.min(corner1.getBlockY(), corner2.getBlockY());
		int zLower = Math.min(corner1.getBlockZ(), corner2.getBlockZ());

		Material[][][] area = new Material[length][height][width];
	
		Location relativeDoorLocation;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < height; j++){
				for (int k = 0; k < width; k++) {
					
					Location blockLocation = new Location(
							corner1.getWorld(), 
							xLower + i, 
							yLower + j,
							zLower + k).getBlock().getLocation();

					Material currentMaterial = blockLocation.getBlock().getType();
					
					if (currentMaterial == Material.GREEN_WOOL) {
						relativeDoorLocation = new Location(blockLocation.getWorld(), i, j, k);
						this.relativeExitDoorLocation.setLocation(relativeDoorLocation);
						Bukkit.broadcastMessage("Set relative exit door position to be " + relativeDoorLocation);
						exitDoorDirection.setDirection(getFacingDirection(length, width, i, k));
						
						Location doorMaterialLocation = new Location(blockLocation.getWorld(), blockLocation.getX(), blockLocation.getY()+1,blockLocation.getZ());
						doorMaterial = doorMaterialLocation.getBlock().getType();
						
						
						currentMaterial = Material.AIR;
					}
					
					else if (currentMaterial == Material.RED_WOOL) {
						relativeDoorLocation = new Location(blockLocation.getWorld(), i, j, k);
						this.relativeEntranceDoorLocation.setLocation(relativeDoorLocation);
						Bukkit.broadcastMessage("Set relative entrance door position to be " + relativeDoorLocation);
						entranceDoorDirection.setDirection(getFacingDirection(length, width, i, k));
						
						currentMaterial = Material.AIR;
					}
					
					else if (currentMaterial == doorMaterial) {
						currentMaterial = Material.AIR;
					}
					
					area[i][j][k] = currentMaterial;
				}
			}
		}
		
		this.blockData = area;
	
		return area;
	}
	
	public void rotateClockwise() {
		
		int temp = length;
		length = width;
		width = temp;
		
		Material[][][] rotatedData = new Material[length][height][width];
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < height; j++){
				for (int k = 0; k < width; k++) {
					rotatedData[i][j][k] = blockData[k][j][length-i-1];
				}
			}
		}
		
		blockData = rotatedData;
		
		Location location = relativeEntranceDoorLocation.getLocation();
		Location newLocation = new Location(location.getWorld(), (length-1) - location.getZ(), location.getY(), location.getX());
		relativeEntranceDoorLocation.setLocation(newLocation);
		
		location = relativeExitDoorLocation.getLocation();
		newLocation = new Location(location.getWorld(), (length-1) - location.getZ(), location.getY(), location.getX());
		relativeExitDoorLocation.setLocation(newLocation);
		
		entranceDoorDirection.rotateClockwise();
		exitDoorDirection.rotateClockwise();
	}
	
	public void rotateCounterClockwise() {
		
		for (int i = 0; i < 3; i++) {
			rotateClockwise();
		}
	}
	
	public void flipX() {
		
		Material temp;
		Location location;
		
		for (int i = 0; i < length/2; i++) {
			for (int j = 0; j < height; j++){
				for (int k = 0; k < width; k++) {
					temp = blockData[i][j][k];
					blockData[i][j][k] = blockData[length-i-1][j][k];
					blockData[length-i-1][j][k] = temp;
				}
			}
		}
		
		if (exitDoorDirection.direction == Direction.Directions.EAST || exitDoorDirection.direction == Direction.Directions.WEST) {
			exitDoorDirection.flip();
			
			location = relativeExitDoorLocation.getLocation();
			relativeExitDoorLocation.setLocation(new Location(location.getWorld(), length-location.getX()-1, location.getY(), location.getZ()));
		}
		
		if (entranceDoorDirection.direction == Direction.Directions.EAST || entranceDoorDirection.direction == Direction.Directions.WEST) {
			entranceDoorDirection.flip();
			
			location = relativeEntranceDoorLocation.getLocation();
			relativeEntranceDoorLocation.setLocation(new Location(location.getWorld(), length-location.getX()-1, location.getY(), location.getZ()));
		}
	}
	
	public void flipZ() {
		
		Material temp;
		Location location;
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < height; j++){
				for (int k = 0; k < width/2; k++) {
					temp = blockData[i][j][k];
					blockData[i][j][k] = blockData[i][j][width-k-1];
					blockData[i][j][width-k-1] = temp;
				}
			}
		}
		
		if (exitDoorDirection.direction == Direction.Directions.NORTH || exitDoorDirection.direction == Direction.Directions.SOUTH) {
			exitDoorDirection.flip();
			
			location = relativeExitDoorLocation.getLocation();
			relativeExitDoorLocation.setLocation(new Location(location.getWorld(), location.getX(), location.getY(), width-location.getZ()-1));
		}
		
		if (entranceDoorDirection.direction == Direction.Directions.NORTH || entranceDoorDirection.direction == Direction.Directions.SOUTH) {
			entranceDoorDirection.flip();
			
			location = relativeEntranceDoorLocation.getLocation();
			relativeEntranceDoorLocation.setLocation(new Location(location.getWorld(), location.getX(), location.getY(), width-location.getZ()-1));
		}
	}
	
	public Material setRelativeBlockAt(int x, int y, int z, Material material) {
		Material temp = blockData[x][y][z];
		
		blockData[x][y][z] = material;
		
		return temp;
	}
	
	public Material setRelativeBlockAt(Location location, Material material) {
		return setRelativeBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
	}
	
	public Material getRelativeBlockAt(int x, int y, int z) {
		return blockData[x][y][z];
	}
	
	public Material getRelativeBlockAt(Location location) {

		return getRelativeBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public static RoomCubicBlockData readData(String filename) {
		
		FileInputStream fileIn;
		ObjectInputStream objectIn;
		RoomCubicBlockData blockData = null;

		
		try {
			fileIn = new FileInputStream(PLUGIN_DIR + filename + ".ser");
			objectIn = new ObjectInputStream(fileIn);
			blockData = (RoomCubicBlockData) objectIn.readObject();
			
			fileIn.close();
			objectIn.close();
		} catch(Exception e) {
			Bukkit.broadcastMessage("Error: " + e.toString() + " on line " + e.getStackTrace()[0].getLineNumber() + " of " + e.getStackTrace()[0].getClassName() + "'s " + e.getStackTrace()[0].getMethodName() + " method.");
		}
		
		return blockData;
		
	}
	
	
	public static void writeData(RoomCubicBlockData blockData, String filename) throws IOException {
		
		FileOutputStream fileOut = new FileOutputStream(PLUGIN_DIR + filename + ".ser");
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		
		
		objectOut.writeObject(blockData);
		
		fileOut.close();
		objectOut.close();
		
		Bukkit.broadcastMessage("Block data saved into \"" + PLUGIN_DIR + filename + "\"");
	}

	
	private Direction.Directions getFacingDirection(int length, int width, int i, int k) {

		Direction.Directions direction;
		
		if (i == 0) {
			direction = Direction.Directions.WEST;
		}
		
		else if (i == length-1) {
			direction = Direction.Directions.EAST;
		}
		
		else if (k == 0) {
			direction = Direction.Directions.NORTH;
		}
		
		else {
			direction = Direction.Directions.SOUTH;
		}
		
		return direction;
	}

	
	
}
