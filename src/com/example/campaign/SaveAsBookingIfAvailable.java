package com.example.campaign;

import com.example.Area;
import com.example.Booking;
import com.example.CampaignApplicationStrategy;
import com.example.CampaignCode;
import com.example.CampaignEvents;
import com.example.CampaignRule;
import com.example.CustomerId;
import com.example.ProductId;
import com.example.SalesStore;
import com.example.URIBuilder;
import com.example.WaitingListLogic;
import java.net.URI;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SaveAsBookingIfAvailable(
    @NotNull CampaignEvents campaignEvents,
    @NotNull SalesStore salesStore,
    @NotNull URIBuilder uriBuilder,
    @NotNull Instant now)
    implements CampaignApplicationStrategy {
  @NotNull
  private URI saveAsBookingIfAvailable(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    // Waiting List を締め切った場合
    CampaignRule rule = campaignEvents().findRule(productId, area);
    if (rule != null && rule.acceptsBookingAfterCampaignDeadline()) {
      Booking booking = salesStore().bookPurchaseContract(customerId, productId, area, now());
      return uriBuilder()
          .name(WaitingListLogic.PRODUCTS)
          .value(productId)
          .name("contracts")
          .name(WaitingListLogic.BOOKINGS)
          .value(booking.getId())
          .build();
    } else {
      campaignEvents()
          .saveExpiredCampaignApplication(customerId, productId, area, now(), campaignCode);
      return uriBuilder().name(WaitingListLogic.PRODUCTS).value(productId).name("expired").build();
    }
  }

  @Override
  public @NotNull URI register(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    return saveAsBookingIfAvailable(customerId, productId, area, campaignCode);
  }
}
