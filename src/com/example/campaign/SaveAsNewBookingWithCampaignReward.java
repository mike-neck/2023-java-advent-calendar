package com.example.campaign;

import com.example.Area;
import com.example.Booking;
import com.example.CampaignApplicationStrategy;
import com.example.CampaignCode;
import com.example.CampaignEvents;
import com.example.CampaignPriority;
import com.example.CampaignRewardRequest;
import com.example.CustomerId;
import com.example.ProductId;
import com.example.Reference;
import com.example.SalesStore;
import com.example.URIBuilder;
import com.example.WaitingListLogic;
import java.net.URI;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SaveAsNewBookingWithCampaignReward(
    @NotNull CampaignEvents campaignEvents,
    @NotNull SalesStore salesStore,
    @NotNull URIBuilder uriBuilder,
    @NotNull CampaignPriority priority,
    @NotNull Instant now)
    implements CampaignApplicationStrategy {
  @NotNull
  private URI saveAsNewBookingWithCampaignReward(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    // ウェイティングリストが終了している場合は予約として扱う
    Booking booking = salesStore().bookPurchaseContract(customerId, productId, area, now());
    if (campaignCode != null) {
      CampaignRewardRequest request =
          new CampaignRewardRequest(
              productId,
              Reference.of(Booking.class, booking.getId()),
              priority(),
              campaignCode,
              now());
      campaignEvents().createCampaignReward(request);
    }
    return uriBuilder()
        .name(WaitingListLogic.PRODUCTS)
        .value(productId)
        .name("contracts")
        .name(WaitingListLogic.BOOKINGS)
        .value(booking.getId())
        .build();
  }

  @Override
  public @NotNull URI register(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    return saveAsNewBookingWithCampaignReward(customerId, productId, area, campaignCode);
  }
}
