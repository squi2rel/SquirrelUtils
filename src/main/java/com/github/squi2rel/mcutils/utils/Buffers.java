package com.github.squi2rel.mcutils.utils;

import java.nio.ByteBuffer;

public class Buffers {
    public static void copy(byte[] src, int srcOffset, ByteBuffer dst, int numElements){
        dst.limit(dst.position() + numElements);
        dst.put(src, srcOffset, numElements);
    }
}
