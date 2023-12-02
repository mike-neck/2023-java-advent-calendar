package com.example.campaign;

import com.example.Area;
import com.example.CampaignApplicationStrategy;
import com.example.CampaignCode;
import com.example.CampaignEvents;
import com.example.CampaignPriority;
import com.example.CampaignRewardRequest;
import com.example.CampaignRule;
import com.example.CustomerId;
import com.example.IdGenerator;
import com.example.ProductId;
import com.example.Reference;
import com.example.URIBuilder;
import com.example.WaitingList;
import com.example.WaitingListId;
import com.example.WaitingListLogic;
import com.example.WaitingRequest;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AddNewWaitingList(
    @NotNull CampaignEvents campaignEvents,
    @NotNull IdGenerator idGenerator,
    @NotNull URIBuilder uriBuilder,
    @NotNull Collection<WaitingList> waitingList,
    @NotNull CampaignPriority priority,
    @NotNull Instant now)
    implements CampaignApplicationStrategy {
  @NotNull
  private URI addNewWaitingList(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    // ウェイティングリストに並ぶ場合、現在の待ち順位を算出して登録
    WaitingListId waitingListId = idGenerator().generateNew(WaitingListId.class);
    CampaignRule rule = campaignEvents().findRule(productId, area);
    if (rule == null) {
      throw new IllegalStateException(
          "no rules found for the waiting list of the product, id = { productId } area = { area }");
    }
    WaitingRequest waitingRequest =
        rule.createRequest(
            waitingList(), waitingListId, customerId, productId, priority(), area, now());
    WaitingList waiting = campaignEvents().createNewWaitingCustomer(waitingRequest);
    if (campaignCode != null) {
      CampaignRewardRequest request =
          new CampaignRewardRequest(
              productId,
              Reference.of(WaitingList.class, waiting.getId()),
              priority(),
              campaignCode,
              now());
      campaignEvents().createCampaignReward(request);
    }
    return uriBuilder()
        .name(WaitingListLogic.PRODUCTS)
        .value(productId)
        .name(WaitingListLogic.WAITING_LIST)
        .value(waiting.getId())
        .build();
  }

  @Override
  public @NotNull URI register(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    return addNewWaitingList(customerId, productId, area, campaignCode);
  }
}
