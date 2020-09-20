package com.qingclass.bigbay.tool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    private static final String[] hexDigits = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public MD5Util() {
    }

    public static String md5(String text) {
        MessageDigest msgDigest = null;

        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var9) {
            throw new IllegalStateException("System doesn't support MD5 algorithm.");
        }

        msgDigest.update(text.getBytes());
        byte[] bytes = msgDigest.digest();
        String md5Str = new String();

        for(int i = 0; i < bytes.length; ++i) {
            byte tb = bytes[i];
            char tmpChar = (char)(tb >>> 4 & 15);
            char high;
            if (tmpChar >= '\n') {
                high = (char)(97 + tmpChar - 10);
            } else {
                high = (char)(48 + tmpChar);
            }

            md5Str = md5Str + high;
            tmpChar = (char)(tb & 15);
            char low;
            if (tmpChar >= '\n') {
                low = (char)(97 + tmpChar - 10);
            } else {
                low = (char)(48 + tmpChar);
            }

            md5Str = md5Str + low;
        }

        return md5Str;
    }

    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;

        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname != null && !"".equals(charsetname)) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            }
        } catch (Exception ee) {
            ee.printStackTrace();;
        }

        return resultString;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();

        for(int i = 0; i < b.length; ++i) {
            resultSb.append(byteToHexString(b[i]));
        }

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (b < 0) {
            n = b + 256;
        }

        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
