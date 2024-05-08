package com.demo.android;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

/**
 * HmacSHA1工具类
 *
 * @author liujunyong
 */
public class HmacSha256Util {
    private HmacSha256Util() {
    }

    /**
     * Return the bytes of hash encryption.
     *
     * @param data      The data.
     * @param algorithm The name of hash encryption.
     * @return the bytes of hash encryption
     */
    private static byte[] hashTemplate(final byte[] data, final String algorithm) {
        if (data != null && data.length > 0) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm);
                md.update(data);
                return md.digest();
            } catch (NoSuchAlgorithmException e) {
                Log.e("debug", Log.getStackTraceString(e));
                Log.e("debug", Objects.requireNonNull(e.getLocalizedMessage()));
            }
        }
        return null;
    }

    /**
     * Return the bytes of hmac encryption.
     *
     * @param data      The data.
     * @param key       The key.
     * @param algorithm The name of hmac encryption.
     * @return the bytes of hmac encryption
     */
    private static byte[] hmacTemplate(final byte[] data,
                                       final byte[] key,
                                       final String algorithm) {
        if (data != null && data.length > 0
                && key != null && key.length > 0) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(key, algorithm);
                Mac mac = Mac.getInstance(algorithm);
                mac.init(secretKey);
                return mac.doFinal(data);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                Log.e("debug", Log.getStackTraceString(e));
                Log.e("debug", Objects.requireNonNull(e.getLocalizedMessage()));
            }
        }
        return null;
    }

    /**
     * hmacsha256
     *
     * @param data 数据
     * @param key  密钥
     * @return 签名值
     */
    public static byte[] hmacsha256(byte[] data, byte[] key) {
//        HMac mac = SecureUtil.hmac(HmacAlgorithm.HmacSHA256, key);
//        return mac.digest(data);
//        return hmacTemplate(data, key, "HmacSHA256");
        return hashTemplate(data, "SHA-256");
    }

    /**
     * 生成签名
     *
     * @param <T>       the type parameter
     * @param path      the path
     * @param signMap   map参数，参数转成map，这里直接用treeMap排序
     * @param secretKey 加密key
     * @return 签名 string
     */
//    public static <T> List<String> genSigns(final String path, SortedMap<String, T> signMap, final String secretKey) {
//        String content = path + signMap.entrySet().stream().filter(t -> t.getValue() != null).map(t -> String.format("%s=%s", t.getKey(), t.getValue())).collect(Collectors.joining("&"));
//        byte[] signBytes = hmacsha256(content.getBytes(StandardCharsets.UTF_8), secretKey.getBytes(StandardCharsets.UTF_8));
//        String sign1 = Base64.encodeBase64String(signBytes);
//        String sign2 = Base64.encodeBase64URLSafeString(signBytes);
//        String sign3 = HexUtil.encodeHexStr(signBytes);
//        String[] signs = {sign1, sign2, sign3, "pass"};
//        return Arrays.asList(signs);
//    }

//    public static <T> String genBeforeSignString(final String path, SortedMap<String, T> signMap) {
//        return path + signMap.entrySet().stream().map(t -> String.format("%s=%s", t.getKey(), t.getValue())).collect(Collectors.joining("&"));
//    }
}