package com.example.talkup;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption_Decryption {
    private static String AES = "AES";

    public static SecretKeySpec generateKey(String text) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] bytes = text.getBytes("ISO-8859-1");
        digest.update(bytes,0,bytes.length);
        byte [] key = digest.digest();
        System.out.println(key.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public static String encrypt(String data, String pass) throws Exception{
        SecretKeySpec key = generateKey(pass);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte [] data1 = data.getBytes();
        byte [] encVal;
        encVal = c.doFinal(data1);
        String encryptedValue = new String(encVal, "ISO-8859-1");
        return encryptedValue;
    }

    public static String decrypt(String data, String pass) throws Exception{
        SecretKeySpec key = generateKey(pass);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE,key);
        byte [] decodedValue = data.getBytes("ISO-8859-1");
        byte [] decVal = c.doFinal(decodedValue);
        String decryptedValue = new String(decVal);
        return decryptedValue;
    }
}
