package com.example;

import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CampaignApplicationStrategyFactory {
  @NotNull
  CampaignApplicationStrategy getStrategy(
      @NotNull Collection<WaitingList> waitingList,
      @NotNull Product product,
      @NotNull CampaignPriority priority,
      @NotNull Area area,
      @NotNull Instant now,
      @Nullable CampaignCode campaignCode);
}
