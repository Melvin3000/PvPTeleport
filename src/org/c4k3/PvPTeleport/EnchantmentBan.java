package org.c4k3.PvPTeleport;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EnchantmentBan implements Listener {

	public static boolean TeleportCheck(Player player) {
		/** Checks if input player has any items with enchantments on any illegal slots (hotbar and armor slots)
		 * 
		 * Return true if has illegal items. Returns false else
		 */

		PlayerInventory inventory = player.getInventory();

		/* Checks the hotbar (item slots 0 through 8) */
		for ( int i = 0 ; i < 9 ; i++) {

			if ( inventory.getItem(i) != null )  { // Bukkit likes to throw an annoyingly ambiguous error if we try to call getItem on an empty slot
				if ( !inventory.getItem(i).getEnchantments().isEmpty() ) return true; // Double negative (If Not Is Empty == if has enchantments)
			}

		}

		/* Checks the armor slots (item slots 36 through 39) */
		for ( int i = 36 ; i < 40 ; i++) {

			if ( inventory.getItem(i) != null )  {
				if ( !inventory.getItem(i).getEnchantments().isEmpty() ) return true;
			}

		}

		return false;

	}


	@EventHandler(priority = EventPriority.NORMAL,ignoreCancelled=true)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		/** Ensures that players do not move any enchanted items onto their hotbar whilst in the pvp world */
		/* Important: event.getSlot is NOT the same as inventory.getItem, so the slot numbers are different here
		 * 
		 * The bukkit documentation is a bit ambiguous. Use getSlotType(), this conveniently automatically has the
		 * QUICKBAR and ARMOR slottypes ready.
		 * 
		 * Use getAction() to get the action. PLACE_ALL seems to encompass everything inventory-wise
		 * (including pickups.) Even ctrl + number actions are registered as PLACE_ALL.
		 * 
		 * getCursor() appears to get what will happen to the clicked slot AFTER transfer.
		 */
				
		if ( !event.getWhoClicked().getWorld().getName().equals("pvp") ) return; // Return if this happens anywhere but the pvp world
		
		Bukkit.getLogger().info("Clicked slot: " + event.getSlot() + " " + event.getSlotType() + " " + event.getAction() + " " + event.getCursor()); // For decoding the bukkit api
		
		if ( event.getSlotType() == InventoryType.SlotType.QUICKBAR || event.getSlotType() == InventoryType.SlotType.ARMOR ) {
			Bukkit.getLogger().info("Clicked dangerous slot");
			
			if ( !event.getCursor().getEnchantments().isEmpty() ) {
				/* Again with the double negatives. This is now an illegal action. Simply canceling the action will make
				 * the item disappear. So we have to cancel, and then we use World.dropItem to spawn it again in front
				 * of the player.
				 */
				
				ItemStack item = event.getCursor();
				
				Location loc = event.getWhoClicked().getLocation();
				
				loc.getWorld().dropItem(loc, item);
				
				event.setCancelled(true);
				
			}
			
		}

	}
	
	private List<Integer> tempStones = new ArrayList<Integer>(); // Lists the hotbar slots in which stone was temporarily put, used to remove them again after 1 ticks in the scheduler
	
	private ItemStack stone = new ItemStack(Material.STONE);

	
	@EventHandler(priority = EventPriority.NORMAL,ignoreCancelled=true)
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		/** Ensures that picked up items with enchantments are not put into the player's hotbar */
		
		Bukkit.getLogger().info("Pickup event");
		
		if ( event.getItem().getItemStack().getEnchantments().isEmpty() ) return; // Ensures that this only acts upon enchanted items

		final PlayerInventory inventory = event.getPlayer().getInventory();
		
		for ( int i = 0 ; i < 9 ; i++ ) { // Hotbar slots are 0 through 8
			if ( inventory.getItem(i) == null ) {
				inventory.setItem(i, stone);
				tempStones.add(i);
			}
		}
		
		/* Schedule the removal of the stone blocks again */
		PvPTeleport.instance.getServer().getScheduler().scheduleSyncDelayedTask(PvPTeleport.instance, new Runnable() {
			public void run() {
				for ( int i : tempStones ) {
					inventory.clear(i);
				}
				tempStones.clear();
			}
		}, 1);

	}

}
