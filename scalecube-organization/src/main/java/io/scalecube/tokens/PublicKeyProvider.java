package io.scalecube.tokens;

public interface PublicKeyProvider {
  String getPublicKey(String token) throws Exception;
}
