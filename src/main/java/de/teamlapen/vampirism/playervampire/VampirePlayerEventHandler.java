package de.teamlapen.vampirism.playervampire;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class VampirePlayerEventHandler {

	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event){
		if(event.entity instanceof EntityPlayer && VampirePlayer.get((EntityPlayer)event.entity)==null){
			VampirePlayer.register((EntityPlayer)event.entity);
		}
	}
	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event){
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
		{
			VampirePlayer.saveProxyData((EntityPlayer)event.entity);
		}
	}
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
		{
			VampirePlayer.loadProxyData((EntityPlayer)event.entity);
		}
	}
	
}