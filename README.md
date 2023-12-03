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

| コミット | 変更内容 |
|:----:|:----|
| [422b9a6d](https://github.com/mike-neck/2023-java-advent-calendar/commit/422b9a6d) | if 文で区切られた部分単位でメソッドを抽出する |
| [118a53f5](https://github.com/mike-neck/2023-java-advent-calendar/commit/118a53f5) | [422b9a6d](https://github.com/mike-neck/2023-java-advent-calendar/commit/422b9a6d) のメソッドをスタティックメソッドへ変換 |
| [932cbb11](https://github.com/mike-neck/2023-java-advent-calendar/commit/932cbb11) | [118a53f5](https://github.com/mike-neck/2023-java-advent-calendar/commit/118a53f5) のメソッド一つ一つを表すオブジェクト(パラメーターオブジェクトないしはメソッドオブジェクト)を作成 |
| [1c425b2b](https://github.com/mike-neck/2023-java-advent-calendar/commit/1c425b2b) | メソッド呼び出しのパラメーターのうち、フィールドと条件分岐に使われるものを [932cbb11](https://github.com/mike-neck/2023-java-advent-calendar/commit/932cbb11) のオブジェクトのコンストラクターパラメーターにする |
| [23b18f97](https://github.com/mike-neck/2023-java-advent-calendar/commit/23b18f97) | [1c425b2b](https://github.com/mike-neck/2023-java-advent-calendar/commit/1c425b2b) で追加したオブジェクトのコンポーネントで、メソッドのパラメーターを置き換えて、不要になったメソッドのパラメーターを削除する |
| [3ce2dd1a](https://github.com/mike-neck/2023-java-advent-calendar/commit/3ce2dd1a) | [118a53f5](https://github.com/mike-neck/2023-java-advent-calendar/commit/118a53f5) のスタティックメソッドを [932cbb11](https://github.com/mike-neck/2023-java-advent-calendar/commit/932cbb11) のオブジェクトのインスタンスメソッドに変換する |
| [69f1b7f6](https://github.com/mike-neck/2023-java-advent-calendar/commit/69f1b7f6) | ストラテジーをあらわすインターフェースのガワを作成する |
| [de757310](https://github.com/mike-neck/2023-java-advent-calendar/commit/de757310) | [932cbb11](https://github.com/mike-neck/2023-java-advent-calendar/commit/932cbb11) のオブジェクトに [69f1b7f6](https://github.com/mike-neck/2023-java-advent-calendar/commit/69f1b7f6) のストラテジーインターフェースを実装させる |
| [a26bedc6](https://github.com/mike-neck/2023-java-advent-calendar/commit/a26bedc6) | [3ce2dd1a](https://github.com/mike-neck/2023-java-advent-calendar/commit/3ce2dd1a) で変換したインスタンスメソッドも呼び出しを [69f1b7f6](https://github.com/mike-neck/2023-java-advent-calendar/commit/69f1b7f6) のメソッドの呼び出しに変更する |
| [5456caff](https://github.com/mike-neck/2023-java-advent-calendar/commit/5456caff) | [932cbb11](https://github.com/mike-neck/2023-java-advent-calendar/commit/932cbb11) のオブジェクトを [69f1b7f6](https://github.com/mike-neck/2023-java-advent-calendar/commit/69f1b7f6) のインターフェースの変数に一度割り当ててからメソッドを呼び出すようにする |
| [dad1d9f3](https://github.com/mike-neck/2023-java-advent-calendar/commit/dad1d9f3) | if 文全体をメソッドに抽出して、ファクトリーメソッドとする |
| [8a6080ab](https://github.com/mike-neck/2023-java-advent-calendar/commit/8a6080ab) | [dad1d9f3](https://github.com/mike-neck/2023-java-advent-calendar/commit/dad1d9f3) のファクトリーメソッドをスタティックメソッドに変換する |
| [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) | [8a6080ab](https://github.com/mike-neck/2023-java-advent-calendar/commit/8a6080ab) をあらわすパラメーターオブジェクト(メソッドオブジェクト)を record クラスで作成する |
| [b8a9464f](https://github.com/mike-neck/2023-java-advent-calendar/commit/b8a9464f) | [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) のオブジェクトのコンポーネントにフィールドのオブジェクト群を追加し [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) のオブジェクトをフィールドにする。 |
| [f4f307b2](https://github.com/mike-neck/2023-java-advent-calendar/commit/f4f307b2) | [8a6080ab](https://github.com/mike-neck/2023-java-advent-calendar/commit/8a6080ab) のスタティックメソッド内で使っているパラメーターのうち [b8a9464f](https://github.com/mike-neck/2023-java-advent-calendar/commit/b8a9464f) によって [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) のオブジェクトのコンポーネントになったものを置き換えて、メソッドのパラメーターの数を減らす |
| [4af6d8a3](https://github.com/mike-neck/2023-java-advent-calendar/commit/4af6d8a3) | [8a6080ab](https://github.com/mike-neck/2023-java-advent-calendar/commit/8a6080ab) のスタティックメソッドを [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) のインスタンスメソッドに変換する |
| [5d7ea8d7](https://github.com/mike-neck/2023-java-advent-calendar/commit/5d7ea8d7) | [932cbb11](https://github.com/mike-neck/2023-java-advent-calendar/commit/932cbb11) のクラス群をインナークラスから別パッケージのクラスへ移動する |
| [e9ba2dee](https://github.com/mike-neck/2023-java-advent-calendar/commit/e9ba2dee) | [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) のクラスをインナークラスから別パッケージのクラスへと移動する |
| [0b4046ef](https://github.com/mike-neck/2023-java-advent-calendar/commit/0b4046ef) | [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) のクラスを抽象するインターフェースを作成、 [c1dc7c7b](https://github.com/mike-neck/2023-java-advent-calendar/commit/c1dc7c7b) に実装させる |
| [a86c4946](https://github.com/mike-neck/2023-java-advent-calendar/commit/a86c4946) | [0b4046ef](https://github.com/mike-neck/2023-java-advent-calendar/commit/0b4046ef) のクラスを生成するインターフェースと、実際のバインディング機構を `ServiceLoader` によってつくる |

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

