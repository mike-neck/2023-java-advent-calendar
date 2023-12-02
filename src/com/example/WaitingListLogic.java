package com.example;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class WaitingListLogic {
  final @NotNull Clock clock;
  final @NotNull URIBuilder uriBuilder;
  final @NotNull IdGenerator idGenerator;

  final @NotNull CustomerStore customerStore;
  final @NotNull ProductTable productTable;
  final @NotNull CampaignEvents campaignEvents;
  final @NotNull SalesStore salesStore;

  WaitingListLogic(
      @NotNull Clock clock,
      @NotNull URIBuilder uriBuilder,
      @NotNull IdGenerator idGenerator,
      @NotNull CustomerStore customerStore,
      @NotNull ProductTable productTable,
      @NotNull CampaignEvents campaignEvents,
      @NotNull SalesStore salesStore) {
    this.clock = clock;
    this.uriBuilder = uriBuilder;
    this.idGenerator = idGenerator;
    this.customerStore = customerStore;
    this.productTable = productTable;
    this.campaignEvents = campaignEvents;
    this.salesStore = salesStore;
  }

  static final @NotNull PathParam<ProductId> PRODUCTS = PathParam.ofId("products");
  static final @NotNull PathParam<WaitingListId> WAITING_LIST = PathParam.ofId("waiting-list");

  static final @NotNull PathParam<BookingId> BOOKINGS = PathParam.ofId("bookings");

  @NotNull
  URI addWaitingList(
      @NotNull CustomerId customerId,
      @NotNull ProductId productId,
      @NotNull Area area,
      @Nullable CampaignCode campaignCode) {
    Customer customer = customerStore.findCustomerByIdAndArea(customerId, area);
    if (customer == null) {
      throw new IllegalArgumentException("no customer found, id { customerId }");
    }
    Product product = productTable.findProductByIdAndArea(productId);
    if (product == null) {
      throw new IllegalArgumentException("no product found, id { productId }");
    }
    if (product.isOnSale() || product.isEndOfSale()) {
      throw new IllegalArgumentException(
          "product has no waiting list, id = { productId }, name = { product.getName() }");
    }
    Instant now = Instant.now(clock);
    CampaignPriority priority =
        campaignCode == null ? customer.getPlan().campaignPriority() : campaignCode.getPriority();
    Collection<WaitingList> waitingList = campaignEvents.findWaitingList(productId, area, now);
    if (waitingList.isEmpty()) {
      if (product.isWaitingListAvailableForArea(area)) {
        SaveNewWaitingList saveNewWaitingList =
            new SaveNewWaitingList(campaignEvents, idGenerator, uriBuilder, priority, now);
        return saveNewWaitingList.saveNewWaitingList(customerId, productId, area, campaignCode);
      } else {
        SaveAsNewBookingWithCampaignReward saveAsNewBookingWithCampaignReward =
            new SaveAsNewBookingWithCampaignReward(
                campaignEvents, salesStore, uriBuilder, priority, now);
        return saveAsNewBookingWithCampaignReward.saveAsNewBookingWithCampaignReward(
            customerId, productId, area, campaignCode);
      }
    } else {
      if (product.isWaitingListAvailableForArea(area)) {
        AddNewWaitingList addNewWaitingList =
            new AddNewWaitingList(
                campaignEvents, idGenerator, uriBuilder, waitingList, priority, now);
        return addNewWaitingList.addNewWaitingList(customerId, productId, area, campaignCode);
      } else {
        SaveAsBookingIfAvailable saveAsBookingIfAvailable =
            new SaveAsBookingIfAvailable(campaignEvents, salesStore, uriBuilder, now);
        return saveAsBookingIfAvailable.saveAsBookingIfAvailable(
            customerId, productId, area, campaignCode);
      }
    }
  }

  interface CampaignApplicationStrategy {
    @NotNull
    URI register(
        @NotNull CustomerId customerId,
        @NotNull ProductId productId,
        @NotNull Area area,
        @Nullable CampaignCode campaignCode);
  }

  record SaveNewWaitingList(
      @NotNull CampaignEvents campaignEvents,
      @NotNull IdGenerator idGenerator,
      @NotNull URIBuilder uriBuilder,
      @NotNull CampaignPriority priority,
      @NotNull Instant now) {
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
          .name(PRODUCTS)
          .value(productId)
          .name(Area.PATH_PARAM)
          .value(area)
          .name(WAITING_LIST)
          .value(waiting.getId())
          .build();
    }
  }

  record SaveAsNewBookingWithCampaignReward(
      @NotNull CampaignEvents campaignEvents,
      @NotNull SalesStore salesStore,
      @NotNull URIBuilder uriBuilder,
      @NotNull CampaignPriority priority,
      @NotNull Instant now) {
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
          .name(PRODUCTS)
          .value(productId)
          .name("contracts")
          .name(BOOKINGS)
          .value(booking.getId())
          .build();
    }
  }

  record AddNewWaitingList(
      @NotNull CampaignEvents campaignEvents,
      @NotNull IdGenerator idGenerator,
      @NotNull URIBuilder uriBuilder,
      @NotNull Collection<WaitingList> waitingList,
      @NotNull CampaignPriority priority,
      @NotNull Instant now) {
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
          .name(PRODUCTS)
          .value(productId)
          .name(WAITING_LIST)
          .value(waiting.getId())
          .build();
    }
  }

  record SaveAsBookingIfAvailable(
      @NotNull CampaignEvents campaignEvents,
      @NotNull SalesStore salesStore,
      @NotNull URIBuilder uriBuilder,
      @NotNull Instant now) {
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
            .name(PRODUCTS)
            .value(productId)
            .name("contracts")
            .name(BOOKINGS)
            .value(booking.getId())
            .build();
      } else {
        campaignEvents()
            .saveExpiredCampaignApplication(customerId, productId, area, now(), campaignCode);
        return uriBuilder().name(PRODUCTS).value(productId).name("expired").build();
      }
    }
  }
}
