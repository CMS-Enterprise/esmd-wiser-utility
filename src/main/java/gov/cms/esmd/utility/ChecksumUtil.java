package gov.cms.esmd.utility;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;

/**
 * Checksum utilities. Defaults to SHA-256.
 *
 * Notes:
 * - Uses constant-time comparisons via MessageDigest.isEqual.
 * - Validates client-supplied checksums in HEX or Base64.
 * - Streams file hashing to avoid loading entire files into memory.
 *
 * Author: Srinivas Eadara;
 */
public final class ChecksumUtil {
	private static final Logger logger = LoggerFactory.getLogger(ChecksumUtil.class);

	private static final String DEFAULT_ALG = "SHA-256";
	private static final int BUFFER_SIZE = 8192;

	private ChecksumUtil() {}

	/* ==============================
	 * Public API – SHA-256 by default
	 * ============================== */

	/** Calculate SHA-256 (hex) of a byte array. */
	public static String sha256Hex(byte[] data) {
		Objects.requireNonNull(data, "data");
		logger.debug("Calculating SHA-256 for data size {}", data.length);
		return toHex(digest(DEFAULT_ALG, data));
	}

	/** Calculate SHA-256 (hex) of a String using UTF-8. */
	public static String sha256Hex(String input) {
		Objects.requireNonNull(input, "input");
		return sha256Hex(input.getBytes(StandardCharsets.UTF_8));
	}

	/** Calculate SHA-256 (hex) of a file (streaming). */
	public static String sha256Hex(Path file) {
		Objects.requireNonNull(file, "file");
		logger.debug("Calculating SHA-256 for file {}", file);
		return toHex(digestFile(DEFAULT_ALG, file));
	}

	/**
	 * Validate a client-provided checksum (hex or Base64) against original bytes.
	 * Returns true if equal, using constant-time comparison.
	 */
	public static boolean validateChecksum(String clientValue, byte[] originalData) {
		Objects.requireNonNull(clientValue, "clientValue");
		Objects.requireNonNull(originalData, "originalData");

		logger.debug("Validating checksum value: {}", clientValue);

		byte[] expected = digest(DEFAULT_ALG, originalData);

		// Try HEX first
		byte[] provided = decodeHexOrNull(clientValue);
		if (provided == null) {
			// Try Base64
			provided = decodeBase64OrNull(clientValue);
		}

		if (provided == null) {
			logger.debug("Client value is neither valid HEX nor Base64.");
			return false;
		}

		boolean match = MessageDigest.isEqual(expected, provided);
		logger.debug("Checksum {}.", match ? "validated successfully" : "validation failed");
		return match;
	}

	/**
	 * Validate a client-provided checksum (hex or Base64) against a file’s SHA-256.
	 */
	public static boolean validateFileChecksum(String clientValue, Path file) {
		Objects.requireNonNull(clientValue, "clientValue");
		Objects.requireNonNull(file, "file");

		logger.debug("Validating file checksum. file={}, value={}", file, clientValue);
		byte[] expected = digestFile(DEFAULT_ALG, file);

		byte[] provided = decodeHexOrNull(clientValue);
		if (provided == null) {
			provided = decodeBase64OrNull(clientValue);
		}
		if (provided == null) return false;

		boolean match = MessageDigest.isEqual(expected, provided);
		logger.debug("File checksum {}.", match ? "validated successfully" : "validation failed");
		return match;
	}

	/* ==============================
	 * MD5 helpers (legacy)
	 * ============================== */

	/** Calculate MD5 (Base64) of a file. Prefer SHA-256 instead. */
	@Deprecated
	public static String md5Base64(Path file) {
		Objects.requireNonNull(file, "file");
		logger.debug("Calculating MD5 (Base64) for file {}", file);
		return Base64.getEncoder().encodeToString(digestFile("MD5", file));
	}

	/** Calculate MD5 (hex) of a String (UTF-8). Prefer SHA-256 instead. */
	@Deprecated
	public static String md5Hex(String input) {
		Objects.requireNonNull(input, "input");
		logger.debug("Calculating MD5 (hex) for input string length {}", input.length());
		return toHex(digest("MD5", input.getBytes(StandardCharsets.UTF_8)));
	}

    public static String checkMD5(String filePath) {
        try {
            File file = new File(filePath);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] enc = md.digest(FileUtils.readFileToByteArray(file));
            String md5Sum = org.apache.commons.codec.binary.Base64.encodeBase64String(enc);
            return md5Sum;
        } catch (IOException | NoSuchAlgorithmException ex) {
            System.out.println(((Exception)ex).getMessage());
            return null;
        }
    }

	/* ==============================
	 * Internal helpers
	 * ============================== */

	private static byte[] digest(String algorithm, byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			return md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Digest algorithm unavailable: " + algorithm, e);
		}
	}

	private static byte[] digestFile(String algorithm, Path file) {
		try (InputStream in = Files.newInputStream(file);
			 DigestInputStream din = new DigestInputStream(in, MessageDigest.getInstance(algorithm))) {
			byte[] buffer = new byte[BUFFER_SIZE];
			while (din.read(buffer) != -1) {
				// digest updated automatically
			}
			return din.getMessageDigest().digest();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Digest algorithm unavailable: " + algorithm, e);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read file: " + file, e);
		}
	}

	/* ==============================
	 * Encoding utilities
	 * ============================== */

	/** Encode bytes to lowercase hex. */
	public static String toHex(byte[] bytes) {
		char[] out = new char[bytes.length * 2];
		final char[] HEX = "0123456789abcdef".toCharArray();
		for (int i = 0, j = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			out[j++] = HEX[v >>> 4];
			out[j++] = HEX[v & 0x0F];
		}
		return new String(out);
	}

	/** Decode hex string to bytes; returns null if invalid. Accepts upper/lowercase. */
	public static byte[] decodeHexOrNull(String s) {
		String v = s.trim();
		int len = v.length();
		if ((len & 1) != 0) return null; // must be even length
		byte[] out = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int hi = hexValue(v.charAt(i));
			int lo = hexValue(v.charAt(i + 1));
			if (hi < 0 || lo < 0) return null;
			out[i / 2] = (byte) ((hi << 4) + lo);
		}
		return out;
	}

	/** Decode Base64; returns null if invalid. */
	public static byte[] decodeBase64OrNull(String s) {
		try {
			return Base64.getDecoder().decode(s.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** Returns 0-15 for hex char, or -1 if invalid. */
	private static int hexValue(char c) {
		char x = Character.toLowerCase(c);
		if (x >= '0' && x <= '9') return x - '0';
		if (x >= 'a' && x <= 'f') return 10 + (x - 'a');
		return -1;
	}

	/* ==============================
	 * Advanced: generic digest helpers (optional)
	 * ============================== */

	/** Calculate digest (hex) with a specified algorithm (e.g., "SHA-256", "SHA-512"). */
	public static String digestHex(String algorithm, byte[] data) {
		return toHex(digest(algorithm, data));
	}

	/** Calculate digest (hex) of a file with a specified algorithm. */
	public static String digestHex(String algorithm, Path file) {
		return toHex(digestFile(algorithm, file));
	}

	/** Validate a checksum (hex or Base64) against supplied bytes for a given algorithm. */
	public static boolean validateChecksum(String algorithm, String clientValue, byte[] data) {
		byte[] expected = digest(algorithm, data);
		byte[] provided = decodeHexOrNull(clientValue);
		if (provided == null) provided = decodeBase64OrNull(clientValue);
		return provided != null && MessageDigest.isEqual(expected, provided);
	}
}
