package com.github.arekolek.sarenka;

public class Ints {
    public static int checkedCast(long l) {
        int i = (int) l;
        if ((long) i != l) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return i;
    }
}
