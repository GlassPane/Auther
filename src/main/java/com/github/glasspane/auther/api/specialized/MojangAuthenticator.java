package com.github.glasspane.auther.api.specialized;

import com.github.glasspane.auther.api.PasswordAuthenticator;
import net.minecraft.client.util.Session;

import java.util.concurrent.CompletableFuture;

public interface MojangAuthenticator extends PasswordAuthenticator<Session> {

    /**
     * verifies the credentials against Mojang's session server and updates the current Session.
     *
     * @param username the Mojang Account username
     * @param password the Mojang Account password
     */
    @Override
    CompletableFuture<Session> login(String username, String password);

    /**
     * tests the current token by emulating a server handshake
     *
     * @return whether or not the current token can be used to join an online-mode server
     */
    @Override
    CompletableFuture<Boolean> isAuthenticated();

    /**
     * lets the player switch username to an "offline mode" account, i.e. without logging in to Mojang servers
     *
     * @param username the new username
     */
    CompletableFuture<Session> offlineLogin(String username);
}
