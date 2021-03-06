package io.github.glasspane.auther.impl;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import io.github.glasspane.auther.api.specialized.MojangAuthenticator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class MojangAuthenticatorImpl implements MojangAuthenticator {

    private static final Executor AUTH_THREAD = Executors.newSingleThreadExecutor(r -> new Thread(r, "Auther Authentication Thread"));
    private static final MojangAuthenticatorImpl INSTANCE = new MojangAuthenticatorImpl();
    private final String uniqueToken = UUIDTypeAdapter.fromUUID(UUID.randomUUID());
    private final YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy(), uniqueToken);
    private final MinecraftSessionService sessionService = authenticationService.createMinecraftSessionService();

    private MojangAuthenticatorImpl() {
        //NO-OP
    }

    public static MojangAuthenticator getInstance() {
        return INSTANCE;
    }

    /**
     * @deprecated see {@link #login(String, String)}
     */
    @Deprecated
    @Override
    public CompletableFuture<Session> login(String password) {
        throw new UnsupportedOperationException("must provide a username");
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    @Override
    public final CompletableFuture<Session> login(String username, String password) {
        //copy the arguments to be extra safe
        final String usernameIntern = new String(username);
        final String passwordIntern = new String(password);
        return CompletableFuture.supplyAsync(() -> {
            final YggdrasilUserAuthentication userAuthentication = (YggdrasilUserAuthentication) authenticationService.createUserAuthentication(Agent.MINECRAFT);
            try {
                userAuthentication.setUsername(usernameIntern);
                userAuthentication.setPassword(passwordIntern);
                userAuthentication.logIn(); //if this doesn't throw we are logged in
                String uName = userAuthentication.getSelectedProfile().getName();
                UUID uuid = userAuthentication.getSelectedProfile().getId();
                String accessToken = userAuthentication.getAuthenticatedToken();
                String sessionType = userAuthentication.getUserType().getName();
                return new Session(uName, UUIDTypeAdapter.fromUUID(uuid), accessToken, sessionType);
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            } finally {
                userAuthentication.logOut();
            }
        }, AUTH_THREAD);
    }

    @Override
    public final CompletableFuture<Boolean> isAuthenticated() {
        Session currentSession = this.getUserInformation();
        GameProfile profile = currentSession.getProfile();
        String token = currentSession.getAccessToken();
        String randomID = UUIDTypeAdapter.fromUUID(UUID.randomUUID());
        return CompletableFuture.supplyAsync(() -> {
            try {
                sessionService.joinServer(profile, token, randomID);
                @Nullable GameProfile gp = sessionService.hasJoinedServer(profile, randomID, null);
                return gp != null && gp.isComplete();
            } catch (AuthenticationException e) {
                throw new RuntimeException("unable to validate token", e);
            }
        }, AUTH_THREAD).exceptionally(throwable -> false);
    }

    @Override
    public Session getUserInformation() {
        return MinecraftClient.getInstance().getSession();
    }

    @Override
    public final CompletableFuture<Session> offlineLogin(String username) {
        String id = UUIDTypeAdapter.fromUUID(PlayerEntity.getOfflinePlayerUuid(username));
        return CompletableFuture.completedFuture(new Session(username, id, "invalid", UserType.LEGACY.getName()));
    }
}
