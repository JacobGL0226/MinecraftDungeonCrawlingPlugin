package iriakar.plugins.firstplugin;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class CubicBlockData implements Serializable {

	private static final long serialVersionUID = 1L;
	static final String PLUGIN_DIR = "plugins\\CubicBlockData\\";
	
	int length;
	public int getLength() {
		return length;
	}
	
	int width;
	public int getWidth() {
		return width;
	}
	
	int height;
	public int getHeight() {
		return height;
	}
	
	Material[][][] blockData;
	
	public CubicBlockData() {
		blockData = null;
	}

	public CubicBlockData(Material[][][] blockData) {
		this.blockData = blockData;
	}
	
	public CubicBlockData(Location pos1, Location pos2) {
		getArea(pos1, pos2);
	}
	
	public Material[][][] getBlockData() {
		return blockData;
	}
	
	public void saveBlockData(String dir) throws IOException {
		writeData(dir);
	}
	
	public void writeData(String filename) throws IOException {
		
		FileOutputStream fileOut = new FileOutputStream(PLUGIN_DIR + filename + ".ser");
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		
		objectOut.writeObject(this);
		
		fileOut.close();
		objectOut.close();
		
		Bukkit.broadcastMessage("Block data saved into \"" + PLUGIN_DIR + filename + ".ser\"");
	}
	
	public static void writeData(CubicBlockData blockData, String filename) throws IOException {
		
		FileOutputStream fileOut = new FileOutputStream(PLUGIN_DIR + filename);
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		
		
		objectOut.writeObject(blockData);
		
		fileOut.close();
		objectOut.close();
		
		Bukkit.broadcastMessage("Block data saved into \"" + PLUGIN_DIR + filename + "\"");
	}

	public static CubicBlockData readData(String filename) {
		
		FileInputStream fileIn;
		ObjectInputStream objectIn;
		CubicBlockData blockData = null;

		
		try {
			fileIn = new FileInputStream(PLUGIN_DIR + filename + ".ser");
			objectIn = new ObjectInputStream(fileIn);
			blockData = (CubicBlockData) objectIn.readObject();
			
			fileIn.close();
			objectIn.close();
		} catch(Exception e) {
			Bukkit.broadcastMessage("Error: " + e.toString() + " on line " + e.getStackTrace()[0].getLineNumber() + " of " + e.getStackTrace()[0].getClassName() + "'s " + e.getStackTrace()[0].getMethodName() + " method.");
		}
		
		return blockData;
		
	}

	public Material[][][] getArea(Location corner1, Location corner2) {
		
		length = Math.abs(corner1.getBlockX() - corner2.getBlockX()) + 1;
		width = Math.abs(corner1.getBlockZ() - corner2.getBlockZ()) + 1;
		height = Math.abs(corner1.getBlockY() - corner2.getBlockY()) + 1;
		
		int xLower = Math.min(corner1.getBlockX(), corner2.getBlockX());
		int yLower = Math.min(corner1.getBlockY(), corner2.getBlockY());
		int zLower = Math.min(corner1.getBlockZ(), corner2.getBlockZ());

		Material[][][] area = new Material[length][height][width];
	
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < height; j++){
				for (int k = 0; k < width; k++) {
					
					Location blockLocation = new Location(
							corner1.getWorld(), 
							xLower + i, 
							yLower + j,
							zLower + k).getBlock().getLocation();

					Material currentMaterial = blockLocation.getBlock().getType();
					
					area[i][j][k] = currentMaterial;
				}
			}
		}
		
		this.blockData = area;
	
		return area;
	}

}
