package de.teamlapen.vampirism.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.teamlapen.vampirism.ModItems;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.entity.player.VampirePlayer;
import de.teamlapen.vampirism.util.Logger;
import de.teamlapen.vampirism.util.REFERENCE;

/**
 * 
 * @author WILLIAM
 *
 */
public class ItemBloodBottle extends ItemGlassBottle {

	/**
	 * Adds the given amount of blood to the bottle
	 * @param stack
	 * @param a amount
	 * @return The amount which could not be added
	 */
	public static int addBlood(ItemStack stack, int a) {
		int amt=stack.getItemDamage() + a;
		int left=0;
		if(amt>MAX_BLOOD){
			left=amt-MAX_BLOOD;
			amt=MAX_BLOOD;
		}
		stack.setItemDamage(amt);
		return left;
	}
	public static int getBlood(ItemStack stack) {
		return stack.getItemDamage();
	}
	
	/**
	 * Returns the first blood bottle found on the hotbar
	 * @param inventory
	 * @param onlyNonFull If true only bottles with space left will be returned
	 * @return
	 */
	public static ItemStack getBloodBottleInInventory(InventoryPlayer inventory,boolean onlyNonFull) {
		int hotbarSize = InventoryPlayer.getHotbarSize();
		for (int i = 0; i < hotbarSize; i++) {
			ItemStack itemStack = inventory.getStackInSlot(i);
			if (itemStack != null && itemStack.getItem().equals(ModItems.bloodBottle)&&(!onlyNonFull||(getBlood(itemStack)<MAX_BLOOD))) {
				return itemStack;
			}
		}
		return null;
	}
	
	/**
	 * Returns the first itemstack of normal glas bottles
	 * @param inventory
	 * @return
	 */
	public static ItemStack getGlasBottleInInventory(InventoryPlayer inventory) {
		int hotbarSize = InventoryPlayer.getHotbarSize();
		for (int i = 0; i < hotbarSize; i++) {
			ItemStack itemStack = inventory.getStackInSlot(i);
			if (itemStack != null && itemStack.getItem().equals(Items.glass_bottle)) {
				return itemStack;
			}
		}
		return null;
	}
	
	public static void removeBlood(ItemStack stack, int a) {
		stack.setItemDamage(Math.max(stack.getItemDamage() - a, 0));
	}
	public static final String name = "bloodBottle";

	public static final String textureBaseName = "blood_bottle_";

	public static final int NUM_ICONS = 10;

	public static final int MAX_BLOOD = (NUM_ICONS * 2) - 1;

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public ItemBloodBottle() {
		setUnlocalizedName(REFERENCE.MODID + ":" + name);
		setHasSubtypes(true);
		setCreativeTab(VampirismMod.tabVampirism);
		this.maxStackSize = 1; // TODO: I want this to not stack when blood is
								// in bottle, but stack when empty
	}

	/**
	 * Gets an icon based on an item's damage value
	 */
	@Override
	public IIcon getIconFromDamage(int index) {
		if (index != 0)
			index = index / 2;
		return icons[index];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(this,1,0));
		list.add(new ItemStack(this, 1, MAX_BLOOD));
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. 
	 * @param itemStack
	 * @param world
	 * @param entityPlayer
	 * This method does: 
	 * 1) Shift + right click adds blood from blood bar 
	 * 2) Right click removes blood and puts it in blood bar 
	 * 3) TODO: Fill blood bottle from any other blood containers (if made)
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if (!world.isRemote) {
			VampirePlayer vampire = VampirePlayer.get(player);

			// Remove blood from blood bar and add to bottle on shift + right click
			if (player.isSneaking()){
				int bloodBottle = getBlood(stack);
				int bloodBar = vampire.getBlood();
				if (bloodBottle < MAX_BLOOD && bloodBar > 0) {
					addBlood(stack, 1);
					vampire.getBloodStats().consumeBlood(1);
				}
			}
			// Add blood to blood bar from bottle on right click
			else {
				int bloodBottle = getBlood(stack);
				int bloodBar = vampire.getBlood();
				if (bloodBottle > 0 && bloodBar < VampirePlayer.MAXBLOOD) {
					removeBlood(stack, 1);
					vampire.getBloodStats().addBlood(1);
				}
			}
		}
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister par1) {
		icons = new IIcon[NUM_ICONS];

		for (int i = 0; i < icons.length; i++) {
			icons[i] = par1.registerIcon(REFERENCE.MODID + ":" + textureBaseName + Integer.toString(i));
		}
	}
}
