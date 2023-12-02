package com.example;

import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

interface CampaignRule {
  static @NotNull CampaignRule getDefault() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  CampaignRuleId getId();

  @NotNull
  WaitingRequest createRequest(
      @NotNull WaitingListId waitingListId,
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull CampaignPriority plan,
      @NotNull Area area,
      @NotNull Instant startWaitingDateTime);

  @NotNull
  WaitingRequest createRequest(
      @NotNull Collection<WaitingList> waitingList,
      @NotNull WaitingListId waitingListId,
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull CampaignPriority plan,
      @NotNull Area area,
      @NotNull Instant startWaitingDateTime);

  boolean acceptsBookingAfterCampaignDeadline();
}
