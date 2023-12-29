package com.github.squi2rel.mcutils.events;

import com.github.squi2rel.mcutils.utils.Lambda;
import com.github.squi2rel.mcutils.utils.Param;

import java.util.ArrayList;
import java.util.HashMap;

public class Events {
    private static final HashMap<Object, ArrayList<Param<Object>>> map = new HashMap<>();
    public static void init() {
        map.clear();
    }

    public static void fire(Object type, Object args) {
        ArrayList<Param<Object>> a = map.get(type);
        if (a == null) return;
        for (Param<Object> p : a) {
            p.invoke(args);
        }
    }

    public static void fire(Enum<?> trigger) {
        fire(trigger, null);
    }

    public static void on(Object type, Param<Object> listener) {
        map.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public static void on(Enum<?> type, Lambda listener) {
        on(type, e -> listener.invoke());
    }
}

