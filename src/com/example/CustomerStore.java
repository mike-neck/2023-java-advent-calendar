package com.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface CustomerStore {
  @Nullable
  Customer findCustomerByIdAndArea(@NotNull CustomerId customerId, @NotNull Area area);
}
