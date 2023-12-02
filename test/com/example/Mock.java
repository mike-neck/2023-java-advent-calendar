package com.example;

import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public enum Mock {
  ;

  public static <T> T mock(@NotNull Class<T> klass) {
    throw new UnsupportedOperationException(klass.getName());
  }

  public interface Calling<T> {
    <V> @NotNull Expectation<V> call(@NotNull Function<? super T, ? extends V> operation);
  }

  public static <T> @NotNull T any(@NotNull Class<T> klass) {
    throw new UnsupportedOperationException();
  }

  public interface Expectation<V> {
    void returnsNull();

    void returns(@NotNull V value);

    void answers(@NotNull Supplier<? extends V> supplier);
  }

  public static <T> @NotNull Calling<T> with(@NotNull T mock) {
    throw new UnsupportedOperationException();
  }
}
