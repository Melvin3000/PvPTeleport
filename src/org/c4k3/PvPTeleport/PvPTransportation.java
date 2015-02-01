package org.c4k3.PvPTeleport;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Handles transportation from 'world' to 'pvp'.
 * 
 * Generally called by WorldCommand.
 */
public class PvPTransportation {

	/**
	 * Blindly teleports player to the PvP world. Assumes all checks have been completed already.
	 * @param player Player to teleport.
	 */
	public static void teleportToPvP(Player player) {

		Location loc = randomSpawn();

		/* If unable to get random spawn location */
		if ( loc == null ) {
			player.sendMessage(ChatColor.RED + "Unable to get a spawn location for you.\n"
					+ "Please try again.\n"
					+ "If this problem persists, please contact an admin for assistance.");
			return;
		}


		String sPlayer = player.getName();

		/* Teleporting will glitch if a player is inside a vehicle */
		if ( player.isInsideVehicle() ) {
			player.leaveVehicle();
		}

		SQLite.worldLocsInsert(player);

		player.teleport(loc);
		player.sendMessage(ChatColor.GOLD + "Teleporting you to a random location in the pvp world.");
		PvPTeleport.instance.getLogger().info("Teleporting " + sPlayer + " to the pvp world.");

	}

	/**
	 * Gives a random spawn location in the pvp world
	 * @return The random location.
	 */
	private static Location randomSpawn() {

		/* Array of materials the player is allowed to spawn inside of */
		Material[] nonsolids = new Material[11];

		nonsolids[0] = Material.AIR;
		nonsolids[1] = Material.SAPLING;
		nonsolids[2] = Material.GRASS;
		nonsolids[3] = Material.DEAD_BUSH;
		nonsolids[4] = Material.YELLOW_FLOWER;
		nonsolids[5] = Material.RED_ROSE;
		nonsolids[6] = Material.BROWN_MUSHROOM;
		nonsolids[7] = Material.RED_MUSHROOM;
		nonsolids[8] = Material.TORCH;
		nonsolids[9] = Material.SIGN;
		nonsolids[10] = Material.SIGN_POST;

		Random randomGenerator = new Random();

		Material blocktype = null;
		Material blocktype1 = null;
		Material blocktype2 = null;

		Location spawnLoc = new Location(PvPTeleport.instance.getServer().getWorld("pvp"), 0, 0, 0);

		int counter = 0;

		/* Only give it 1000 tries */
		while ( counter < 1000 ) {

			Double y = 60.0;
			Double x = (double) randomGenerator.nextInt(599) - 299.5;
			Double z = (double) randomGenerator.nextInt(599) - 299.5;

			spawnLoc.setX(x);
			spawnLoc.setY(y);
			spawnLoc.setZ(z);

			blocktype = spawnLoc.getBlock().getType();

			/* While the block at the given x and z coords is of one of the below types, we check if the two blocks above it are
			 * empty blocks that would allow the player to teleport. */
			while ( ( blocktype == Material.STONE
					|| blocktype == Material.GRASS
					|| blocktype == Material.DIRT
					|| blocktype == Material.COBBLESTONE
					|| blocktype == Material.WATER
					|| blocktype == Material.STATIONARY_WATER
					|| blocktype == Material.SAND
					|| blocktype == Material.SANDSTONE
					) && y <= 250 ) {

				spawnLoc.setY(y + 1);
				blocktype1 = spawnLoc.getBlock().getType();

				spawnLoc.setY(y + 2);
				blocktype2 = spawnLoc.getBlock().getType();

				/* If the two blocks above the y position are valid nonsolids, then we got a good location */
				if ( Arrays.binarySearch(nonsolids, blocktype1) > -1 && Arrays.binarySearch(nonsolids, blocktype2) > -1 ) {

					PvPTeleport.instance.getLogger().info("Found valid pvp world spawn location on attempt " + counter + ".");
					return spawnLoc;

				}

				/* Increment y by one, to test that location */
				y++;
				spawnLoc.setY(y);
				blocktype = spawnLoc.getBlock().getType();

			}

			counter++;

		}

		PvPTeleport.instance.getLogger().info("Tried over 1000 times to find a suitable spawn, no result.");
		return null;

	}

}
