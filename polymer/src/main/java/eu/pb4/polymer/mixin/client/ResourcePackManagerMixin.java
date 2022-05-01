package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.client.rendering.PolymerResourcePack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
@Environment(EnvType.CLIENT)
@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin<T extends ResourcePackProfile> {
    @Shadow
    @Final
    @Mutable
    private Set<ResourcePackProvider> providers;

    @Inject(method = "<init>(Lnet/minecraft/resource/ResourcePackProfile$Factory;[Lnet/minecraft/resource/ResourcePackProvider;)V", at = @At("RETURN"))
    public void polymer_construct(ResourcePackProfile.Factory profileFactory, ResourcePackProvider[] providers, CallbackInfo ci) {
        this.providers = new HashSet<>(this.providers);
        this.providers.add(new PolymerResourcePack.Provider());
    }
}
