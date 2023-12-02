package com.example;

import org.jetbrains.annotations.NotNull;

public interface Customer {
  @NotNull
  ContractPlan getPlan();
}
