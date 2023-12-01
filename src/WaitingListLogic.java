import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    ContractPlan plan = campaignCode == null ? customer.getPlan() : customer.getPlan().rankUp();
    Collection<WaitingList> waitingList =
        campaignEvents.findWaitingList(productId, plan, area, now);
    if (waitingList.isEmpty()) {
      if (product.isWaitingListAvailableForArea(area)) {
        CampaignRule rule = campaignEvents.findRule(productId, area);
        if (rule == null) {
          WaitingListRuleId waitingListRuleId = idGenerator.generateNew(WaitingListRuleId.class);
          rule =
              campaignEvents.createNewRule(
                  productId, area, waitingListRuleId, CampaignRule.getDefault());
        }
        WaitingListId waitingListId = idGenerator.generateNew(WaitingListId.class);
        WaitingRequest waitingRequest =
            rule.createRequest(waitingListId, customerId, productId, plan, area, now);
        WaitingList waiting = campaignEvents.createNewWaitingCustomer(waitingRequest);
        if (campaignCode != null) {
          CampaignRewardRequest request =
              new CampaignRewardRequest(
                  productId,
                  Reference.of(WaitingList.class, waiting.getId()),
                  plan,
                  campaignCode,
                  now);
          campaignEvents.createCampaignReward(request);
        }
        return uriBuilder
            .name(PRODUCTS)
            .value(productId)
            .name(Area.PATH_PARAM)
            .value(area)
            .name(WAITING_LIST)
            .value(waiting.getId())
            .build();
      } else {
        Booking booking = salesStore.bookPurchaseContract(customerId, productId, area, now);
        if (campaignCode != null) {
          CampaignRewardRequest request =
              new CampaignRewardRequest(
                  productId, Reference.of(Booking.class, booking.getId()), plan, campaignCode, now);
          campaignEvents.createCampaignReward(request);
        }
        return uriBuilder
            .name(PRODUCTS)
            .value(productId)
            .name("contracts")
            .name(BOOKINGS)
            .value(booking.getId())
            .build();
      }
    } else {
      if (product.isWaitingListAvailableForArea(area)) {
        WaitingListId waitingListId = idGenerator.generateNew(WaitingListId.class);
        CampaignRule rule = campaignEvents.findRule(productId, area);
        if (rule == null) {
          throw new IllegalStateException(
              "no rules found for the waiting list of the product, id = { productId } area = { area }");
        }
        WaitingRequest waitingRequest =
            rule.createRequest(waitingList, waitingListId, customerId, productId, plan, area, now);
        WaitingList waiting = campaignEvents.createNewWaitingCustomer(waitingRequest);
        if (campaignCode != null) {
          CampaignRewardRequest request =
              new CampaignRewardRequest(
                  productId,
                  Reference.of(WaitingList.class, waiting.getId()),
                  plan,
                  campaignCode,
                  now);
          campaignEvents.createCampaignReward(request);
        }
        return uriBuilder
            .name(PRODUCTS)
            .value(productId)
            .name(WAITING_LIST)
            .value(waiting.getId())
            .build();
      } else { // Waiting List を締め切った場合
        CampaignRule rule = campaignEvents.findRule(productId, area);
        if (rule != null && rule.acceptsBookingAfterCampaignDeadline()) {
          Booking booking = salesStore.bookPurchaseContract(customerId, productId, area, now);
          return uriBuilder
              .name(PRODUCTS)
              .value(productId)
              .name("contracts")
              .name(BOOKINGS)
              .value(booking.getId())
              .build();
        } else {
          campaignEvents.saveExpiredCampaignApplication(
              customerId, productId, area, now, campaignCode);
          return uriBuilder.name(PRODUCTS).value(productId).name("expired").build();
        }
      }
    }
  }
}