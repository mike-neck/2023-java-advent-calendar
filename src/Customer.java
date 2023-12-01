import org.jetbrains.annotations.NotNull;

interface Customer {
  @NotNull
  ContractPlan getPlan();
}
