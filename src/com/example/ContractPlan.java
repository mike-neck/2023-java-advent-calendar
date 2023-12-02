package com.example;

import org.jetbrains.annotations.NotNull;

interface ContractPlan {
  @NotNull CampaignPriority campaignPriority();
  @NotNull
  ContractPlan rankUp();
}
