package org.victorrobotics.devilscoutserver.database;

public record User(long userID,
                   String username,
                   String fullName,
                   int team,
                   Permission permissions,
                   byte[] salt,
                   byte[] storedKey,
                   byte[] serverKey) {}
