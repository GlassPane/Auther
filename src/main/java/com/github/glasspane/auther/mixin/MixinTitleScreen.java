package com.github.glasspane.auther.mixin;

import com.github.glasspane.auther.screen.AuthScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    private MixinTitleScreen() {
        super(NarratorManager.EMPTY);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initWidgetsNormal", at = @At("RETURN"))
    private void addAuthButton(int xButtonMargin, int yButtonMargin, CallbackInfo ci) {
        this.addButton(new ButtonWidget(this.width / 2 - 124, yButtonMargin + 72 + 12 + 22, 20, 20, new LiteralText("Auth"), button -> this.client.openScreen(new AuthScreen(this))));
    }
}
