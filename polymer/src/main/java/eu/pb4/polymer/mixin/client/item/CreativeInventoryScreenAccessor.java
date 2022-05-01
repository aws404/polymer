package eu.pb4.polymer.mixin.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public interface CreativeInventoryScreenAccessor {
    @Accessor
    static int getSelectedTab() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static void setSelectedTab(int selectedTab) {
        throw new UnsupportedOperationException();
    }
}
