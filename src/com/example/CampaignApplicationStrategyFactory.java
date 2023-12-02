package com.example;

import java.time.Instant;
import java.util.Collection;
import java.util.ServiceLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CampaignApplicationStrategyFactory {
  static @NotNull CampaignApplicationStrategyFactory getInstance(
      @NotNull CampaignEvents campaignEvents,
      @NotNull IdGenerator idGenerator,
      @NotNull SalesStore salesStore,
      @NotNull URIBuilder uriBuilder) {
    ServiceLoader<CampaignApplicationStrategyFactory.Factory> serviceLoader =
        ServiceLoader.load(
            CampaignApplicationStrategyFactory.Factory.class,
            CampaignApplicationStrategyFactory.class.getClassLoader());
    return serviceLoader
        .findFirst()
        .orElseThrow()
        .createInstance(campaignEvents, idGenerator, salesStore, uriBuilder);
  }

  @NotNull
  CampaignApplicationStrategy getStrategy(
      @NotNull Collection<WaitingList> waitingList,
      @NotNull Product product,
      @NotNull CampaignPriority priority,
      @NotNull Area area,
      @NotNull Instant now,
      @Nullable CampaignCode campaignCode);

  interface Factory {
    CampaignApplicationStrategyFactory createInstance(
        @NotNull CampaignEvents campaignEvents,
        @NotNull IdGenerator idGenerator,
        @NotNull SalesStore salesStore,
        @NotNull URIBuilder uriBuilder);
  }
}
