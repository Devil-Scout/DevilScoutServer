package org.victorrobotics.devilscoutserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.victorrobotics.devilscoutserver.controller.SessionController.AuthRequest;
import org.victorrobotics.devilscoutserver.controller.SessionController.AuthResponse;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginChallenge;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginRequest;
import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import io.javalin.http.Context;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SessionControllerTest {
  private static final User   user  = mock(User.class);
  private static final UserDB users = mock(UserDB.class);

  @BeforeAll
  static void setupData() {
    when(user.userID()).thenReturn(5L);
    when(user.username()).thenReturn("xander");
    when(user.team()).thenReturn(1559);
    when(user.fullName()).thenReturn("Xander Bhalla");
    when(user.accessLevel()).thenReturn(User.AccessLevel.SUDO);
    when(user.salt()).thenReturn("bad-salt".getBytes());
    when(user.storedKey()).thenReturn(base64Decode("jMeQaCzoJs81MobCQfcMSq4W298aAnSsF5WRGRf7U1s="));
    when(user.serverKey()).thenReturn(base64Decode("hsEcMmcap9WWLv+XYoT/gamB6b/P3tgOoOOIgbi26W8="));

    when(users.getUser(1559, "xander")).thenReturn(user);
    when(users.getSalt(1559, "xander")).thenReturn("bad-salt".getBytes());

    Controller.setUserDB(users);
  }

  @Test
  void testLogin() {
    LoginRequest request = mock(LoginRequest.class);
    when(request.team()).thenReturn(1559);
    when(request.username()).thenReturn("xander");
    when(request.clientNonce()).thenReturn(base64Decode("EjRWeJCrze8="));

    Context ctx = mock(Context.class);
    when(ctx.bodyAsClass(LoginRequest.class)).thenReturn(request);

    SessionController.login(ctx);

    verify(request, atLeastOnce()).team();
    verify(request, atLeastOnce()).username();
    verify(request, atLeastOnce()).clientNonce();

    verify(ctx).json(argThat((LoginChallenge c) -> Arrays.equals("bad-salt".getBytes(), c.salt())
        && c.nonce().length == 16));
  }

  @Test
  void testAuth() throws InvalidKeyException, NoSuchAlgorithmException {
    SessionDB sessions = mock(SessionDB.class);
    when(users.containsNonce("1559,xander,EjRWeJCrze8AAAAAAAAAAA==")).thenReturn(true);
    Controller.setSessionDB(sessions);

    AuthRequest request = mock(AuthRequest.class);
    when(request.team()).thenReturn(1559);
    when(request.username()).thenReturn("xander");
    when(request.nonce()).thenReturn(base64Decode("EjRWeJCrze8AAAAAAAAAAA=="));
    when(request.clientProof()).thenReturn(base64Decode("UFXKbLn2qYTfZ8clOs0g4DQARrl+M505nIPADfX7zwI="));

    Context ctx = mock(Context.class);
    when(ctx.bodyAsClass(AuthRequest.class)).thenReturn(request);

    SessionController.auth(ctx);

    verify(request, atLeastOnce()).username();
    verify(request, atLeastOnce()).team();
    verify(request, atLeastOnce()).nonce();
    verify(request, atLeastOnce()).clientProof();

    verify(sessions).registerSession(any(Session.class));
    verify(ctx).json(argThat((AuthResponse r) -> r.accessLevel() == User.AccessLevel.SUDO
        && "Xander Bhalla".equals(r.fullName())
        && Arrays.equals(r.serverSignature(),
                         base64Decode("HLgptJ2+owkHw73MIA9bmeLG0hxGEwsEhgUwl85HeNQ="))
        && base64Decode(r.sessionID()).length == 8));
  }

  @Test
  void testLogout() {
    Session session = mock(Session.class);
    when(session.getSessionID()).thenReturn("vcOVI8k869c=");

    SessionDB sessions = mock(SessionDB.class);
    when(sessions.getSession("vcOVI8k869c=")).thenReturn(session);
    Controller.setSessionDB(sessions);

    Context ctx = mock(Context.class);
    when(ctx.header("X-DS-SESSION-KEY")).thenReturn("vcOVI8k869c=");

    SessionController.logout(ctx);

    verify(ctx).header("X-DS-SESSION-KEY");
    verify(sessions).getSession("vcOVI8k869c=");
    verify(sessions).deleteSession(session);
  }

  private static byte[] base64Decode(String base64) {
    return Base64.getDecoder()
                 .decode(base64);
  }
}
