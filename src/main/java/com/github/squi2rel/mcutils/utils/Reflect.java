package com.github.squi2rel.mcutils.utils;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class Reflect {
    public <T> T get(Object o, String s) throws Exception {
        Field f = o.getClass().getField(s);
        f.setAccessible(true);
        return (T) f.get(o);
    }
}
