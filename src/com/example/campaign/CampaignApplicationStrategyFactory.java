package com.example.campaign;

import com.example.Area;
import com.example.CampaignApplicationStrategy;
import com.example.CampaignCode;
import com.example.CampaignEvents;
import com.example.CampaignPriority;
import com.example.IdGenerator;
import com.example.Product;
import com.example.SalesStore;
import com.example.URIBuilder;
import com.example.WaitingList;
import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CampaignApplicationStrategyFactory(
    @NotNull CampaignEvents campaignEvents,
    @NotNull IdGenerator idGenerator,
    @NotNull SalesStore salesStore,
    @NotNull URIBuilder uriBuilder)
    implements com.example.CampaignApplicationStrategyFactory {

  public static class FactoryImpl
      implements com.example.CampaignApplicationStrategyFactory.Factory {
    @Override
    public com.example.CampaignApplicationStrategyFactory createInstance(
        @NotNull CampaignEvents campaignEvents,
        @NotNull IdGenerator idGenerator,
        @NotNull SalesStore salesStore,
        @NotNull URIBuilder uriBuilder) {
      return new CampaignApplicationStrategyFactory(
          campaignEvents, idGenerator, salesStore, uriBuilder);
    }
  }

  @Override
  @NotNull
  public CampaignApplicationStrategy getStrategy(
      @NotNull Collection<WaitingList> waitingList,
      @NotNull Product product,
      @NotNull CampaignPriority priority,
      @NotNull Area area,
      @NotNull Instant now,
      @Nullable CampaignCode campaignCode) {
    if (waitingList.isEmpty()) {
      if (product.isWaitingListAvailableForArea(area)) {
        return new SaveNewWaitingList(campaignEvents(), idGenerator(), uriBuilder(), priority, now);
      } else {
        return new SaveAsNewBookingWithCampaignReward(
            campaignEvents(), salesStore(), uriBuilder(), priority, now);
      }
    } else {
      if (product.isWaitingListAvailableForArea(area)) {
        return new AddNewWaitingList(
            campaignEvents(), idGenerator(), uriBuilder(), waitingList, priority, now);
      } else {
        return new SaveAsBookingIfAvailable(campaignEvents(), salesStore(), uriBuilder(), now);
      }
    }
  }
}
