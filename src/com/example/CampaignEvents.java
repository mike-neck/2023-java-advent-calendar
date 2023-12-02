package com.example;

import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CampaignEvents {

  @NotNull
  Collection<WaitingList> findWaitingList(
      @NotNull ProductId productId, @NotNull Area area, @NotNull Instant startWaitingDateTime);

  @NotNull
  WaitingList createNewWaitingCustomer(@NotNull WaitingRequest waitingRequest);

  void createCampaignReward(@NotNull CampaignRewardRequest campaignRewardRequest);

  @Nullable
  CampaignRule findRule(@NotNull ProductId productId, @NotNull Area area);

  @NotNull
  CampaignRule createNewRule(
      @NotNull ProductId productId,
      @NotNull Area area,
      @NotNull CampaignRuleId campaignRuleId,
      @NotNull CampaignRule rule);

  void saveExpiredCampaignApplication(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @NotNull Instant applicationDateTime,
      @Nullable CampaignCode campaignCode);
}
