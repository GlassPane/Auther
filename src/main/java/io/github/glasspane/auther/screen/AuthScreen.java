package io.github.glasspane.auther.screen;

import io.github.glasspane.auther.Auther;
import io.github.glasspane.auther.api.specialized.MojangAuthenticator;
import joptsimple.internal.Strings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class AuthScreen extends Screen {

    private final Screen parent;
    private static final int LOGGED_IN_COLOR = 0x2ecc71;
    private static final int NOT_LOGGED_IN_COLOR = 0xe74c3c;
    private TextFieldWidget usernameField;
    private TextFieldWidget passwordField;
    private int ticks;
    private boolean updating = true;
    private MutableText displayText;

    public AuthScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        checkLogin();
    }

    private MutableText createUpdateText() {
        return new TranslatableText("menu.auther.status.session.updating").styled(style -> style.withColor(Formatting.YELLOW));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 50;
        this.usernameField = this.addButton(new TextFieldWidget(this.textRenderer, x + 2, y + 2, 196, 20, this.usernameField, new TranslatableText("menu.auther.field.username")));
        this.passwordField = this.addButton(new TextFieldWidget(this.textRenderer, x + 2, y + 26, 196, 20, this.passwordField, new TranslatableText("menu.auther.field.password")));
        this.passwordField.setRenderTextProvider((text, cursorPos) -> Strings.repeat('*', text.length()));
        this.addButton(new ButtonWidget(x, y + 48, 200, 20, new TranslatableText("menu.auther.button.login"), button -> {
            if (!this.usernameField.getText().trim().isEmpty() && !this.passwordField.getText().trim().isEmpty()) {
                this.updating = true;
                this.displayText = createUpdateText();
                final Session original = client.getSession();
                MojangAuthenticator.getInstance().login(usernameField.getText(), passwordField.getText())
                        .exceptionally(throwable -> {
                            Auther.getLogger().debug("login failure", throwable);
                            return original;
                        })
                        .thenApplyAsync(session -> {
                            Auther.setMinecraftSession(session);
                            return session;
                        }, MinecraftClient.getInstance())
                        .thenAccept(session -> {
                            if(session != original) {
                                Auther.getLogger().info(I18n.translate("log.auther.login.success", session.getUsername()));
                            }
                            else {
                                Auther.getLogger().info(I18n.translate("log.auther.login.failure"));
                            }
                            this.checkLogin();
                        });
            }
        }));
        this.setInitialFocus(usernameField);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        MutableText text = this.displayText;
        if (this.updating) {
            text = text.shallowCopy();
            int dots = this.ticks / 5 % 4;
            for (int i = 1; i <= dots; i++) {
                text.append(" .");
            }
        }
        this.drawCenteredText(matrices, textRenderer, text, this.width / 2, this.height / 2 + 20, 0xFFFFFF);
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        int x1 = this.width / 2 - 100;
        int x2 = x1 + 200;
        int y1 = this.height / 2 - 50;
        int y2 = y1 + 82;
        fill(matrices, x1, y1, x2, y2, 0x7F000000);
    }

    @Override
    public void onClose() {
        this.client.openScreen(parent);
    }

    @Override
    public void tick() {
        ticks++;
        this.usernameField.tick();
        this.passwordField.tick();
        super.tick();
    }

    /**
     * verify the current auth status
     */
    private void checkLogin() {
        this.updating = true;
        this.displayText = createUpdateText();
        MojangAuthenticator.getInstance().isAuthenticated().thenAccept(isLoggedIn -> {
            this.updating = false;
            if (isLoggedIn) {
                this.displayText = new TranslatableText("menu.auther.status.session.valid").styled(style -> style.withColor(TextColor.fromRgb(LOGGED_IN_COLOR)));
            } else {
                this.displayText = new TranslatableText("menu.auther.status.session.invalid").styled(style -> style.withColor(TextColor.fromRgb(NOT_LOGGED_IN_COLOR)));
            }
        });
    }
}
