package com.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface ProductTable {
  @Nullable
  Product findProductByIdAndArea(@NotNull ProductId productId);
}
