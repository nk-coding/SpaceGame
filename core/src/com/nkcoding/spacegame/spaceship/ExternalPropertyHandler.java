package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ExternalMethodFuture;

public interface ExternalPropertyHandler {
    String getName();

    boolean handleExternalMethod(ExternalMethodFuture future);
}
