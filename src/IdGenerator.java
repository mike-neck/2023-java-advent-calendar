
import org.jetbrains.annotations.NotNull;

public interface IdGenerator {
    @NotNull <T, I extends Id<T>> I generateNew(@NotNull Class<I> klass);
}
