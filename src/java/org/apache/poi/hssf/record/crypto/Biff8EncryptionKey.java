package org.apache.poi.hssf.record.crypto;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutputStream;

public final class Biff8EncryptionKey {
	// these two constants coincidentally have the same value
	private static final int KEY_DIGEST_LENGTH = 5;
	private static final int PASSWORD_HASH_NUMBER_OF_BYTES_USED = 5;

	private final byte[] _keyDigest;

	/**
	 * Create using the default password and a specified docId
	 * @param docId 16 bytes
	 */
	public static Biff8EncryptionKey create(byte[] docId) {
		return new Biff8EncryptionKey(createKeyDigest("VelvetSweatshop", docId));
	}
	public static Biff8EncryptionKey create(String password, byte[] docIdData) {
		return new Biff8EncryptionKey(createKeyDigest(password, docIdData));
	}

	Biff8EncryptionKey(byte[] keyDigest) {
		if (keyDigest.length != KEY_DIGEST_LENGTH) {
			throw new IllegalArgumentException("Expected 5 byte key digest, but got " + HexDump.toHex(keyDigest));
		}
		_keyDigest = keyDigest;
	}

	static byte[] createKeyDigest(String password, byte[] docIdData) {
		check16Bytes(docIdData, "docId");
		int nChars = Math.min(password.length(), 16);
		byte[] passwordData = new byte[nChars*2];
		for (int i=0; i<nChars; i++) {
			char ch = password.charAt(i);
			passwordData[i*2+0] = (byte) ((ch << 0) & 0xFF);
			passwordData[i*2+1] = (byte) ((ch << 8) & 0xFF);
		}

		byte[] kd;
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		md5.update(passwordData);
		byte[] passwordHash = md5.digest();
		md5.reset();

		for (int i=0; i<16; i++) {
			md5.update(passwordHash, 0, PASSWORD_HASH_NUMBER_OF_BYTES_USED);
			md5.update(docIdData, 0, docIdData.length);
		}
		kd = md5.digest();
		byte[] result = new byte[KEY_DIGEST_LENGTH];
		System.arraycopy(kd, 0, result, 0, KEY_DIGEST_LENGTH);
		return result;
	}

	/**
	 * @return <code>true</code> if the keyDigest is compatible with the specified saltData and saltHash
	 */
	public boolean validate(byte[] saltData, byte[] saltHash) {
		check16Bytes(saltData, "saltData");
		check16Bytes(saltHash, "saltHash");

		// validation uses the RC4 for block zero
		RC4 rc4 = createRC4(0);
		byte[] saltDataPrime = saltData.clone();
		rc4.encrypt(saltDataPrime);

		byte[] saltHashPrime = saltHash.clone();
		rc4.encrypt(saltHashPrime);

		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md5.update(saltDataPrime);
		byte[] finalSaltResult = md5.digest();

		if (false) { // set true to see a valid saltHash value
			byte[] saltHashThatWouldWork = xor(saltHash, xor(saltHashPrime, finalSaltResult));
			System.out.println(HexDump.toHex(saltHashThatWouldWork));
		}

		return Arrays.equals(saltHashPrime, finalSaltResult);
	}

	private static byte[] xor(byte[] a, byte[] b) {
		byte[] c = new byte[a.length];
		for (int i = 0; i < c.length; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}
		return c;
	}
	private static void check16Bytes(byte[] data, String argName) {
		if (data.length != 16) {
			throw new IllegalArgumentException("Expected 16 byte " + argName + ", but got " + HexDump.toHex(data));
		}
	}

	/**
	 * The {@link RC4} instance needs to be changed every 1024 bytes.
	 * @param keyBlockNo used to seed the newly created {@link RC4}
	 */
	RC4 createRC4(int keyBlockNo) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		md5.update(_keyDigest);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
		new LittleEndianOutputStream(baos).writeInt(keyBlockNo);
		md5.update(baos.toByteArray());

		byte[] digest = md5.digest();
		return new RC4(digest);
	}


	/**
	 * Stores the BIFF8 encryption/decryption password for the current thread.  This has been done
	 * using a {@link ThreadLocal} in order to avoid further overloading the various public APIs
	 * (e.g. {@link HSSFWorkbook}) that need this functionality.
	 */
	private static final ThreadLocal<String> _userPasswordTLS = new ThreadLocal<String>();

	/**
	 * Sets the BIFF8 encryption/decryption password for the current thread.
	 *
	 * @param password pass <code>null</code> to clear user password (and use default)
	 */
	public static void setCurrentUserPassword(String password) {
		_userPasswordTLS.set(password);
	}

	/**
	 * @return the BIFF8 encryption/decryption password for the current thread.
	 * <code>null</code> if it is currently unset.
	 */
	public static String getCurrentUserPassword() {
		return _userPasswordTLS.get();
	}
}
