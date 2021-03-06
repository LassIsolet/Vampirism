package de.teamlapen.vampirism.entity.ai;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import de.teamlapen.vampirism.entity.EntityVampireHunter;
import de.teamlapen.vampirism.entity.player.VampirePlayer;
import de.teamlapen.vampirism.util.Helper;

public class EntityAIModifier {

	public static void addVampireMobTasks(EntityCreature entity) {
		EntityAITasks tasks = entity.tasks;
		// Attack player
		tasks.addTask(1, new EntityAIAttackOnCollide(entity, EntityPlayer.class, 1.0D, false));
		// Attack vampire hunter
		tasks.addTask(1, new EntityAIAttackOnCollide(entity, EntityVampireHunter.class, 1.0D, true));

		EntityAITasks targetTasks = entity.targetTasks;
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(entity, EntityPlayer.class, 0, true, false, new IEntitySelector() {

			@Override
			public boolean isEntityApplicable(Entity entity) {
				if (entity instanceof EntityPlayer) {
					return VampirePlayer.get((EntityPlayer) entity).getLevel() <= 0;
				}
				return false;
			}

		}));
		// Search for vampire hunters
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(entity, EntityVampireHunter.class, 0, true));
	}

	@Deprecated
	private static EntityAITasks getTargetTasks(EntityLiving entity) {
		return (EntityAITasks) Helper.Reflection.getPrivateFinalField(EntityLiving.class, entity, Helper.Obfuscation.getPosNames("EntityLiving/targetTasks"));
	}

	@Deprecated
	private static EntityAITasks getTasks(EntityLiving entity) {
		return (EntityAITasks) Helper.Reflection.getPrivateFinalField(EntityLiving.class, entity, Helper.Obfuscation.getPosNames("EntityLiving/tasks"));
	}

	public static void makeMinion(IMinion minion, EntityCreature entity) {
		EntityAITasks tasks = entity.tasks;
		tasks.taskEntries.clear();
		tasks.addTask(0, new EntityAISwimming(entity));
		tasks.addTask(2, new EntityAIAttackOnCollide(entity, EntityLivingBase.class, 1.0D, false));
		tasks.addTask(4, new EntityAIFollowBoss(minion, 1.0D));

		EntityAITasks targetTasks = entity.targetTasks;
		targetTasks.taskEntries.clear();
		targetTasks.addTask(2, new EntityAIDefendLord(minion));
		targetTasks.addTask(8, new EntityAIHurtByTarget(entity, false));
	}

}
