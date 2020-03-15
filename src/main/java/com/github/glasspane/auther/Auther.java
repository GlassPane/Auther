package com.github.glasspane.auther;

import com.github.glasspane.auther.api.specialized.MinecraftAuthenticator;
import com.github.glasspane.auther.impl.MinecraftAuthenticatorImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public final class Auther implements ClientModInitializer {

    private static final Logger logger = LogManager.getFormatterLogger("Auther");
    private static MinecraftAuthenticator authenticator;

    public static Logger getLogger() {
        return logger;
    }

    public static MinecraftAuthenticator getMinecraftAuthenticator() {
        return authenticator;
    }

    @Override
    public void onInitializeClient() {
        authenticator = MinecraftAuthenticatorImpl.getInstance();
    }
}
