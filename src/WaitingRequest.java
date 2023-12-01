
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public record WaitingRequest(
    @NotNull WaitingListId waitingListId,
    @NotNull CustomerId customerId,
    @NotNull ProductId productId,
    @NotNull ContractPlan plan,
    @NotNull Area area,
    @NotNull Instant now) {}
