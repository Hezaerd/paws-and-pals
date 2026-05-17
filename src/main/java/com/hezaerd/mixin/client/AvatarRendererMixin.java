package com.hezaerd.mixin.client;

import com.hezaerd.accessor.AvatarRenderStateAccessor;
import com.hezaerd.accessor.PlayerAccessor;
import com.hezaerd.render.AxolotlOnShoulderLayer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin extends LivingEntityRenderer<Avatar, AvatarRenderState, PlayerModel> {
    private AvatarRendererMixin() {
        super(null, null, 0);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pawspals$addAxolotlShoulderLayer(EntityRendererProvider.Context context, boolean slimSteve, CallbackInfo ci) {
        this.addLayer(new AxolotlOnShoulderLayer(this, context.getModelSet()));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void pawspals$extractAxolotlShoulder(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        AvatarRenderStateAccessor stateAcc = (AvatarRenderStateAccessor) state;
        PlayerAccessor playerAcc = (PlayerAccessor) entity;
        stateAcc.pawspals$setAxolotlOnLeftShoulder(playerAcc.pawspals$getShoulderAxolotlLeft().orElse(null));
        stateAcc.pawspals$setAxolotlOnRightShoulder(playerAcc.pawspals$getShoulderAxolotlRight().orElse(null));
    }
}
