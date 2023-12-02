package com.example;

import org.jetbrains.annotations.NotNull;

public interface ContractPlan {
  @NotNull CampaignPriority campaignPriority();
  @NotNull
  ContractPlan rankUp();
}
