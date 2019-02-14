package io.scalecube.organization.repository.couchbase.admin;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordGenerator {

  private static final String salt = "DGE$5SGr@3VsHYUMas2323E4d57vfBfFSTRU@!DSH(*%FDSdfg13sgfsg";

  /**
   * Returns a digest string form of the message argument.
   *
   * @param message the message to digest
   * @return a digest string
   */
  public static String md5Hash(String message) {
    String md5 = "";
    if (null == message) {
      return null;
    }

    message = message + salt; // adding a salt to the string before it gets hashed.
    try {
      MessageDigest digest =
          MessageDigest.getInstance("MD5"); // Create MessageDigest object for MD5
      digest.update(
          message.getBytes(), 0, message.length()); // Update input string in message digest
      md5 =
          new BigInteger(1, digest.digest())
              .toString(16); // Converts message digest value in base 16 (hex)

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return md5;
  }
}
