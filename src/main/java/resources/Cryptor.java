package resources;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Cryptor {
    private static String algorithm = "DESede";
    private static Key    key;
    private static Cipher cipher;

    static {
        try {
            key = KeyGenerator.getInstance(algorithm).generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public synchronized static byte[] decryptMessage(final byte[] message) throws BadPaddingException, IllegalBlockSizeException {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return cipher.doFinal(message);
    }

    public synchronized static byte[] encryptMessage(final byte[] message) throws BadPaddingException, IllegalBlockSizeException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return cipher.doFinal(message);
    }


}
