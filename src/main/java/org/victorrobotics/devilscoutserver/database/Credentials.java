package org.victorrobotics.devilscoutserver.database;

public record Credentials(long userID,
                          String username,
                          String fullname,
                          int team,
                          Permission permissions,
                          byte[] salt,
                          byte[] storedKey,
                          byte[] serverKey) {}
