package io.scalecube.tokens;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public final class IdGenerator {

  /**
   * The default message digest algorithm to use if we cannot use the requested one.
   */
  private static final String DEFAULT_ALGORITHM = "MD5";

  private static final int DEFAULT_SIZE = 10;

  private static ThreadLocal<MessageDigest> digestHolder =
      ThreadLocal.withInitial(IdGenerator::getDigest);

  /**
   * Generates a unique id using this class default algorithm and byte array size.
   *
   * @return a string representation of a unique id.
   */
  public static String generateId() {
    byte[] buffer = new byte[DEFAULT_SIZE];

    int resultLenCounter = 0;
    MessageDigest digest = digestHolder.get();
    int resultLen = DEFAULT_SIZE * 2;
    char[] result = new char[resultLen];

    while (resultLenCounter < resultLen) {
      ThreadLocalRandom.current().nextBytes(buffer);
      buffer = digest.digest(buffer);
      for (int j = 0; j < buffer.length && resultLenCounter < resultLen; j++) {
        result[resultLenCounter++] = forHexDigit((buffer[j] & 0xf0) >> 4);
        result[resultLenCounter++] = forHexDigit(buffer[j] & 0x0f);
      }
    }

    digest.reset();
    return new String(result);
  }

  private static char forHexDigit(int digit) {
    if (digit < 10) {
      return (char) ('0' + digit);
    }
    return (char) ('A' - 10 + digit);
  }

  /**
   * Return the MessageDigest object to be used for calculating session identifiers. If none has
   * been created yet, initialize one the first time this method is called.
   *
   * @return The hashing algorithm
   */
  private static MessageDigest getDigest() {
    MessageDigest digest;
    try {
      // The message digest algorithm to be used when generating session identifiers.
      // This must be an algorithm supported by the <code>java.security.
      // MessageDigest</code> class on your platform.
      digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
    } catch (NoSuchAlgorithmException ex) {
      try {
        digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
      } catch (NoSuchAlgorithmException ex2) {
        throw new IllegalStateException("No algorithms for IdGenerator");
      }
    }
    return digest;
  }

  // TODO
  // This is to make the point that we need toString to return something
  // that includes some sort of system identifier as does the default.
  // Don't change this unless you really know what you are doing.
  //  @Override
  //  public String toString() {
  //    return super.toString();
  //  }

}
