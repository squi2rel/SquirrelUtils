package com.github.squi2rel.mcutils.utils;

import java.util.ArrayList;

public class Func {
    public static <T> T find(ArrayList<T> a, ObjBol<T> p) {
        T t = null;
        for (T o : a) {
            if (p.get(o)) {
                t = o;
                break;
            }
        }
        return t;
    }

    public interface ObjBol<T> {
        boolean get(T o);
    }
}
