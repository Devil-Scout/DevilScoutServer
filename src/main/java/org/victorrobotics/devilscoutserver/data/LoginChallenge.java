package org.victorrobotics.devilscoutserver.data;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import java.util.Arrays;
import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record LoginChallenge(@OpenApiRequired @OpenApiExample("mHZUMhCrze8=") byte[] salt,
                             @OpenApiRequired
                             @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce) {
  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof LoginChallenge other
        && Arrays.equals(salt(), other.salt()) && Arrays.equals(nonce(), other.nonce()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(salt()), Arrays.hashCode(nonce()));
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("LoginChallenge[")
     .append("salt=")
     .append(base64Encode(salt()))
     .append(",nonce=")
     .append(base64Encode(nonce()))
     .append("]");
    return b.toString();
  }
}
