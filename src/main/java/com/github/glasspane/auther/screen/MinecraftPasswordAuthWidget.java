package com.github.glasspane.auther.screen;

import com.github.glasspane.auther.Auther;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

//TODO make oauth widget
@Environment(EnvType.CLIENT)
public class MinecraftPasswordAuthWidget extends AbstractButtonWidget {

    private static final int LOGGED_IN_COLOR = 0x2ecc71;
    private static final int NOT_LOGGED_IN_COLOR = 0xe74c3c;
    private final TextFieldWidget usernameField;
    private final TextFieldWidget passwordField;
    private final ButtonWidget loginButton;
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private int ticks = 0;
    private boolean loggedIn;
    private boolean updating;
    private String displayText;

    public MinecraftPasswordAuthWidget(int x, int y, String label) {
        this(x, y, 200, 80, label);
    }

    public MinecraftPasswordAuthWidget(int x, int y, int width, int height, String string_1) {
        super(x, y, width, height, string_1);
        this.usernameField = new TextFieldWidget(this.textRenderer, x + 2, y + 2, width - 4, 20, "Username");
        this.passwordField = new TextFieldWidget(this.textRenderer, x + 2, y + 26, width - 4, 20, "Password");
        this.passwordField.setRenderTextProvider((text, cursorPos) -> new String(new char[text.length()]).replace("\0", "*"));
        this.loginButton = new ButtonWidget(x, y + 48, width, 20, "Login", button -> {
            if(!this.usernameField.getText().trim().isEmpty() && !this.passwordField.getText().trim().isEmpty()) {
                this.updating = true;
                Auther.getMinecraftAuthenticator().login(usernameField.getText(), passwordField.getText()).thenAccept(session -> {
                    Auther.getLogger().info("successfully logged in as {}", session.getUsername());
                    this.checkLogin();
                });
            }
        });
        checkLogin();
    }

    /**
     * verify the current auth status
     */
    private void checkLogin() {
        this.updating = true;
        this.loggedIn = false;
        this.displayText = "updating";
        Auther.getMinecraftAuthenticator().isAuthenticated().thenAccept(isLoggedIn -> {
            this.updating = false;
            if(isLoggedIn) {
                this.loggedIn = true;
                this.displayText = "Logged in!";
            }
            else {
                this.displayText = "Not Logged in!";
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        fill(this.x, this.y, this.x + this.width, this.y + this.height, 0x7F000000);
        this.usernameField.render(mouseX, mouseY, deltaTime);
        this.passwordField.render(mouseX, mouseY, deltaTime);
        this.loginButton.render(mouseX, mouseY, deltaTime);
        String text = this.displayText;
        if(this.updating) {
            int dots = this.ticks / 5 % 4;
            StringBuilder sb = new StringBuilder(text);
            for(int i = 1; i <= dots; i++) {
                sb.append(" .");
            }
            text = sb.toString();
        }
        int textX = this.x + (this.width - this.textRenderer.getStringWidth(this.displayText)) / 2;
        int textY = this.y + this.height - 10;
        this.drawString(this.textRenderer, text, textX, textY, !this.updating && this.loggedIn ? LOGGED_IN_COLOR : NOT_LOGGED_IN_COLOR);
    }

    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        return this.usernameField.mouseClicked(double_1, double_2, int_1) | this.passwordField.mouseClicked(double_1, double_2, int_1) | this.loginButton.mouseClicked(double_1, double_2, int_1);
    }

    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        return this.usernameField.keyPressed(int_1, int_2, int_3) | this.passwordField.keyPressed(int_1, int_2, int_3);
    }

    @Override
    public boolean charTyped(char char_1, int int_1) {
        return this.usernameField.charTyped(char_1, int_1) | this.passwordField.charTyped(char_1, int_1);
    }

    public void tick() {
        ticks++;
        this.usernameField.tick();
        this.passwordField.tick();
    }
}
