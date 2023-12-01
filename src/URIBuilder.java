
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public interface URIBuilder {
  @NotNull
  URI build();

  @NotNull URIBuilder name(@NotNull String name);

  @NotNull
  <T> URIBuilder.OfValue<T> name(@NotNull PathParam<T> name);

  interface OfValue<T> {
    @NotNull
    URIBuilder value(@NotNull T value);
  }
}
