package org.victorrobotics.devilscoutserver;

import org.victorrobotics.devilscoutserver.auth.SCRAM_AuthHandler;
import org.victorrobotics.devilscoutserver.auth.SCRAM_LoginHandler;
import org.victorrobotics.devilscoutserver.database.SimCredentialDB;

import io.javalin.Javalin;

public class Main {
  public static void main(String... args) {
    SimCredentialDB database = new SimCredentialDB();

    Javalin.create()
           .get("/status", ctx -> ctx.result("{\"status\":\"okay\"}"))
           .post("/auth", new SCRAM_AuthHandler(database))
           .post("/login", new SCRAM_LoginHandler(database))
           .start(80);
  }
}
