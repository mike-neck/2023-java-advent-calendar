package com.example;

import org.jetbrains.annotations.NotNull;

public interface PathParam<T> {

    static <T, I extends Id<T>> @NotNull PathParam<I> ofId(@NotNull String name) {
        return new PathParam<>() {};
    }
}
