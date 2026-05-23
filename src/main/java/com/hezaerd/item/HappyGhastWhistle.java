package com.hezaerd.item;

import com.hezaerd.accessor.HappyGhastAccessor;
import com.hezaerd.advancement.ModCriteria;
import com.hezaerd.utils.TranslationKeys;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Optional;

public class HappyGhastWhistle extends Item {
    private static final int MIN_BLOW_TICKS = 10; // 5 seconds
    private static final double RECALL_RADIUS = 128.0;
    private static final int RECALL_DURATION_TICKS = 200; // 10 seconds

    public HappyGhastWhistle(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity user) {
        return 72000;
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack stack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    private static int getTimeHeld(ItemStack stack, LivingEntity user, int remainingTick) {
        return stack.getUseDuration(user) - remainingTick;
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTicks) {
        if (!(entity instanceof Player player)) return false;
        int timeHeld = getTimeHeld(stack, entity, remainingTicks);
        if (timeHeld < MIN_BLOW_TICKS) {
            if (level.isClientSide()) {
                spawnCallFizzle(level, player, 0.25F);
            } else if (level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, player.blockPosition(),
                        SoundEvents.BREEZE_IDLE_GROUND, SoundSource.PLAYERS, 0.3F, 1.8F);
            }
            return false;
        }
        float power = BowItem.getPowerForTime(Math.min(timeHeld, 20));
        Optional<HappyGhast> nearest = findNearestEligibleGhast(level, player);

        if (level.isClientSide()) {
            if (nearest.isPresent()) {
                spawnCallBeam(level, player, nearest.get(), power);
            } else {
                spawnCallFizzle(level, player, 0.55F);
            }
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)) return false;
        if (nearest.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendOverlayMessage(
                        Component.translatable(TranslationKeys.Message.HappyGhastWhistle.NoTarget));
            }
            return false;
        }
        HappyGhast ghast = nearest.get();
        ((HappyGhastAccessor) ghast).pawspals$startRecall(player, RECALL_DURATION_TICKS);
        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.GOAT_HORN_SOUND_VARIANTS.getFirst().value(),
                SoundSource.PLAYERS, 0.6F + power * 0.4F, 1.0F + power * 0.2F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(
                    Component.translatable(TranslationKeys.Message.HappyGhastWhistle.Success));
            ModCriteria.CALLED_HAPPY_GHAST.trigger(serverPlayer);
        }
        player.getCooldowns().addCooldown(stack, 150);
        return true;
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity entity, @NonNull ItemStack stack, int remainingTicks) {
        int timeHeld = getTimeHeld(stack, entity, remainingTicks);
        if (timeHeld < 1) return;
        spawnVortexBreathParticles(level, entity, timeHeld);
    }


    private static Optional<HappyGhast> findNearestEligibleGhast(Level level, Player player) {
        AABB searchBox = player.getBoundingBox().inflate(RECALL_RADIUS);
        return level.getEntitiesOfClass(HappyGhast.class, searchBox, HappyGhastWhistle::isEligible)
                .stream()
                .min(Comparator.comparingDouble(g -> g.distanceToSqr(player)));
    }

    private static boolean isEligible(HappyGhast ghast) {
        return !ghast.isBaby()
                && !ghast.isVehicle()
                && !ghast.isOnStillTimeout();
    }

    private static void spawnVortexBreathParticles(Level level, LivingEntity entity, int timeHeld) {
        if (!level.isClientSide()) return;
        float charge = Mth.clamp(timeHeld / (float) MIN_BLOW_TICKS, 0.0F, 1.0F);
        if (charge <= 0.0F) return;
        RandomSource random = entity.getRandom();
        Vec3 look = entity.getViewVector(1.0F).normalize();
        // Horizontal basis from look — swirl stays readable while looking around
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-4) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }
        Vec3 forward = new Vec3(-look.x, 0.0, -look.z);
        if (forward.lengthSqr() < 1.0E-4) {
            forward = new Vec3(0.0, 0.0, 1.0);
        } else {
            forward = forward.normalize();
        }
        // Vortex center: upper chest / mouth
        Vec3 target = entity.getEyePosition(1.0F)
                .add(0.0, -0.35, 0.0)
                .add(look.scale(0.15));
        // Spin faster as charge builds; whole spiral rotates over time
        double spin = timeHeld * (0.12 + charge * 0.10);
        int arms = 2; // dual spiral — use 1 for simpler, 3 for busier
        int perArm = 1 + (int) (charge * 2);
        double minRadius = 1.1;
        double maxRadius = 2.8 - charge * 0.7;
        for (int arm = 0; arm < arms; arm++) {
            double armOffset = (Math.PI * 2.0 / arms) * arm;
            for (int i = 0; i < perArm; i++) {
                double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
                // Helix: angle rotates with time + arm + per-particle offset
                double angle = spin + armOffset + i * 0.9 + random.nextDouble() * 0.4;
                // Vertical pitch along the helix
                double height = Math.sin(angle * 1.5) * (0.35 + charge * 0.45);
                // Position on the ring (horizontal plane around player)
                Vec3 offset = right.scale(Math.cos(angle) * radius)
                        .add(forward.scale(Math.sin(angle) * radius))
                        .add(0.0, height, 0.0);
                Vec3 spawn = target.add(offset);
                Vec3 toTarget = target.subtract(spawn);
                if (toTarget.lengthSqr() < 1.0E-6) continue;
                Vec3 radialDir = toTarget.normalize();
                // Tangential = perpendicular to radius in horizontal swirl plane
                // right * -sin(angle) + forward * cos(angle) matches the ring direction
                Vec3 tangentialDir = right.scale(-Math.sin(angle))
                        .add(forward.scale(Math.cos(angle)))
                        .normalize();
                // Slight downward pull toward chest height
                Vec3 vel = radialDir.scale(0.04 + charge * 0.14)
                        .add(tangentialDir.scale(0.06 + charge * 0.16))
                        .add(0.0, -0.01 - charge * 0.02, 0.0);
                level.addParticle(ParticleTypes.CLOUD,
                        spawn.x, spawn.y, spawn.z,
                        vel.x, vel.y, vel.z);
                // Every other particle on the opposite side of the swirl
                if (random.nextFloat() < 0.35F) {
                    level.addParticle(ParticleTypes.WHITE_SMOKE,
                            spawn.x, spawn.y, spawn.z,
                            vel.x * 0.75, vel.y * 0.75, vel.z * 0.75);
                }
            }
        }
    }

    private static void spawnCallBeam(Level level, Player player, HappyGhast ghast, float power) {
        if (!level.isClientSide()) return;
        RandomSource random = player.getRandom();
        Vec3 origin = getCallOrigin(player);
        Vec3 target = getGhastCallTarget(ghast);
        Vec3 delta = target.subtract(origin);
        double distance = delta.length();
        if (distance < 1.0E-4) return;
        Vec3 direction = delta.scale(1.0 / distance);
        Vec3 lateral = direction.cross(new Vec3(0.0, 1.0, 0.0));
        if (lateral.lengthSqr() < 1.0E-4) {
            lateral = new Vec3(1.0, 0.0, 0.0);
        } else {
            lateral = lateral.normalize();
        }
        int segments = Mth.clamp(8 + (int) (distance * 0.35F) + (int) (power * 6), 8, 40);
        double speed = 0.10 + power * 0.18;
        for (int step = 0; step <= segments; step++) {
            double t = step / (double) segments;
            double wobble = Math.sin(t * Math.PI * 4.0 + player.tickCount * 0.4) * (0.08 + power * 0.06);
            Vec3 point = origin.add(delta.scale(t)).add(lateral.scale(wobble));
            Vec3 vel = direction.scale(speed)
                    .add(0.0, (random.nextDouble() - 0.5) * 0.02, 0.0);
            level.addParticle(ParticleTypes.CLOUD, point.x, point.y, point.z, vel.x, vel.y, vel.z);
            if (step % 2 == 0 && random.nextFloat() < 0.45F) {
                level.addParticle(ParticleTypes.WHITE_SMOKE,
                        point.x, point.y, point.z,
                        vel.x * 0.7, vel.y * 0.7, vel.z * 0.7);
            }
        }
        for (int i = 0; i < 4 + (int) (power * 5); i++) {
            double spread = 0.35 + random.nextDouble() * 0.55;
            double angle = random.nextDouble() * Math.PI * 2.0;
            Vec3 ping = target.add(
                    Math.cos(angle) * spread,
                    (random.nextDouble() - 0.3) * spread,
                    Math.sin(angle) * spread
            );
            level.addParticle(ParticleTypes.CLOUD,
                    ping.x, ping.y, ping.z,
                    (random.nextDouble() - 0.5) * 0.03,
                    0.02 + random.nextDouble() * 0.03,
                    (random.nextDouble() - 0.5) * 0.03);
        }
    }

    private static void spawnCallFizzle(Level level, Player player, float intensity) {
        if (!level.isClientSide()) return;
        RandomSource random = player.getRandom();
        Vec3 origin = getCallOrigin(player);
        int count = 3 + (int) (intensity * 8);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double spread = 0.08 + random.nextDouble() * 0.22 * intensity;
            Vec3 vel = new Vec3(
                    Math.cos(angle) * spread * 0.4,
                    0.02 + random.nextDouble() * 0.04,
                    Math.sin(angle) * spread * 0.4
            );
            level.addParticle(ParticleTypes.WHITE_SMOKE,
                    origin.x, origin.y, origin.z,
                    vel.x, vel.y, vel.z);
        }
    }

    private static Vec3 getCallOrigin(Player player) {
        Vec3 look = player.getViewVector(1.0F);
        return player.getEyePosition(1.0F)
                .add(0.0, -0.35, 0.0)
                .add(look.scale(0.2));
    }

    private static Vec3 getGhastCallTarget(HappyGhast ghast) {
        return ghast.getBoundingBox().getCenter().add(0.0, ghast.getBbHeight() * 0.15, 0.0);
    }
}
