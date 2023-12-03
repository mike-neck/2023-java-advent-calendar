# 2023-java-advent-calendar

2023 年 Java アドベントカレンダーの二日目です。

リファクタリングとデザインパターン
---

JJUG CCC にて参加者を募っているということで、こちらの勉強会(?実験)にサンプルとして参加してきました。

デザインパターンを理解していることが参加条件っぽかったので、バレないようにウィキペで勉強しました。

というわけで、デザインパターンをあまりよく知らないのですが、
リファクタリングを拗らせてやりすぎると大抵はデザインパターンのいくつかの形になります。
具体的には if 文が複雑で処理内容が副作用(?データベースを更新するとかのあれ)を伴うメソッドをリファクタリングすると、
ファクトリーメソッドパターン + ストラテジーパターンになります。

というわけで、複雑なメソッドをリファクタリングして教科書的なデザインパターンにしていく様子をずんだもんに実況させようとしたのですが、
時間がたりないので、このリポジトリーを見てくださいという内容になっています。

以下に示すコミットを追いかけていくと、リファクタリングの様子がわかると思います。

|   コミット   | 変更内容          |
|:--------:|:--------------|
| 422b9a6d | メソッドの抽出       |
| 118a53f5 | スタティックメソッドへ変換 |

## 従来のソースコード

元々のソースコードは以下に示す通りになっています。

```java
class WaitingListLogic {

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
                // ウェイティングリストに初めて人が並ぶ場合
                CampaignRule rule = campaignEvents.findRule(productId, area);
                if (rule == null) {
                    CampaignRuleId campaignRuleId = idGenerator.generateNew(CampaignRuleId.class);
                    rule =
                            campaignEvents.createNewRule(
                                    productId, area, campaignRuleId, CampaignRule.getDefault());
                }
                WaitingListId waitingListId = idGenerator.generateNew(WaitingListId.class);
                WaitingRequest waitingRequest =
                        rule.createRequest(waitingListId, customerId, productId, priority, area, now);
                WaitingList waiting = campaignEvents.createNewWaitingCustomer(waitingRequest);
                if (campaignCode != null) {
                    CampaignRewardRequest request =
                            new CampaignRewardRequest(
                                    productId,
                                    Reference.of(WaitingList.class, waiting.getId()),
                                    priority,
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
                // ウェイティングリストが終了している場合は予約として扱う
                Booking booking = salesStore.bookPurchaseContract(customerId, productId, area, now);
                if (campaignCode != null) {
                    CampaignRewardRequest request =
                            new CampaignRewardRequest(
                                    productId,
                                    Reference.of(Booking.class, booking.getId()),
                                    priority,
                                    campaignCode,
                                    now);
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
                // ウェイティングリストに並ぶ場合、現在の待ち順位を算出して登録
                WaitingListId waitingListId = idGenerator.generateNew(WaitingListId.class);
                CampaignRule rule = campaignEvents.findRule(productId, area);
                if (rule == null) {
                    throw new IllegalStateException(
                            "no rules found for the waiting list of the product, id = { productId } area = { area }");
                }
                WaitingRequest waitingRequest =
                        rule.createRequest(
                                waitingList, waitingListId, customerId, productId, priority, area, now);
                WaitingList waiting = campaignEvents.createNewWaitingCustomer(waitingRequest);
                if (campaignCode != null) {
                    CampaignRewardRequest request =
                            new CampaignRewardRequest(
                                    productId,
                                    Reference.of(WaitingList.class, waiting.getId()),
                                    priority,
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
```

