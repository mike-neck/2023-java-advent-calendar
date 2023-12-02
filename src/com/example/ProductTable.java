package com.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProductTable {
  @Nullable
  Product findProductByIdAndArea(@NotNull ProductId productId);
}
