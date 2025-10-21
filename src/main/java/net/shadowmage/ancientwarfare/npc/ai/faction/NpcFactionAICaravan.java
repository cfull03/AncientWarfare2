package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;

/**
 * Faction-specific caravan AI.
 * Moves traders, soldiers, and vehicle drivers toward their assigned caravan destination.
 * Handles both standard ground navigation and vehicle-based movement.
 */
public class NpcFactionAICaravan<T extends EntityCreature> extends EntityAIBase {

    private final T entity;
    private final BlockPos destination;
    private final World world;

    private boolean reached = false;
    private int updateCooldown = 0;

    public NpcFactionAICaravan(T entity, BlockPos destination) {
        this.entity = entity;
        this.world = entity.world;
        this.destination = destination;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (destination == null || entity.isDead) return false;
        return entity.getDistanceSq(destination) > 4.0 && !reached;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (reached || entity.isDead) return false;
        return entity.getDistanceSq(destination) > 4.0;
    }

    @Override
    public void startExecuting() {
        moveEntityTowardDestination();
    }

    @Override
    public void updateTask() {
        if (reached || entity.isDead) return;

        double distanceSq = entity.getDistanceSq(destination);
        if (distanceSq < 4.0) {
            reached = true;
            entity.getNavigator().clearPath();
            return;
        }

        if (--updateCooldown <= 0) {
            updateCooldown = 40 + world.rand.nextInt(40);
            moveEntityTowardDestination();
        }
    }

    private void moveEntityTowardDestination() {
        if (entity instanceof IVehicleUser) {
            ((IVehicleUser) entity).getVehicle().ifPresent(vehicle -> {
                vehicle.moveHelper.setMoveTo(
                        destination.getX() + 0.5,
                        destination.getY(),
                        destination.getZ() + 0.5
                );
                vehicle.moveHelper.onUpdate();
            });
        } else {
            entity.getNavigator().tryMoveToXYZ(
                    destination.getX() + 0.5,
                    destination.getY(),
                    destination.getZ() + 0.5,
                    0.8D
            );
        }
    }

    @Override
    public void resetTask() {
        if (entity.getNavigator() != null) {
            entity.getNavigator().clearPath();
        }
    }

    public boolean hasReachedDestination() {
        return reached;
    }
}

