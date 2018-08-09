package io.scalecube.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtAuthenticatorImpl;

import io.scalecube.security.Profile;

import java.io.IOException;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

class TokenVerifierImpl implements TokenVerifier {

  private PublicKey publicKey;

  @Override
  public Profile verify(Token token) throws Exception {
    JwtAuthenticator authenticator = new JwtAuthenticatorImpl
        .Builder()
        .keyResolver(map -> Optional.of(getPublicKey()))
        .build();

    return authenticator.authenticate(token.token());
  }

  private PublicKey getPublicKey() {
    if (publicKey == null) {
      Properties properties = new Properties();

      try {
        properties.load(getClass().getResourceAsStream("/settings.properties"));
      } catch (IOException ex) {
        throw new RuntimeException("Failed to initialize", ex);
      }
      String key = properties.getProperty("token.verify.public.key");

      try {
        byte[] byteKey = Base64.getDecoder().decode(Objects.requireNonNull(key).getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        publicKey = kf.generatePublic(keySpec);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    return publicKey;
  }
}
