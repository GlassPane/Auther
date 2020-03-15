package com.github.glasspane.auther.api;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * simple password-based auth, optionally with username
 */
public interface PasswordAuthenticator<T> extends Authenticator<T> {

    CompletableFuture<T> login(@Nullable String username, String password);

    default CompletableFuture<T> login(String password) {
        return login(null, password);
    }
}
