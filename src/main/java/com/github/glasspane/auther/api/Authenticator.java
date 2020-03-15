package com.github.glasspane.auther.api;

import java.util.concurrent.CompletableFuture;

public interface Authenticator<T> {

    T getUserInformation();

    CompletableFuture<Boolean> isAuthenticated();
}
