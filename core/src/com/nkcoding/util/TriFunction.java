package com.nkcoding.util;

@FunctionalInterface
public interface TriFunction<T1, T2, T3, R> {
    R apply(T1 v1, T2 v2, T3 v3);
}
