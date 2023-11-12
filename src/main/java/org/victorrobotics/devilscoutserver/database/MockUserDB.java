package org.victorrobotics.devilscoutserver.database;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public final class MockUserDB implements UserDB {
  private final Set<String>       nonces;
  private final Map<String, User> data;

  MockUserDB() {
    nonces = new HashSet<>();
    data = new HashMap<>();
    data.put("1559,xander",
             new User(5, "xander", "Xander Bhalla", 1559, User.AccessLevel.SUDO,
                      "bad-salt".getBytes(),
                      parseHex("8cc790682ce826cf353286c241f70c4aae16dbdf1a0274ac1795911917fb535b"),
                      parseHex("86c11c32671aa7d5962eff976284ff81a981e9bfcfded80ea0e38881b8b6e96f")));
  }

  @Override
  public User getUser(int team, String name) {
    return data.get(team + "," + name);
  }

  @Override
  public void putNonce(String nonceID) {
    nonces.add(nonceID);
  }

  @Override
  public boolean containsNonce(String nonceID) {
    return nonces.contains(nonceID);
  }

  private static byte[] parseHex(String hex) {
    return HexFormat.of()
                    .parseHex(hex);
  }
}
