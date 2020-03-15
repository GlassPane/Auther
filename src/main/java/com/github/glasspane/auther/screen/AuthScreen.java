package com.github.glasspane.auther.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

@Environment(EnvType.CLIENT)
public class AuthScreen extends Screen {

    private final Screen parent;
    private MinecraftPasswordAuthWidget authWidget;

    public AuthScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.authWidget = this.addButton(new MinecraftPasswordAuthWidget(this.width / 2 - 100, this.height / 2 - 50, ""));
    }

    @Override
    public void tick() {
        super.tick();
        if(this.authWidget != null) this.authWidget.tick();
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        super.render(mouseX, mouseY, deltaTime);
    }

    @Override
    public void onClose() {
        this.minecraft.openScreen(parent);
    }
}
