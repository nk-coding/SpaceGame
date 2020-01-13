package com.nkcoding.util;

import java.io.IOException;

@FunctionalInterface
public interface IOBiFunction<T1, T2, R> {
    R apply(T1 v1, T2 v2) throws IOException;
}
