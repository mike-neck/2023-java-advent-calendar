package com.example;

import org.jetbrains.annotations.NotNull;

public record Reference<T, I extends Id<T>>(
        @NotNull String name,
        @NotNull I id
) {
    public static <T, I extends Id<T>> @NotNull Reference<T, I> of(@NotNull Class<T> klass, @NotNull I id) {
        return new Reference<>(klass.getSimpleName(), id);
    }
}
