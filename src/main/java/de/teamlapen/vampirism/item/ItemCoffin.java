package de.teamlapen.vampirism.item;

import de.teamlapen.vampirism.ModBlocks;
import de.teamlapen.vampirism.tileEntity.TileEntityCoffin;
import de.teamlapen.vampirism.util.Logger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * 
 * @author Moritz
 *
 */
public class ItemCoffin extends BasicItem {

	private static final String TAG = "ItemCoffin";
	public static final String name = "coffin";
	private int[][] shiftArray = { { 0, 0, 1 }, { -1, 0, 0 }, { 0, 0, -1 },
			{ 1, 0, 0 } };

	public ItemCoffin() {
		super(name);
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World world,
			int x, int y, int z, int side, float xOffset, float yOffset,
			float zOffset) {
		if (world.isRemote || side > 1)
			return false;
		// Increasing y, so the coffin is placed on top of the block that was
		// clicked at
		y++;
		// Direction the player is facing
		int direction = MathHelper
				.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

		Logger.i(TAG, "Direction = " + direction);
		if (world.isAirBlock(x, y, z)
				&& world.isAirBlock(x + shiftArray[direction][0], y
						+ shiftArray[direction][1], z
						+ shiftArray[direction][2])) {
			if (!world.setBlock(x, y, z, ModBlocks.coffin, direction|-8, 3))
				Logger.e(TAG, "Primary coffin block placement failed");
			else {
				Logger.i(TAG, "Primary coffin block placed");
				if (!world.setBlock(x + shiftArray[direction][0], y
						+ shiftArray[direction][1], z
						+ shiftArray[direction][2], ModBlocks.coffin, 0, 3))
					Logger.e(TAG, "Secondary coffin block placement failed");
				else {
					Logger.i(TAG, "Secondary block placed");
				}
				TileEntityCoffin tePrim = (TileEntityCoffin) world
						.getTileEntity(x, y, z);
				TileEntityCoffin teSec = (TileEntityCoffin) world
						.getTileEntity(x + shiftArray[direction][0], y
								+ shiftArray[direction][1], z
								+ shiftArray[direction][2]);
				if (tePrim != null) {
					tePrim.otherX = x + shiftArray[direction][0];
					tePrim.otherY = y + shiftArray[direction][1];
					tePrim.otherZ = z + shiftArray[direction][2];
				} else
					Logger.e(TAG, "No (primary) coffin tile entity found at x="
							+ x + ", y=" + y + ", z=" + z);

				if (teSec != null) {
					teSec.otherX = x;
					teSec.otherY = y;
					teSec.otherZ = z;
				} else
					Logger.e(TAG,
							"No (secondary) coffin tile entity found at x=" + x
									+ ", y=" + y + ", z=" + z);
			}
		}
		return true;
	}
}
