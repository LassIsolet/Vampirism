package de.teamlapen.vampirism.network;

import io.netty.buffer.ByteBuf;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import de.teamlapen.vampirism.client.gui.VampireHudOverlay;
import de.teamlapen.vampirism.util.Helper;
import de.teamlapen.vampirism.util.Logger;

/**
 * Used to tell the client it should render the screen reddish
 * @author Maxanier
 *
 */
public class RenderScreenRedPacket implements IMessage {

	public static class Handler implements IMessageHandler<RenderScreenRedPacket, IMessage> {

		@Override
		public IMessage onMessage(RenderScreenRedPacket message, MessageContext ctx) {
			FMLCommonHandler.instance().bus().register(new RenderScreenRedManager(message.durationOn,message.durationOff));
			return null;
		}

	}

	private int durationOn;
	private int durationOff;
	/**
	 * Dont use
	 */
	public RenderScreenRedPacket() {

	}


	/**
	 * 
	 * @param durationOn duration(in ticks) which should it take to turn full red
	 * @param durationOff duration(in ticks) which should it take to turn normal again
	 */
	public RenderScreenRedPacket(int durationOn,int durationOff) {
		this.durationOff=durationOff;
		this.durationOn=durationOn;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		durationOn=tag.getInteger("on");
		durationOff=tag.getInteger("off");

	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("on", durationOn);
		tag.setInteger("off", durationOff);
		ByteBufUtils.writeTag(buf, tag);

	}
	
	public static class RenderScreenRedManager{
		private final int on,off;
		private int tick;
		
		public RenderScreenRedManager(int durationOn,int durationOff){
			on=durationOn;
			off=durationOff;
			tick=on;
		}
		
		@SubscribeEvent
		public void onWorldTick(ClientTickEvent event){
			if(event.phase.equals(TickEvent.Phase.END)){
				if(tick<-off){
					VampireHudOverlay.setRenderRed(0);
					FMLCommonHandler.instance().bus().unregister(this);
					return;
				}
				if(tick>=0){
					VampireHudOverlay.setRenderRed(1F-((float)tick)/((float)(on))*1F);
				}
				else{
					VampireHudOverlay.setRenderRed(((float)tick+(float)off)/(float)off);
				}
				tick--;
			}
		}
	}

}
