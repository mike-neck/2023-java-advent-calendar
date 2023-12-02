package com.example.campaign;

import com.example.Area;
import com.example.CampaignApplicationStrategy;
import com.example.CampaignCode;
import com.example.CampaignEvents;
import com.example.CampaignPriority;
import com.example.CampaignRewardRequest;
import com.example.CampaignRule;
import com.example.CampaignRuleId;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SaveNewWaitingList(
    @NotNull CampaignEvents campaignEvents,
    @NotNull IdGenerator idGenerator,
    @NotNull URIBuilder uriBuilder,
    @NotNull CampaignPriority priority,
    @NotNull Instant now)
    implements CampaignApplicationStrategy {
  @NotNull
  URI saveNewWaitingList(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    // ウェイティングリストに初めて人が並ぶ場合
    CampaignRule rule = campaignEvents().findRule(productId, area);
    if (rule == null) {
      CampaignRuleId campaignRuleId = idGenerator().generateNew(CampaignRuleId.class);
      rule =
          campaignEvents()
              .createNewRule(productId, area, campaignRuleId, CampaignRule.getDefault());
    }
    WaitingListId waitingListId = idGenerator().generateNew(WaitingListId.class);
    WaitingRequest waitingRequest =
        rule.createRequest(waitingListId, customerId, productId, priority(), area, now());
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
        .name(Area.PATH_PARAM)
        .value(area)
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
    return saveNewWaitingList(customerId, productId, area, campaignCode);
  }
}
