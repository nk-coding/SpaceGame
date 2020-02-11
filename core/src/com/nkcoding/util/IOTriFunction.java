package com.nkcoding.util;

import java.io.IOException;

@FunctionalInterface
public interface IOTriFunction<T1, T2, T3, R> {
    R apply(T1 v1, T2 v2, T3 v3) throws IOException;
}