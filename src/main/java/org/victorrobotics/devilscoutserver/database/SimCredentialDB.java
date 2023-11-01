package org.victorrobotics.devilscoutserver.database;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

public class SimCredentialDB implements CredentialDB {
  private final Map<String, byte[]>    nonces;
  private final Map<String, Credentials> data;

  public SimCredentialDB() {
    nonces = new HashMap<>();
    data = new HashMap<>();
    // t=1559,n=xander,r=1234567890abcdef
    data.put("1559,xander", new Credentials(5, "xander", 1559, Permission.SUDO, "bad-salt".getBytes(),
                                          HexFormat.of()
                                                   .parseHex("8cc790682ce826cf353286c241f70c4aae16dbdf1a0274ac1795911917fb535b"),
                                          HexFormat.of()
                                                   .parseHex("86c11c32671aa7d5962eff976284ff81a981e9bfcfded80ea0e38881b8b6e96f")));
  }

  @Override
  public Credentials get(int team, String name) {
    return data.get(team + "," + name);
  }

  @Override
  public void putNonce(byte[] userHash, byte[] nonce) {
    nonces.put(new String(userHash), nonce);
  }

  @Override
  public byte[] getNonce(byte[] userHash) {
    return nonces.remove(new String(userHash));
  }
}
