package com.example;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public record CampaignRewardRequest(
    @NotNull ProductId productId,
    @NotNull Reference<?, ?> id,
    @NotNull CampaignPriority plan,
    @NotNull CampaignCode campaignCode,
    @NotNull Instant now) {}
