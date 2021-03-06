package de.teamlapen.vampirism;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import de.teamlapen.vampirism.block.BlockBloodAltar1;
import de.teamlapen.vampirism.block.BlockBloodAltar2;
import de.teamlapen.vampirism.block.BlockBloodAltar3;
import de.teamlapen.vampirism.block.BlockBloodAltar4;
import de.teamlapen.vampirism.block.BlockBloodAltar4Tip;
import de.teamlapen.vampirism.block.BlockChurchAltar;
import de.teamlapen.vampirism.block.BlockCoffin;
import de.teamlapen.vampirism.block.BlockCursedEarth;
import de.teamlapen.vampirism.block.MaterialLiquidBlood;
import de.teamlapen.vampirism.block.BlockBloodAltar4Tip.TileEntityBloodAltar4Tip;
import de.teamlapen.vampirism.block.BlockChurchAltar.TileEntityChurchAltar;
import de.teamlapen.vampirism.block.BlockFlower.VampireFlower;
import de.teamlapen.vampirism.tileEntity.TileEntityBloodAltar1;
import de.teamlapen.vampirism.tileEntity.TileEntityBloodAltar2;
import de.teamlapen.vampirism.tileEntity.TileEntityBloodAltar3;
import de.teamlapen.vampirism.tileEntity.TileEntityBloodAltar4;
import de.teamlapen.vampirism.tileEntity.TileEntityCoffin;

public class ModBlocks {

	public static void init() {
		GameRegistry.registerBlock(bloodAltar1, BlockBloodAltar1.name);
		GameRegistry.registerBlock(bloodAltar2, ItemBlock.class, BlockBloodAltar2.name);
		//GameRegistry.registerBlock(bloodAltarTier3, ItemBlock.class,BlockBloodAltarTier3.name);
		GameRegistry.registerBlock(bloodAltar4, ItemBlock.class,BlockBloodAltar4.name);
		GameRegistry.registerBlock(bloodAltar4Tip, ItemBlock.class,BlockBloodAltar4Tip.name);
		GameRegistry.registerBlock(cursedEarth, BlockCursedEarth.name);
		GameRegistry.registerBlock(churchAltar, BlockChurchAltar.name);
		GameRegistry.registerBlock(coffin, BlockCoffin.name);
		GameRegistry.registerTileEntity(TileEntityBloodAltar1.class, "TileEntityBloodAltar");
		GameRegistry.registerTileEntity(TileEntityBloodAltar2.class, "TileEntityBloodAltarTier2");
		//GameRegistry.registerTileEntity(TileEntityBloodAltarTier3.class, "TileEntityBloodAltarTier3");
		GameRegistry.registerTileEntity(TileEntityBloodAltar4.class, "TileEntityBloodAltarTier4");
		GameRegistry.registerTileEntity(TileEntityChurchAltar.class, "TileEntityChurchAltar");
		GameRegistry.registerTileEntity(TileEntityBloodAltar4Tip.class, "TileEntityBloodAltarTier4Tip");
		GameRegistry.registerTileEntity(TileEntityCoffin.class, "TileEntityCoffin");

		// Flowers
		GameRegistry.registerBlock(vampireFlower, VampireFlower.name);
		

	}
	
	public static void registerRecipes(){
		GameRegistry.addRecipe(new ItemStack(bloodAltar2, 1), " X ", "XYX", "ZZZ", 'X', Blocks.glass, 'Y', Items.gold_ingot, 'Z',
				Items.iron_ingot);
		GameRegistry.addRecipe(new ItemStack(bloodAltar4,1), "   ","YZY","ZZZ",'Y',Items.gold_ingot,'Z',Blocks.obsidian);
		GameRegistry.addRecipe(new ItemStack(bloodAltar4Tip,1),"   "," X ","XYX",'X',Items.iron_ingot,'Y',Blocks.iron_block);
	}
	public final static BlockBloodAltar1 bloodAltar1 = new BlockBloodAltar1();
	public final static MaterialLiquid blood = new MaterialLiquidBlood(MapColor.netherrackColor);

	public final static BlockBloodAltar2 bloodAltar2 = new BlockBloodAltar2();
	public final static BlockBloodAltar3 bloodAltar3= new BlockBloodAltar3();
	public final static BlockBloodAltar4 bloodAltar4=new BlockBloodAltar4();
	public final static BlockBloodAltar4Tip bloodAltar4Tip=new BlockBloodAltar4Tip();
	public final static BlockCursedEarth cursedEarth = new BlockCursedEarth();
	public final static BlockChurchAltar churchAltar= new BlockChurchAltar();
	public final static BlockCoffin coffin = new BlockCoffin();
	
	// Flowers
	public final static VampireFlower vampireFlower = new VampireFlower();
}
