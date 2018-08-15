package io.scalecube.tokens.jwk;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Represents a JSON Web Key (JWK) used to verify the signature of JWTs.
 */
public class Jwk {

  private static final String PUBLIC_KEY_ALGORITHM = "RSA";

  private final String id;
  private final String type;
  private final String algorithm;
  private final String usage;
  private final Map<String, Object> additionalAttributes;

  /**
   * Creates a new Jwk.
   *
   * @param id kid
   * @param type kyt
   * @param algorithm alg
   * @param usage use
   * @param operations key_ops
   * @param certificateUrl x5u
   * @param certificateChain x5c
   * @param certificateThumbprint x5t
   * @param additionalAttributes additional attributes not part of the standard ones
   */
  public Jwk(String id, String type, String algorithm, String usage,
      List<String> operations, String certificateUrl, List<String>
      certificateChain, String certificateThumbprint,
      Map<String, Object> additionalAttributes) {
    this.id = id;
    this.type = type;
    this.algorithm = algorithm;
    this.usage = usage;
    this.additionalAttributes = additionalAttributes;
  }


  protected static Jwk fromValues(Map<String, Object> map) {
    Map<String, Object> values = new HashMap<>(map);
    String kid = (String) values.remove("kid");
    String kty = (String) values.remove("kty");
    String alg = (String) values.remove("alg");
    String use = (String) values.remove("use");
    List<String> keyOps = (List<String>) values.remove("key_ops");
    String x5u = (String) values.remove("x5u");
    List<String> x5c = (List<String>) values.remove("x5c");
    String x5t = (String) values.remove("x5t");

    if (kty == null) {
      throw new IllegalArgumentException("Attributes " + map + " are not from a valid jwk");
    }

    return new Jwk(kid, kty, alg, use, keyOps, x5u, x5c, x5t, values);
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }


  /**
   * Returns a {@link PublicKey} if the {@code 'alg'} is {@code 'RSA'}.
   *
   * @return a public key
   * @throws InvalidPublicKeyException if the key cannot be built or the key type is not RSA
   */
  public PublicKey getPublicKey() throws InvalidPublicKeyException {
    if (!PUBLIC_KEY_ALGORITHM.equalsIgnoreCase(type)) {
      return null;
    }
    try {
      KeyFactory kf = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM);
      BigInteger modulus = new BigInteger(1, Base64.decodeBase64(stringValue("n")));
      BigInteger exponent = new BigInteger(1, Base64.decodeBase64(stringValue("e")));
      return kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
    } catch (InvalidKeySpecException ex) {
      throw new InvalidPublicKeyException("Invalid public key", ex);
    } catch (NoSuchAlgorithmException ex) {
      throw new InvalidPublicKeyException("Invalid algorithm to generate key", ex);
    }
  }

  private String stringValue(String key) {
    return (String) additionalAttributes.get(key);
  }

  @Override
  public String toString() {
    return this.getClass().getName()
        + "{"
        + "kid=" + id
        + ",kyt=" + type
        + ",alg=" + algorithm
        + ",use=" + usage
        + ",extras=" + additionalAttributes
        + "}";
  }
}
