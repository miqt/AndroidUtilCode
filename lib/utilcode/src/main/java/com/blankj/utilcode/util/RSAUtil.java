package com.blankj.utilcode.util;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;


/**
 * RSA 加密 工具类
 */
public abstract class RSAUtil {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";

    public static final String PUBLIC_KEY = "RSAPublicKey";
    public static final String PRIVATE_KEY = "RSAPrivateKey";

    public static String sign(byte[] data, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        // 解密由base64编码的私钥
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);

        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取私钥匙对象
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);

        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 校验数字签名
     */
    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {

        // 解密由base64编码的公钥
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);

        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取公钥匙对象
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);

        // 验证签名是否正常
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    /**
     * 解密<br>
     * 用私钥解密
     */
    public static byte[] decryptByPrivateKey(byte[] data, String key)
            throws Exception {
        byte[] decryptedData = null;

        // 对密钥解密
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        int maxDecryptBlockSize = getMaxDencryptBytesByPrivate(keyFactory, privateKey);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            int dataLength = data.length;
            for (int i = 0; i < dataLength; i += maxDecryptBlockSize) {
                int decryptLength = Math.min(dataLength - i, maxDecryptBlockSize);
                byte[] doFinal = cipher.doFinal(data, i, decryptLength);
                bout.write(doFinal);
            }
            decryptedData = bout.toByteArray();
        } finally {
            bout.close();
        }

        return decryptedData;

    }

    /**
     * 将Base64编码的密文解密为字符串
     */
    public static String decryptByPrivateKeyToString(String base64Str, String key) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64Str);
        byte[] oriData = decryptByPrivateKey(data, key);

        return new String(oriData);
    }

    /**
     * 获取公钥加密可加密的最大数据字节长度
     */
    private static int getMaxEncryptBytesByPublicKey(KeyFactory keyFactory, Key key) throws InvalidKeySpecException {
        return getPublicKeyBitLength(keyFactory, key) / 8 - 11;
    }

    /**
     * 获取公钥解密每块的字节长度
     */
    private static int getMaxDencryptBytesByPrivate(KeyFactory keyFactory, Key key) throws InvalidKeySpecException {
        return getPrivateKeyBitLength(keyFactory, key) / 8;
    }

    /**
     * 获取公钥加密可加密的最大数据字节长度
     */
    private static int getMaxEncryptBytesByPrivate(KeyFactory keyFactory, Key key) throws InvalidKeySpecException {
        return getPrivateKeyBitLength(keyFactory, key) / 8 - 11;
    }

    /**
     * 获取公钥解密每块的字节长度
     */
    private static int getMaxDencryptBytesByPublicKey(KeyFactory keyFactory, Key key) throws InvalidKeySpecException {
        return getPublicKeyBitLength(keyFactory, key) / 8;
    }

    /**
     * 获取公钥的字节长度
     */
    private static int getPublicKeyBitLength(KeyFactory keyFactory, Key key) throws InvalidKeySpecException {
        RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(key, RSAPublicKeySpec.class);
        return publicKeySpec.getModulus().bitLength();
    }

    /**
     * 获取私钥的字节长度
     */
    private static int getPrivateKeyBitLength(KeyFactory keyFactory, Key key) throws InvalidKeySpecException {
        RSAPrivateKeySpec publicKeySpec = keyFactory.getKeySpec(key, RSAPrivateKeySpec.class);
        return publicKeySpec.getModulus().bitLength();
    }

    /**
     * 解密<br>
     * 用公钥解密
     */
    public static byte[] decryptByPublicKey(byte[] data, String key)
            throws Exception {
        byte[] decryptedData = null;

        // 对密钥解密
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        int maxDecryptBlockSize = getMaxDencryptBytesByPublicKey(keyFactory, publicKey);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            int dataLength = data.length;
            for (int i = 0; i < dataLength; i += maxDecryptBlockSize) {
                int decryptLength = Math.min(dataLength - i, maxDecryptBlockSize);
                byte[] doFinal = cipher.doFinal(data, i, decryptLength);
                bout.write(doFinal);
            }
            decryptedData = bout.toByteArray();
        } finally {
            bout.close();
        }

        return decryptedData;
    }

    /**
     * 加密<br>
     * 用公钥加密
     */
    public static byte[] encryptByPublicKey(byte[] data, String key)
            throws Exception {
        byte[] encryptedData = null;
        // 对公钥解密
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int maxEncryptBlockSize = getMaxEncryptBytesByPublicKey(keyFactory, publicKey);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            int dataLength = data.length;
            for (int i = 0; i < data.length; i += maxEncryptBlockSize) {
                int encryptLength = Math.min(dataLength - i, maxEncryptBlockSize);
                byte[] doFinal = cipher.doFinal(data, i, encryptLength);
                bout.write(doFinal);
            }
            encryptedData = bout.toByteArray();
        } finally {
            bout.close();
        }

        return encryptedData;
    }

    /**
     * 加密<br>
     * 用私钥加密
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key)
            throws Exception {
        byte[] encryptedData = null;

        // 对密钥解密
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        int maxEncryptBlockSize = getMaxEncryptBytesByPrivate(keyFactory, privateKey);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            int dataLength = data.length;
            for (int i = 0; i < data.length; i += maxEncryptBlockSize) {
                int encryptLength = Math.min(dataLength - i, maxEncryptBlockSize);
                byte[] doFinal = cipher.doFinal(data, i, encryptLength);
                bout.write(doFinal);
            }
            encryptedData = bout.toByteArray();
        } finally {
            bout.close();
        }

        return encryptedData;
    }

    /**
     * 取得私钥
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 取得公钥
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 初始化密钥
     */
    public static Map<String, Object> initKey() throws Exception {
        return initKey(1024);
    }

    public static Map<String, Object> initKey(int keySize) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(keySize);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // 私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>(2);

        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        try {
            //生成公钥私钥
            Map<String, Object> map = RSAUtil.initKey(2048);

            String PrivateKey = RSAUtil.getPrivateKey(map);
            String PublicKey = RSAUtil.getPublicKey(map);

            System.out.println("PrivateKey:" + PrivateKey);
            System.out.println("PublicKey:" + PublicKey);

            //明文
            String data = "hello rsa!";

            //公钥加密私钥解密
            byte[] encData = RSAUtil.encryptByPublicKey(data.getBytes(), PublicKey);
            byte[] decData = RSAUtil.decryptByPrivateKey(encData, PrivateKey);
            System.out.println(new String(decData));

            //私钥加密公钥解密
            byte[] encData1 = RSAUtil.encryptByPrivateKey(data.getBytes(), PrivateKey);
            byte[] decData1 = RSAUtil.decryptByPublicKey(encData1, PublicKey);
            System.out.println(new String(decData1));

            //私钥签名公钥验签
            String signStr = RSAUtil.sign(data.getBytes(), PrivateKey);
            boolean verify = RSAUtil.verify(data.getBytes(), PublicKey, signStr);
            System.out.println(signStr + verify);

        } catch (Exception e) {
            //密钥不对等情况
            e.printStackTrace();
        }
    }

}

