package com.nkcoding.interpreter;

public class ListObject {
    public final StackItem[] items;

    public ListObject(int size) {
        items = new StackItem[size];
    }
}
