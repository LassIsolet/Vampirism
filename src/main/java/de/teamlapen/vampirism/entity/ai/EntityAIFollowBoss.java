package de.teamlapen.vampirism.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIFollowBoss extends EntityAIBase {
	/** The child that is following its parent. */
	IMinion minion;
	IMinionLord boss;
	double speed;
	private int timer;
	/**
	 * Min dist for execution
	 */
	private final int MINDIST = 200;
	/**
	 * Max dist for execution
	 */
	private final int MAXDIST = 600;

	public EntityAIFollowBoss(IMinion minion, double speed) {
		this.minion = minion;
		this.speed = speed;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		if (!this.boss.isTheEntityAlive()) {
			boss = null;
			return false;
		} else {
			double d0 = this.boss.getTheDistanceSquared(minion.getRepresentingEntity());
			return d0 >= MINDIST && d0 <= MAXDIST;
		}
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
		this.boss = null;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		boss = minion.getLord();
		if (boss == null) {
			return false;
		} else {
			double d0 = this.boss.getTheDistanceSquared(minion.getRepresentingEntity());
			return d0 >= MINDIST && d0 <= MAXDIST;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		this.timer = 0;
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		if (--this.timer <= 0) {
			this.timer = 10;
			minion.getRepresentingEntity().getNavigator().tryMoveToEntityLiving(this.boss.getRepresentingEntity(), this.speed);
		}
	}
}
