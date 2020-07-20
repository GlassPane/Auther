package com.github.glasspane.auther;

import com.github.glasspane.auther.api.specialized.MojangAuthenticator;
import com.github.glasspane.auther.impl.MojangAuthenticatorImpl;
import com.github.glasspane.auther.mixin.SessionAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public final class Auther implements ClientModInitializer {

    private static final Logger logger = LogManager.getFormatterLogger("Auther");
    private static MojangAuthenticator authenticator;

    public static Logger getLogger() {
        return logger;
    }

    public static MojangAuthenticator getMinecraftAuthenticator() {
        return authenticator;
    }

    @Override
    public void onInitializeClient() {
        authenticator = MojangAuthenticatorImpl.getInstance();
    }

    public static void setMinecraftSession(Session session) {
        ((SessionAccessor) MinecraftClient.getInstance()).setSession(session);
    }
}
