package com.hezaerd.render;

import com.hezaerd.PawsPals;
import com.hezaerd.accessor.AvatarRenderStateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

import java.util.EnumMap;
import java.util.Map;

public class AxolotlOnShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final AdultAxolotlModel model;

    private static final Map<Axolotl.Variant, Identifier> TEXTURE_BY_VARIANT = Util.make(
            new EnumMap<>(Axolotl.Variant.class),
            map -> {
                for (Axolotl.Variant variant : Axolotl.Variant.values()) {
                    map.put(variant, Identifier.withDefaultNamespace("textures/entity/axolotl/axolotl_" + variant.getName() + ".png"));
                }
            }
    );

    public AxolotlOnShoulderLayer(final RenderLayerParent<AvatarRenderState, PlayerModel> renderer, final EntityModelSet modelSet) {
        super(renderer);
        this.model = new AdultAxolotlModel(modelSet.bakeLayer(ModelLayers.AXOLOTL));
    }

    public void submit(
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final int lightCoords,
            final AvatarRenderState state,
            final float yRot,
            final float xRot) {
        AvatarRenderStateAccessor accessor = (AvatarRenderStateAccessor) state;

        Axolotl.Variant axolotlOnLeftShoulder = accessor.pawspals$getAxolotlOnLeftShoulder();
        if (axolotlOnLeftShoulder != null) {
            this.submitOnShoulder(poseStack, submitNodeCollector, lightCoords, state, axolotlOnLeftShoulder, yRot, xRot, true);
        }

        Axolotl.Variant axolotlOnRightShoulder = accessor.pawspals$getAxolotlOnRightShoulder();
        if (axolotlOnRightShoulder != null) {
            this.submitOnShoulder(poseStack, submitNodeCollector, lightCoords, state, axolotlOnRightShoulder, yRot, xRot, false);
        }
    }

    private void submitOnShoulder(
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final int lightCoords,
            final AvatarRenderState playerState,
            final Axolotl.Variant variant,
            final float yRot,
            final float xRot,
            final boolean isLeft) {
        Identifier texture = TEXTURE_BY_VARIANT.get(variant);
        if (texture == null) {
            PawsPals.LOGGER.error("Missing texture to render shoulder axolotl variant: {}", variant.getName());
            return;
        }

        poseStack.pushPose();
        poseStack.translate(isLeft ? 0.4F : -0.4F, playerState.isCrouching ? -0.48F : -0.73F, 0.0F);
        poseStack.scale(0.6F, 0.6F, 0.6F);

        AxolotlRenderState axolotlState = new AxolotlRenderState();
        axolotlState.variant = variant;
        axolotlState.ageInTicks = playerState.ageInTicks;
        axolotlState.walkAnimationPos = playerState.walkAnimationPos;
        axolotlState.walkAnimationSpeed = playerState.walkAnimationSpeed;
        axolotlState.yRot = yRot;
        axolotlState.xRot = xRot;
        axolotlState.inWaterFactor = 0.0F;
        axolotlState.onGroundFactor = 1.0F;
        axolotlState.idleOnGroundAnimationState.startIfStopped((int) playerState.ageInTicks);

        submitNodeCollector.submitModel(
                this.model, axolotlState, poseStack, texture, lightCoords, OverlayTexture.NO_OVERLAY, playerState.outlineColor, null
        );
        poseStack.popPose();
    }
}
