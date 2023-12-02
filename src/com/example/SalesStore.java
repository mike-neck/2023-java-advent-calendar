package com.example;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public interface SalesStore {
  @NotNull
  Booking bookPurchaseContract(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @NotNull Instant bookingDateTime);
}
