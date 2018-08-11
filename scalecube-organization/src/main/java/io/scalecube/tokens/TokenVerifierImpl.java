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
    final PublicKey publicKey = getPublicKey();
    JwtAuthenticator authenticator = new JwtAuthenticatorImpl
        .Builder()
        .keyResolver(map -> Optional.of(publicKey))
        .build();

    return authenticator.authenticate(token.token());
  }

  private PublicKey getPublicKey() throws Exception {
    if (publicKey == null) {
      String key = loadKeyFromSettingsFile();
      generatePublicKey(key);
    }

    return publicKey;
  }


  private String loadKeyFromSettingsFile() throws Exception {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getResourceAsStream("/settings.properties"));
    } catch (IOException ex) {
      throw new Exception("Failed to initialize settings file", ex);
    }
    return properties.getProperty("token.verify.public.key");
  }

  private void generatePublicKey(String key) throws Exception {
    try {
      byte[] byteKey = Base64.getDecoder().decode(Objects.requireNonNull(key).getBytes());
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(byteKey);
      KeyFactory kf = KeyFactory.getInstance("RSA");

      publicKey = kf.generatePublic(keySpec);
    } catch (Exception ex) {
      throw new Exception("Failed to create public key", ex);
    }
  }

}
