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
  final @NotNull CampaignApplicationStrategyFactory campaignApplicationStrategyFactory;

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
    this.campaignApplicationStrategyFactory =
        CampaignApplicationStrategyFactory.getInstance(
            campaignEvents, idGenerator, salesStore, uriBuilder);
  }

  public static final @NotNull PathParam<ProductId> PRODUCTS = PathParam.ofId("products");
  public static final @NotNull PathParam<WaitingListId> WAITING_LIST =
      PathParam.ofId("waiting-list");

  public static final @NotNull PathParam<BookingId> BOOKINGS = PathParam.ofId("bookings");

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
    Collection<WaitingList> waitingList =
        campaignEvents.findWaitingList(product.getId(), area, now);
    CampaignApplicationStrategy strategy =
        campaignApplicationStrategyFactory.getStrategy(
            waitingList, product, priority, area, now, campaignCode);
    return strategy.register(customerId, productId, area, campaignCode);
  }
}
