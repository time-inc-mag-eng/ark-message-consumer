/**
 * Nov 15, 2010
 */
package com.timeinc.messaging.utils;

import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @author apradhan1271
 *
 */
public class DesEncrypter { 
	Cipher ecipher; 
	Cipher dcipher; 
	// 8-byte Salt 
	private static final byte[] SALT = { 
			(byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32, (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03  
	}; 
	// Iteration count 
	private static final int ITERATIONCOUNT = 19; 
	private static final String PASS_PHRASE = "ashmyhero2013";
	
	
	/**
	 * @param passPhrase
	 */
	public DesEncrypter() { 
		try { 
			// Create the key 
			KeySpec keySpec = new PBEKeySpec(PASS_PHRASE.toCharArray(), SALT, ITERATIONCOUNT); 
			SecretKey key = SecretKeyFactory.getInstance( "PBEWithMD5AndDES").generateSecret(keySpec); 
			ecipher = Cipher.getInstance(key.getAlgorithm()); 
			dcipher = Cipher.getInstance(key.getAlgorithm()); 
			// Prepare the parameter to the ciphers 
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATIONCOUNT); 
			// Create the ciphers 
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec); 
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec); 
		} catch (java.security.InvalidAlgorithmParameterException e) { }
		catch (java.security.spec.InvalidKeySpecException e) { } 
		catch (javax.crypto.NoSuchPaddingException e) { } 
		catch (java.security.NoSuchAlgorithmException e) { } 
		catch (java.security.InvalidKeyException e) { } 
	} 
	
	/**
	 * @param str
	 * @return Encrypted String
	 */
	public String encrypt(String str) { 
		try { // Encode the string into bytes using utf-8 
			byte[] utf8 = str.getBytes("UTF8"); 
			// Encrypt 
			byte[] enc = ecipher.doFinal(utf8); 
			// Encode bytes to base64 to get a string 
			return new sun.misc.BASE64Encoder().encode(enc); 
		} catch (javax.crypto.BadPaddingException e) { } 
		catch (IllegalBlockSizeException e) { } 
		catch (UnsupportedEncodingException e) { } 
		return null; 
	} 
	
	/**
	 * @param str
	 * @return Decrypted String
	 */
	public String decrypt(String str) { 
		try { // Decode base64 to get bytes 
			byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str); 
			// Decrypt 
			byte[] utf8 = dcipher.doFinal(dec); 
			// Decode using utf-8 
			return new String(utf8, "UTF8"); 
		} catch (javax.crypto.BadPaddingException e) { } 
		catch (IllegalBlockSizeException e) { } 
		catch (UnsupportedEncodingException e) { } 
		catch (java.io.IOException e) { } 
		return null; 
	} 
	
	
	public static void main(String args[]) {
		DesEncrypter e = new DesEncrypter();
		//String password = "titp4dmul" dev;
		String password ="1luv*nix"; //origin.dreader.net
		//String password = "Timp4pdmul";
		//String password = "timeQ!W@E#";
		//String encrypted = e.encrypt(password);
		String encrypted = e.encrypt(password);
		System.out.println(encrypted);
		System.out.println(e.decrypt("FPncMpKbfZABquZAc0sCZA=="));
	}
	
}
