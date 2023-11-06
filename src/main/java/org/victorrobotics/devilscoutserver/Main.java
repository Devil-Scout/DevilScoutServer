package org.victorrobotics.devilscoutserver;

import org.victorrobotics.devilscoutserver.auth.SCRAM_AuthHandler;
import org.victorrobotics.devilscoutserver.auth.SCRAM_LoginHandler;
import org.victorrobotics.devilscoutserver.database.SimCredentialDB;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import io.javalin.Javalin;

public class Main {
  public static void main(String... args) {
    // printCredentials();
    SimCredentialDB database = new SimCredentialDB();

    Javalin.create()
           .get("/test", ctx -> ctx.result("Hello!"))
           .post("/auth", new SCRAM_AuthHandler(database))
           .post("/login", new SCRAM_LoginHandler(database))
           .start(80);
  }

  private static void printCredentials() {
    HexFormat hexFormat = HexFormat.of();
    String hashAlgorithm = "SHA-256";
    String hmacAgorithm = "HmacSHA256";
    int hashSizeBytes = 32;

    int team = 1559;
    String name = "xander";
    String password = "password";

    byte[] salt = hexFormat.parseHex("6261642d73616c74");
    byte[] nonce = hexFormat.parseHex("1234567890abcdef0000000000000000");

    SecretKeyFactory factory;
    MessageDigest hashFunction;
    Mac hmacFunction;
    try {
      hashFunction = MessageDigest.getInstance(hashAlgorithm);
      hmacFunction = Mac.getInstance(hmacAgorithm);
      hmacFunction.init(new SecretKeySpec(hexFormat.parseHex("e9d94660c39d65c38fbad91c358f14da0eef2bd6"),
                                          hmacAgorithm));
      factory = SecretKeyFactory.getInstance("PBKDF2With" + hmacAgorithm);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }

    System.out.println("saltedPassword");
    KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 4096, hashSizeBytes * 8);
    SecretKey saltedPassword;
    try {
      saltedPassword = factory.generateSecret(keySpec);
    } catch (InvalidKeySpecException e) {
      throw new IllegalStateException(e);
    }
    System.out.println(hexFormat.formatHex(saltedPassword.getEncoded()));

    System.out.println("clientKey");
    try {
      hmacFunction.init(saltedPassword);
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] clientKey = hmacFunction.doFinal("Client Key".getBytes());
    System.out.println(hexFormat.formatHex(clientKey));

    System.out.println("serverKey");
    byte[] serverKey = hmacFunction.doFinal("Server Key".getBytes());
    System.out.println(hexFormat.formatHex(serverKey));

    System.out.println("storedKey");
    byte[] storedKey = hashFunction.digest(clientKey);
    System.out.println(hexFormat.formatHex(storedKey));

    System.out.println("clientSignature");
    try {
      hmacFunction.init(new SecretKeySpec(storedKey, hmacAgorithm));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] clientSignature = hmacFunction.doFinal(toStr(team + name, nonce));
    System.out.println(hexFormat.formatHex(clientSignature));

    System.out.println("clientProof");
    byte[] clientProof = new byte[hashSizeBytes];
    for (int i = 0; i < clientProof.length; i++) {
      clientProof[i] = (byte) (clientSignature[i] ^ clientKey[i]);
    }
    System.out.println(hexFormat.formatHex(clientProof));

    System.out.println("clientKey (server)");
    byte[] clientKey2 = new byte[hashSizeBytes];
    for (int i = 0; i < clientKey2.length; i++) {
      clientKey2[i] = (byte) (clientProof[i] ^ clientSignature[i]);
    }
    System.out.println(hexFormat.formatHex(clientKey2));

    System.out.println("storedKey (server)");
    byte[] storedKey2 = hashFunction.digest(clientKey2);
    System.out.println(hexFormat.formatHex(storedKey2));

    System.out.println("serverSignature");
    try {
      hmacFunction.init(new SecretKeySpec(serverKey, hmacAgorithm));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] serverSignature2 = hmacFunction.doFinal(toStr(team + name, nonce));
    System.out.println(hexFormat.formatHex(serverSignature2));
  }

  private static byte[] toStr(String username, byte[] nonce) {
    byte[] bytes = new byte[username.length() + nonce.length];
    byte[] userBytes = username.getBytes();
    System.arraycopy(userBytes, 0, bytes, 0, userBytes.length);
    System.arraycopy(nonce, 0, bytes, userBytes.length, nonce.length);
    return bytes;
  }
}
