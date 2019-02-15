package io.scalecube.tokens;

abstract class TokenUtils {
  protected static String removeSignature(String token) throws InvalidTokenException {
    String[] parts = token.split("\\.");
    if (parts.length == 2 && token.endsWith(".")) {
      parts = new String[] {parts[0], parts[1], ""};
    }

    if (parts.length != 3) {
      throw new InvalidTokenException(
          String.format("The token was expected to have 3 parts, but got %s.", parts.length));
    } else {
      return String.format("%s.%s.", parts[0], parts[1]);
    }
  }
}
