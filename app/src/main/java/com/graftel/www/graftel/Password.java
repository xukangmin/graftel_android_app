package com.graftel.www.graftel;

import android.util.Base64;

import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Shorabh on 6/20/2016.
 */

public class Password
{
    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 64*4;

    protected static String hashValue(String password) throws Exception
    {
        byte[] salt = getNextSalt();
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password.toCharArray()), salt, ITERATIONS);
        KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(KEY_LENGTH);
        byte[] subkey = key.getKey();
        /*PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        byte[] subkey= new byte[]{};
        try
        {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            subkey = skf.generateSecret(spec).getEncoded();
        }

        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            //throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        }
        finally
        {
            spec.clearPassword();
        }*/

        byte[] outputBytes = new byte[13 + salt.length + subkey.length];
        outputBytes[0] = 0x01;
        writeNetworkByteOrder(outputBytes, 1, (Integer)1);
        writeNetworkByteOrder(outputBytes, 5, 10000);
        writeNetworkByteOrder(outputBytes, 9, 16);
        System.arraycopy(salt, 0, outputBytes, 13, salt.length);
        System.arraycopy(subkey, 0, outputBytes, 13 + 16, subkey.length);
        return Base64.encodeToString(outputBytes,Base64.DEFAULT);
    }

    protected static boolean verifyHashedValue(String hashedValue, String providedValue) throws Exception
    {
        byte[] decodedHashedPassword = Base64.decode(hashedValue,Base64.DEFAULT);
        if(decodedHashedPassword[0]!=0x01)
        {
            return false;
        }
        int saltLength = (int)readNetworkByteOrder(decodedHashedPassword, 9);
        if (saltLength < 128/8)
        {
            return false;
        }
        byte[] salt = new byte[saltLength];
        System.arraycopy(decodedHashedPassword, 13, salt, 0, salt.length);
        int subkeyLength = decodedHashedPassword.length - 13 - salt.length;
        if (subkeyLength < 128 / 8)
        {
            return false;
        }

        byte[] expectedSubkey = new byte[subkeyLength];
        System.arraycopy(decodedHashedPassword, 13 + salt.length, expectedSubkey, 0, expectedSubkey.length);
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(providedValue.toCharArray()), salt, ITERATIONS);
        KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(KEY_LENGTH);
        byte[] actualSubkey = key.getKey();
        /*PBEKeySpec spec = new PBEKeySpec(providedValue.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        byte[] actualSubkey= new byte[]{};
        try
        {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            actualSubkey = skf.generateSecret(spec).getEncoded();
        }

        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            //throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        }
        finally
        {
            spec.clearPassword();
        }*/
        return Base64.encodeToString(expectedSubkey,Base64.DEFAULT).equals(Base64.encodeToString(actualSubkey,Base64.DEFAULT));
    }

    public static byte[] getNextSalt()
    {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private static void writeNetworkByteOrder(byte[] buffer, int offset, Integer value)
    {
        buffer[offset + 0] = (byte)(value >> 24);
        buffer[offset + 1] = (byte)(value >> 16);
        buffer[offset + 2] = (byte)(value >> 8);
        buffer[offset + 3] = (byte)(value >> 0);
    }

    private static Integer readNetworkByteOrder(byte[] buffer, int offset)
    {
        return ((Byte)(buffer[offset + 0]) << 24) | ((Byte)(buffer[offset + 1]) << 16) | ((Byte)(buffer[offset + 2]) << 8) | ((Byte)(buffer[offset + 3]));
    }
}
