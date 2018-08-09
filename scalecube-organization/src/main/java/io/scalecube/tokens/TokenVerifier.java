package io.scalecube.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;

public interface TokenVerifier {

  Profile verify(Token token) throws Exception;

}
