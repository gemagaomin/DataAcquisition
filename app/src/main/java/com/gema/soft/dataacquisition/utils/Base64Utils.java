package com.gema.soft.dataacquisition.utils;
import android.util.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class Base64Utils {
    private static final Charset DEFAULT_CHARSET;

    public Base64Utils() {
    }

    public static byte[] encode(byte[] src) {
        return src.length == 0 ? src : Base64.encode(src, Base64.NO_WRAP);
    }

    public static byte[] decode(byte[] src) {
        return src.length == 0 ? src : Base64.decode(src,Base64.NO_WRAP);// Base64.getDecoder().decode(src);
    }

    public static String encodeToString(byte[] src) {
        return src.length == 0 ? "" : new String(encode(src), DEFAULT_CHARSET);
    }

    public static byte[] decodeFromString(String src) {
        return src.isEmpty() ? new byte[0] : decode(src.getBytes(DEFAULT_CHARSET));
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }
}
