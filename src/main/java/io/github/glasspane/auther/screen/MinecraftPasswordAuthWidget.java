package io.github.glasspane.auther.screen;

import io.github.glasspane.auther.Auther;
import io.github.glasspane.auther.api.specialized.MojangAuthenticator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

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
    private boolean updating;
    private MutableText displayText;

    public MinecraftPasswordAuthWidget(int x, int y, Text label) {
        this(x, y, 200, 80, label);
    }

    public MinecraftPasswordAuthWidget(int x, int y, int width, int height, Text label) {
        super(x, y, width, height, label);
        this.usernameField = new TextFieldWidget(this.textRenderer, x + 2, y + 2, width - 4, 20, new TranslatableText("menu.auther.field.username"));
        this.passwordField = new TextFieldWidget(this.textRenderer, x + 2, y + 26, width - 4, 20, new TranslatableText("menu.auther.field.password"));
        this.passwordField.setRenderTextProvider((text, cursorPos) -> text.replace("\0", "*"));
        this.loginButton = new ButtonWidget(x, y + 48, width, 20, new TranslatableText("menu.auther.button.login"), button -> {
            if(!this.usernameField.getText().trim().isEmpty() && !this.passwordField.getText().trim().isEmpty()) {
                this.updating = true;
                MojangAuthenticator.getInstance().login(usernameField.getText(), passwordField.getText())
                        .thenApplyAsync(session -> {
                            Auther.setMinecraftSession(session);
                            return session;
                        }, MinecraftClient.getInstance())
                        .thenAccept(session -> {
                    Auther.getLogger().info(I18n.translate("log.auther.login.success"), session.getUsername());
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
        this.displayText = new TranslatableText("menu.auther.status.session.updating").styled(style -> style.withColor(Formatting.YELLOW));
        MojangAuthenticator.getInstance().isAuthenticated().thenAccept(isLoggedIn -> {
            this.updating = false;
            if(isLoggedIn) {
                this.displayText = new TranslatableText("menu.auther.status.session.valid").styled(style -> style.withColor(TextColor.fromRgb(LOGGED_IN_COLOR)));
            }
            else {
                this.displayText = new TranslatableText("menu.auther.status.session.invalid").styled(style -> style.withColor(TextColor.fromRgb(NOT_LOGGED_IN_COLOR)));
            }
        });
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float deltaTime) {
        fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x7F000000);
        this.usernameField.render(matrices, mouseX, mouseY, deltaTime);
        this.passwordField.render(matrices, mouseX, mouseY, deltaTime);
        this.loginButton.render(matrices, mouseX, mouseY, deltaTime);
        MutableText text = this.displayText.shallowCopy();
        if(this.updating) {
            int dots = this.ticks / 5 % 4;
            for(int i = 1; i <= dots; i++) {
                text.append(" .");
            }
        }
        int textX = this.x + this.width / 2;
        int textY = this.y + this.height - 10;
        this.drawCenteredText(matrices, textRenderer, text, textX, textY, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.usernameField.mouseClicked(mouseX, mouseY, button) | this.passwordField.mouseClicked(mouseX, mouseY, button) | this.loginButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.usernameField.keyPressed(keyCode, scanCode, modifiers) | this.passwordField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return this.usernameField.charTyped(chr, keyCode) | this.passwordField.charTyped(chr, keyCode);
    }

    public void tick() {
        ticks++;
        this.usernameField.tick();
        this.passwordField.tick();
    }
}
