package com.example.learningplatform.service;

import com.example.learningplatform.exception.NotFoundException;

import java.util.Optional;

public final class Lookups {

    private Lookups() {
    }

    public static <T> T required(Optional<T> found, String message) {
        return found.orElseThrow(() -> new NotFoundException(message));
    }
}
