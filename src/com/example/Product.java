package com.example;

import org.jetbrains.annotations.NotNull;

public interface Product {
  boolean isOnSale();

  boolean isEndOfSale();

  boolean isWaitingListAvailableForArea(@NotNull Area area);

  @NotNull
  ProductId getId();
}
