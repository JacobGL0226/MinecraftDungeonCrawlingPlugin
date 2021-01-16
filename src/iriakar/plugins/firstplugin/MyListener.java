package iriakar.plugins.firstplugin;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MyListener implements Listener {
	
	private Main main;
	
	private Location pos1;
	private Location pos2;
	
	private RoomCubicBlockData currentRoom;
	private DungeonRoom currentDungeonRoom;
	
	public MyListener(Main main) {
		this.main = main;
	}
	
	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent event) throws IOException {

		RoomCubicBlockData blockData;
		String[] args = event.getMessage().split(" ");
		
		if (event.getMessage().equals("!sd")) {
			main.dungeonGenerator.startDungeon(event.getPlayer().getLocation());
		}
		
		if (event.getMessage().equals("!ed")) {
			main.dungeonGenerator.endDungeon();
		}
		
		if (event.getMessage().startsWith("!sbd")) {
			Bukkit.broadcastMessage("Attempting to save block data...");
			
			if (args.length < 2) {
				Bukkit.broadcastMessage("Not enough arguments given. The format is: !sbd [name] [objective]");
			}
			
			else if (args.length == 2) {
				new RoomCubicBlockData(pos1, pos2, ObjectiveType.NONE).writeData(args[1]);
			}
			
			else {
				
				if (args[2].toUpperCase().equals("KILLENEMIES")) {
					new RoomCubicBlockData(pos1, pos2, ObjectiveType.KILLENEMIES).writeData(args[1]);
				}
				
				else {
					new RoomCubicBlockData(pos1, pos2, ObjectiveType.NONE).writeData(args[1]);
				}
			}
		}
		
		if (event.getMessage().startsWith("!br")) {
			Bukkit.broadcastMessage("Attempting to build room...");
			if (event.getMessage().length() > 3) {
				currentRoom = RoomCubicBlockData.readData(event.getMessage().substring(4));
			}
			
			currentDungeonRoom = main.dungeonGenerator.addRoom(event.getPlayer().getLocation(), currentRoom);
		}
		
		if (event.getMessage().equals("!rcw")) {
			Bukkit.broadcastMessage("Attempting to rotate room...");
			currentRoom.rotateClockwise();
		}
		
		if (event.getMessage().equals("!grs")) {
			Bukkit.broadcastMessage(currentDungeonRoom.toString());
		}
		
		if (event.getMessage().equals("!fx")) {
			Bukkit.broadcastMessage("Attempting to flip(X) room...");
			currentRoom.flipX();
		}
		
		if (event.getMessage().equals("!fz")) {
			Bukkit.broadcastMessage("Attempting to flip(Z) room...");
			currentRoom.flipZ();
		}
		
		if (event.getMessage().startsWith("!gnr")) {
			Bukkit.broadcastMessage("Attempting to build next room...");
			
			if (event.getMessage().length() == 4) {
				blockData = RoomCubicBlockData.readData("testRoom");
			}
			else {
				blockData = RoomCubicBlockData.readData(event.getMessage().substring(5));
			}
			
			currentDungeonRoom = main.dungeonGenerator.buildNextRoom(blockData);
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {

		try {
		main.dungeonGenerator.handleEntityDeath(event.getEntity());
		}
		catch(Exception e) {
			Bukkit.broadcastMessage("Entity Died with error: " + e.toString());
		}
		
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_SHOVEL) {
				pos1 = event.getBlock().getLocation();
				Bukkit.broadcastMessage("Set pos1 to " + pos1);
				event.setCancelled(true);
		}
		
		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_HOE) {
			pos2 = event.getBlock().getLocation();
			Bukkit.broadcastMessage("Set pos2 to " + pos2);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockRightClick(PlayerInteractEvent event) {
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.HAND) {
			return;
		}
		
		main.dungeonGenerator.handleBlockInteraction(event);
	}
}
