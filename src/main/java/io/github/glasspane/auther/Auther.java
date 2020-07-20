package io.github.glasspane.auther;

import io.github.glasspane.auther.mixin.SessionAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public final class Auther {

    private static final Logger logger = LogManager.getFormatterLogger("Auther");
    public static final String MODID = "auther";

    public static Logger getLogger() {
        return logger;
    }

    public static void setMinecraftSession(Session session) {
        ((SessionAccessor) MinecraftClient.getInstance()).setSession(session);
    }
}
