package de.teamlapen.vampirism.tileEntity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCompressed;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.teamlapen.vampirism.ModBlocks;
import de.teamlapen.vampirism.ModItems;
import de.teamlapen.vampirism.ModPotion;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.block.BlockBloodAltar4;
import de.teamlapen.vampirism.block.BlockBloodAltar4Tip;
import de.teamlapen.vampirism.entity.player.VampirePlayer;
import de.teamlapen.vampirism.network.RenderScreenRedPacket;
import de.teamlapen.vampirism.network.SpawnCustomParticlePacket;
import de.teamlapen.vampirism.util.Logger;
import de.teamlapen.vampirism.util.REFERENCE;

/**
 * Tileentity used for BloodAltar 4
 * 
 * @author Max
 *
 */
public class TileEntityBloodAltar4 extends InventoryTileEntity {

	private static enum LevReq {
		OK, STRUCTURE_WRONG, ITEM_MISSING, LEVEL_WRONG;
	}

	public static enum PHASE {
		NOT_RUNNING, PARTICLE_SPREAD, BEAM1, BEAM2, WAITING, LEVELUP, ENDING, CLEAN_UP;
	}

	private final static String TAG = "TEBAltar4";

	private final static int[][][] structure1 = new int[][][] { { { 1, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0 }, { 0, 0, 3, 0, 0 }, { 0, 0, 0/* Bed */, 0, 0 }, { 1, 0, 0/* Bed */, 0, 1 } },
			{ { 1, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 1 } },
			{ { 2, 0, 0, 0, 2 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 2, 0, 0, 0, 2 } } };
	private final static int[][][] structure2 = new int[][][] {
			{ { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 3, 0, 0, 1 }, { 0, 0, 0, 0/* Bed */, 0, 0, 0 }, { 0, 1, 0, 0/* Bed */, 0, 1, 0 } },
			{ { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 1, 0 } },
			{ { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 1, 0 } },
			{ { 0, 2, 0, 0, 0, 2, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 2, 0, 0, 0, 0, 0, 2 }, { 0, 0, 0, 0, 0, 0, 0 }, { 0, 2, 0, 0, 0, 2, 0 } } };
	private int runningTick;
	private final int DURATION_TICK = 450;

	/**
	 * Only available when running ({@link #runningTick}>0
	 */
	private EntityPlayer player;

	/**
	 * Only available when running ({@link #runningTick}>0
	 */
	private ChunkCoordinates[] tips;

	public TileEntityBloodAltar4() {
		super(new Slot[] { new Slot(ModItems.pureBlood, 44, 34), new Slot(ModItems.humanHeart, 80, 34), new Slot(new InventoryTileEntity.IItemSelector() {

			@Override
			public boolean isItemAllowed(ItemStack item) {
				// Placeholder
				return false;
			}
		}, 116, 34) });
	}

	/**
	 * Checks if the given amount of items is present and if that's the case removes them
	 * 
	 * @param bloodMeta
	 *            The meta value of the pure blood bottles which is required
	 * @param blood
	 * @param heart
	 * @param par3
	 * @return
	 */
	private boolean checkAndRemoveItems(int bloodMeta, int blood, int heart, int par3) {
		ItemStack stackPureBlood = this.getStackInSlot(0);
		ItemStack stackHeart = this.getStackInSlot(1);
		ItemStack stack3 = this.getStackInSlot(2);

		if (blood > 0) {
			if (stackPureBlood == null || stackPureBlood.stackSize < blood) {
				Logger.i(TAG, "Pure blood bottles are not present");
				return false;
			}
			if (stackPureBlood.getItemDamage() < bloodMeta) {
				Logger.i(TAG, "Pure blood is of the wrong level (" + stackPureBlood.getItemDamage() + "/" + bloodMeta + ")");
				return false;
			}
		}
		if (heart > 0) {
			if (stackHeart == null || stackHeart.stackSize < heart) {
				Logger.i(TAG, "Hearts are not present");
				return false;
			}
		}
		if (par3 > 0) {
			if (stack3 == null || stack3.stackSize < par3) {
				Logger.i(TAG, "Item 3 is not present");
				return false;
			}
		}
		this.decrStackSize(0, blood);
		this.decrStackSize(1, heart);
		this.decrStackSize(2, par3);
		return true;
	}

	/**
	 * Checks if the blocks in the given box match to the structure description. If it matches it returns the block at the positions marked with 1 in the structure, otherwise its null
	 * 
	 * @param lx
	 *            Lower x
	 * @param ly
	 *            Lower y
	 * @param lz
	 *            Lower z
	 * @param hx
	 *            Higher x
	 * @param hy
	 *            Higher y
	 * @param hz
	 *            Higher z
	 * @param structure
	 * @return
	 */
	private Block checkBlocks(int lx, int ly, int lz, int hx, int hy, int hz, int[][][] structure) {
		Logger.i(TAG, "0.length: " + structure.length + ":" + structure[0].length + ":" + structure[0][0].length);
		Block blocktype = null;
		for (int x = lx; x <= hx; x++) {
			for (int z = lz; z <= hz; z++) {
				for (int y = ly; y <= hy; y++) {
					int type = structure[y - ly][z - lz][x - lx];
					Block b = worldObj.getBlock(x, y, z);
					// Logger.i("Test", "T:" + x + ":" + y + ":" + z + ";" + (x - lx) + ":" + (y - ly) + ":" + (z - lz) + ";" + type + ";" + b.getUnlocalizedName());
					if (type == 0) {
// Disabled for now to prevent grass or so blocking and stop some mod conflicts, can probably be enabled again sometim TODO
//						if (!(b.isAir(worldObj, x, y, z))) {
//							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
//							return null;
//						}
					}
					if (type == 2) {
						if (!(b instanceof BlockBloodAltar4Tip)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 3) {
						if (!(b instanceof BlockBloodAltar4)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 4) {
						if (!(b instanceof BlockBed)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 1) {
						if (blocktype != null && !blocktype.equals(b)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
						blocktype = b;
					}

				}
			}
		}
		return blocktype;
	}

	/**
	 * Checks if the requirements for a level up are met
	 * 
	 * @param player
	 * @param sl
	 *            Level of the structure
	 * @return
	 */
	private LevReq checkLevelRequirement(EntityPlayer player, int sl) {
		if (sl == 0)
			return LevReq.STRUCTURE_WRONG;

		int pl = VampirePlayer.get(player).getLevel();
		if (pl < 4 || pl > REFERENCE.HIGHEST_REACHABLE_LEVEL)
			return LevReq.LEVEL_WRONG;
		if (pl == 4) {
			if (sl != 1)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(0, 0, 5, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 5) {
			if (sl != 1)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(0, 1, 0, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 6) {
			if (sl != 1)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(0, 1, 5, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 7) {
			if (sl != 2)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(1, 1, 0, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 8) {
			if (sl != 2)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(1, 1, 5, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 9) {
			if (sl != 3)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(2, 1, 5, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 10) {
			if (sl != 3)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(2, 1, 5, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 11) {
			if (sl != 4)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(3, 1, 10, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 12) {
			if (sl != 4)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(3, 1, 5, 0))
				return LevReq.ITEM_MISSING;
		} else if (pl == 13) {
			if (sl != 4)
				return LevReq.STRUCTURE_WRONG;
			if (!checkAndRemoveItems(4, 2, 0, 0))
				return LevReq.ITEM_MISSING;
		}
		return LevReq.OK;

	}

	/**
	 * Determines the level of the structure build around the altar. TODO make it rotatable
	 * 
	 * @return
	 */
	private int determineLevel() {

		int x = this.xCoord;
		int y = this.yCoord;
		int z = this.zCoord;
		// setBlocks(x-2,y,z-2,x+2,y+2,z+2,structure1);
		int meta = worldObj.getBlockMetadata(x, y, z);
		Logger.i(TAG, "Meta: " + meta);
		Block type = null;
		Logger.i(TAG, "Testing structure 2");
		type = checkBlocks(x - 3, y, z - 2, x + 3, y + 3, z + 2, structure2);
		if (type != null) {
			if (type instanceof BlockCompressed) {
				if (((BlockCompressed) type).getMapColor(1).equals(MapColor.ironColor)) {
					return 3;
				}
				if (((BlockCompressed) type).getMapColor(1).equals(MapColor.goldColor)) {
					return 4;
				}
			}
		}
		Logger.i(TAG, "Testing structure 1");
		type = checkBlocks(x - 2, y, z - 2, x + 2, y + 2, z + 2, structure1);
		if (type != null) {
			if (type instanceof BlockStoneBrick) {
				return 1;
			}
			if (type instanceof BlockCompressed) {
				if (((BlockCompressed) type).getMapColor(1).equals(MapColor.ironColor)) {
					return 2;
				}
			}
		}
		return 0;

	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	@Override
	public String getInventoryName() {
		return "block.vampirism:bloodAltarTier4.name";
	}

	/**
	 * Returns the phase the ritual is in
	 * 
	 * @return
	 */
	public PHASE getPhase() {
		if (runningTick < 1) {
			return PHASE.NOT_RUNNING;
		}
		if (runningTick == 1) {
			return PHASE.CLEAN_UP;
		}
		if (runningTick > (DURATION_TICK - 100)) {
			return PHASE.PARTICLE_SPREAD;
		}
		if (runningTick < DURATION_TICK - 160 && runningTick >= (DURATION_TICK - 200)) {
			return PHASE.BEAM1;
		}
		if (runningTick < (DURATION_TICK - 200) && (runningTick > 50)) {
			return PHASE.BEAM2;
		}
		if (runningTick == 50) {
			return PHASE.LEVELUP;
		}
		if (runningTick < 50) {
			return PHASE.ENDING;
		}
		return PHASE.WAITING;
	}

	/**
	 * Returns the affected player. If the ritual isn't running it returns null
	 * 
	 * @return
	 */
	public EntityPlayer getPlayer() {
		if (this.runningTick <= 1)
			return null;
		return this.player;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	public int getRunningTick() {
		return runningTick;
	}

	/**
	 * Returns the position of the tips. If the ritual isn't running it returns null
	 * 
	 * @return
	 */
	public ChunkCoordinates[] getTips() {
		if (this.runningTick <= 1)
			return null;
		return this.tips;
	}

	/**
	 * Searchs for the tips and returns their position
	 * 
	 * @param level
	 * @return
	 */
	private ChunkCoordinates[] getTips(int level) {
		ArrayList<ChunkCoordinates> coord = new ArrayList<ChunkCoordinates>();
		int lx = this.xCoord - 3;
		int ly = this.yCoord;
		int lz = this.zCoord - 3;
		int hx = lx + 6;
		int hy = ly + 4;
		int hz = lz + 6;

		for (int x = lx; x <= hx; x++) {
			for (int z = lz; z <= hz; z++) {
				for (int y = ly; y <= hy; y++) {
					if (worldObj.getBlock(x, y, z).equals(ModBlocks.bloodAltar4Tip)) {
						coord.add(new ChunkCoordinates(x, y, z));
					}
				}
			}
		}
		Logger.i("test", "found " + coord.size());
		return coord.toArray(new ChunkCoordinates[coord.size()]);
	}

	/**
	 * Called when the ritual should start
	 * 
	 * @param player
	 */
	public void onBlockActivated(EntityPlayer player) {
		if (runningTick > 0) {
			return;
		}
		int sl = this.determineLevel();

		LevReq result = checkLevelRequirement(player, sl);
		Logger.i(TAG, "SL: " + sl + " Result: " + result);
		if (result != LevReq.OK) {
			if (!this.worldObj.isRemote) {
				if (result == LevReq.ITEM_MISSING)
					player.addChatMessage(new ChatComponentTranslation("text.vampirism:ritual_missing_times"));
				if (result == LevReq.STRUCTURE_WRONG)
					player.addChatMessage(new ChatComponentTranslation("text.vampirism:ritual_structure_wrong"));
				if (result == LevReq.LEVEL_WRONG)
					player.addChatMessage(new ChatComponentTranslation("text.vampirism:ritual_level_wrong"));
			}

			return;
		}
		runningTick = DURATION_TICK;
		this.player = player;
		tips = getTips(sl);
		if (!this.worldObj.isRemote) {
			for (int i = 0; i < tips.length; i++) {
				NBTTagCompound data = new NBTTagCompound();
				data.setInteger("destX", tips[i].posX);
				data.setInteger("destY", tips[i].posY);
				data.setInteger("destZ", tips[i].posZ);
				data.setInteger("age", 100);
				VampirismMod.modChannel.sendToAll(new SpawnCustomParticlePacket(1, this.xCoord, this.yCoord, this.zCoord, 5, data));
			}
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		player.addPotionEffect(new PotionEffect(Potion.resistance.id, DURATION_TICK, 10));
		this.markDirty();

	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.func_148857_g());
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		int tick = tagCompound.getInteger("tick");
		if (tick > 0 && player == null) {
			try {
				this.player = (EntityPlayer) this.worldObj.getEntityByID(tagCompound.getInteger("playerId"));
				this.tips = getTips(this.determineLevel());
				this.runningTick = tick;
			} catch (NullPointerException e) {
			}
		}
		if (player == null) {
			this.runningTick = 0;
			this.tips = null;
		}
	}

	/**
	 * test method to set the blocks
	 * 
	 * @param lx
	 * @param ly
	 * @param lz
	 * @param hx
	 * @param hy
	 * @param hz
	 * @param structure
	 */
	private void setBlocks(int lx, int ly, int lz, int hx, int hy, int hz, int[][][] structure) {

		Block blocktype = null;
		for (int x = lx; x <= hx; x++) {
			for (int z = lz; z <= hz; z++) {
				for (int y = ly; y < hy; y++) {
					int type = structure[y - ly][z - lz][x - lx];
					if (type == 0) {
						worldObj.setBlock(x, y, z, Blocks.air);
					}
					if (type == 2) {
						worldObj.setBlock(x, y, z, ModBlocks.bloodAltar4Tip);
					}
					if (type == 3) {
						worldObj.setBlock(x, y, z, ModBlocks.bloodAltar4);
					}
					if (type == 4) {
						worldObj.setBlock(x, y, z, Blocks.bed);
					}
					if (type == 1) {
						worldObj.setBlock(x, y, z, Blocks.stonebrick);
					}

				}
			}
		}
	}

	@Override
	public void updateEntity() {
		runningTick--;
		if (runningTick <= 0)
			return;
		if (player == null || player.isDead) {
			runningTick = 1;
		} else {
			player.motionX = 0;
			if (player.motionY >= 0) {
				player.motionY = 0;
			} else {
				player.motionY = player.motionY / 2;
			}
			player.motionZ = 0;
		}

		PHASE phase = getPhase();
		if (!this.worldObj.isRemote) {
			if (phase.equals(PHASE.PARTICLE_SPREAD)) {
				if (runningTick % 15 == 0) {
					for (int i = 0; i < tips.length; i++) {
						NBTTagCompound data = new NBTTagCompound();
						data.setInteger("destX", tips[i].posX);
						data.setInteger("destY", tips[i].posY);
						data.setInteger("destZ", tips[i].posZ);
						data.setInteger("age", 60);
						VampirismMod.modChannel.sendToAll(new SpawnCustomParticlePacket(1, this.xCoord, this.yCoord, this.zCoord, 5, data));
					}
				}
			}
			if (runningTick == DURATION_TICK - 200) {
				VampirismMod.modChannel.sendTo(new RenderScreenRedPacket(DURATION_TICK - 250, 50), (EntityPlayerMP) player);
			}
		}

		if (phase.equals(PHASE.CLEAN_UP)) {
			player = null;
			tips = null;
			this.markDirty();
		}
		if (phase.equals(PHASE.LEVELUP)) {
			VampirePlayer.get(player).levelUp();
			if (this.worldObj.isRemote) {
				this.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "random.explode", 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
				this.worldObj.spawnParticle("hugeexplosion", player.posX, player.posY, player.posZ, 1.0D, 0.0D, 0.0D);
			} else {
				player.addPotionEffect(new PotionEffect(ModPotion.saturation.id, 400, 2));
			}
			player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 400, 2));
			player.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 400, 2));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("tick", runningTick);
		if (player != null) {
			tagCompound.setInteger("playerId", player.getEntityId());
		}
	}

}
