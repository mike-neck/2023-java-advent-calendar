package com.example;

import static com.example.Mock.mock;
import static com.example.Mock.with;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaveNewWaitingListTest {
  CampaignEvents campaignEvents;
  IdGenerator idGenerator;
  URIBuilder uriBuilder;

  @BeforeEach
  void setup() {
    this.campaignEvents = mock(CampaignEvents.class);
    this.idGenerator = mock(IdGenerator.class);
    this.uriBuilder = mock(URIBuilder.class);
  }

  static final Instant OPERATION_DATE_TIME =
      OffsetDateTime.of(2023, 1, 2, 15, 6, 7, 8_000_000, ZoneOffset.ofHours(9)).toInstant();

  @Test
  void testCreateNewWaitingWhenCampaignCodeIsNull() {
    CustomerId customerId = customerId(1003);
    ProductId productId = productId(10);
    Area area = Area.Fixed.TOKYO;
    CampaignRule rule = mock(CampaignRule.class);
    with(campaignEvents).call(ce -> ce.findRule(productId, area)).returns(rule);
    WaitingListId waitingListId = waitingListId("0000-1111");
    with(idGenerator).call(ig -> ig.generateNew(WaitingListId.class)).returns(waitingListId);
    CampaignPriority priority = campaignPriority(10, 25, 50);
    WaitingRequest waitingRequest = mock(WaitingRequest.class);
    with(rule)
        .call(
            r ->
                r.createRequest(
                    waitingListId, customerId, productId, priority, area, OPERATION_DATE_TIME))
        .returns(waitingRequest);
    WaitingList waitingList = mock(WaitingList.class);
    with(campaignEvents)
        .call(ce -> ce.createNewWaitingCustomer(waitingRequest))
        .returns(waitingList);
    with(waitingList).call(WaitingList::getId).returns(waitingListId);

    WaitingListLogic.SaveNewWaitingList saveNewWaitingList =
        new WaitingListLogic.SaveNewWaitingList(
            campaignEvents, idGenerator, uriBuilder, priority, OPERATION_DATE_TIME);
    URI uri = saveNewWaitingList.register(customerId, productId, area, null);
    assertTrue(uri.toASCIIString().contains("product/10/"));
    assertTrue(uri.toASCIIString().contains("waiting-list/0000-1111"));
  }

  private static @NotNull CustomerId customerId(int id) {
    throw new UnsupportedOperationException();
  }

  private static @NotNull ProductId productId(int id) {
    throw new UnsupportedOperationException();
  }

  private static @NotNull CampaignCode campaignCode(@NotNull String code) {
    throw new UnsupportedOperationException();
  }

  private static @NotNull CampaignPriority campaignPriority(int factor1, int factor2, int factor3) {
    throw new UnsupportedOperationException();
  }

  private static @NotNull WaitingListId waitingListId(@NotNull String value) {
    throw new UnsupportedOperationException();
  }
}
