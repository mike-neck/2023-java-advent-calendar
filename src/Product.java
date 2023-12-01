import org.jetbrains.annotations.NotNull;

interface Product {
  boolean isOnSale();

  boolean isEndOfSale();

  boolean isWaitingListAvailableForArea(@NotNull Area area);
}
