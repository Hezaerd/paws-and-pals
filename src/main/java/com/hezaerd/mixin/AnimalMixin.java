package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlAccessor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin {
    @Inject(method = "handleEntityEvent(B)V", at = @At("HEAD"), cancellable = true)
    private void pawspals$onHandleEntity(byte id, CallbackInfo ci) {
        if (!(((Object) this) instanceof Axolotl)) return;
        AxolotlAccessor accessor = (AxolotlAccessor) this;
        if (id == 7) {
            accessor.pawspals$spawnTamingParticles(true);
            ci.cancel();
        } else if (id == 6) {
            accessor.pawspals$spawnTamingParticles(false);
            ci.cancel();
        }
    }
}
