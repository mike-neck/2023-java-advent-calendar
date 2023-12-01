package com.example;

import org.jetbrains.annotations.NotNull;

sealed interface Area {
  enum Fixed implements Area {
    TOKYO,
    SOUTH_KANTO,
    NORTH_KANTO,
  }

  record Foreign(@NotNull String name) implements Area {}

  PathParam<Area> PATH_PARAM = new PathParam<>() {};
}
