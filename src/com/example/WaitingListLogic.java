package com.example;

import com.example.campaign.AddNewWaitingList;
import com.example.campaign.SaveAsBookingIfAvailable;
import com.example.campaign.SaveAsNewBookingWithCampaignReward;
import com.example.campaign.SaveNewWaitingList;
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
        new CampaignApplicationStrategyFactory(campaignEvents, idGenerator, salesStore, uriBuilder);
  }

  public static final @NotNull PathParam<ProductId> PRODUCTS = PathParam.ofId("products");
  public static final @NotNull PathParam<WaitingListId> WAITING_LIST = PathParam.ofId("waiting-list");

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

  record CampaignApplicationStrategyFactory(
      @NotNull CampaignEvents campaignEvents,
      @NotNull IdGenerator idGenerator,
      @NotNull SalesStore salesStore,
      @NotNull URIBuilder uriBuilder) {
    @NotNull
    private CampaignApplicationStrategy getStrategy(
        @NotNull Collection<WaitingList> waitingList,
        @NotNull Product product,
        @NotNull CampaignPriority priority,
        @NotNull Area area,
        @NotNull Instant now,
        @Nullable CampaignCode campaignCode) {
      if (waitingList.isEmpty()) {
        if (product.isWaitingListAvailableForArea(area)) {
          return new SaveNewWaitingList(
              campaignEvents(), idGenerator(), uriBuilder(), priority, now);
        } else {
          return new SaveAsNewBookingWithCampaignReward(
              campaignEvents(), salesStore(), uriBuilder(), priority, now);
        }
      } else {
        if (product.isWaitingListAvailableForArea(area)) {
          return new AddNewWaitingList(
              campaignEvents(), idGenerator(), uriBuilder(), waitingList, priority, now);
        } else {
          return new SaveAsBookingIfAvailable(campaignEvents(), salesStore(), uriBuilder(), now);
        }
      }
    }
  }
}
