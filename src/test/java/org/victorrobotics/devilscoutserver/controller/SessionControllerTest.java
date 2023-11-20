package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.Utils.base64Decode;
import static org.victorrobotics.devilscoutserver.Utils.base64Encode;
import static org.victorrobotics.devilscoutserver.controller.Controller.SESSION_HEADER;

import org.victorrobotics.devilscoutserver.controller.SessionController.AuthRequest;
import org.victorrobotics.devilscoutserver.controller.SessionController.AuthResponse;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginChallenge;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginRequest;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.User;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Stream;

import io.javalin.http.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SessionControllerTest {
  @ParameterizedTest
  @MethodSource("testCases")
  void testLogin(TestCase testCase) {
    testCase.inject();
    LoginRequest request = mock(LoginRequest.class);
    when(request.team()).thenReturn(testCase.user.team());
    when(request.username()).thenReturn(testCase.user.username());
    when(request.clientNonce()).thenReturn(testCase.clientNonce);

    Context ctx = mock(Context.class);
    when(ctx.bodyAsClass(LoginRequest.class)).thenReturn(request);

    SessionController.login(ctx);

    verify(request, atLeastOnce()).team();
    verify(request, atLeastOnce()).username();
    verify(request, atLeastOnce()).clientNonce();

    verify(ctx).json(argThat((LoginChallenge c) -> Arrays.equals(testCase.user.salt(), c.salt())
        && c.nonce().length == 16));
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void testAuth(TestCase testCase) throws InvalidKeyException, NoSuchAlgorithmException {
    SessionDB sessions = mock(SessionDB.class);
    Controller.setSessionDB(sessions);
    testCase.inject();

    AuthRequest request = mock(AuthRequest.class);
    when(request.team()).thenReturn(testCase.user.team());
    when(request.username()).thenReturn(testCase.user.username());
    when(request.nonce()).thenReturn(testCase.nonce);
    when(request.clientProof()).thenReturn(testCase.clientProof);

    Context ctx = mock(Context.class);
    when(ctx.bodyAsClass(AuthRequest.class)).thenReturn(request);

    SessionController.auth(ctx);

    verify(request, atLeastOnce()).username();
    verify(request, atLeastOnce()).team();
    verify(request, atLeastOnce()).nonce();
    verify(request, atLeastOnce()).clientProof();

    verify(sessions).registerSession(any(Session.class));
    verify(ctx).json(argThat((AuthResponse r) -> r.accessLevel() == testCase.user.accessLevel()
        && testCase.user.fullName()
                        .equals(r.fullName())
        && Arrays.equals(r.serverSignature(), testCase.serverSignature)
        && base64Decode(r.sessionID()).length == 8));
  }

  @Test
  void testLogout() {
    String sessionID = "vcOVI8k869c=";
    Session session = mock(Session.class);
    when(session.getSessionID()).thenReturn(sessionID);

    SessionDB sessions = mock(SessionDB.class);
    when(sessions.getSession(sessionID)).thenReturn(session);
    Controller.setSessionDB(sessions);

    Context ctx = mock(Context.class);
    when(ctx.header(SESSION_HEADER)).thenReturn(sessionID);

    SessionController.logout(ctx);

    verify(ctx).header(SESSION_HEADER);
    verify(sessions).getSession(sessionID);
    verify(sessions).deleteSession(session);
  }

  static Stream<TestCase> testCases() {
    return Stream.<TestCase>builder()
                 .add(new TestCase(new User((long) 5, "xander", "Xander Bhalla", 1559,
                                            UserAccessLevel.SUDO, base64Decode("YmFkLXNhbHQ="),
                                            base64Decode("jMeQaCzoJs81MobCQfcMSq4W298aAnSsF5WRGRf7U1s="),
                                            base64Decode("hsEcMmcap9WWLv+XYoT/gamB6b/P3tgOoOOIgbi26W8=")),
                                   base64Decode("EjRWeJCrze8AAAAAAAAAAA=="),
                                   base64Decode("UFXKbLn2qYTfZ8clOs0g4DQARrl+M505nIPADfX7zwI="),
                                   base64Decode("HLgptJ2+owkHw73MIA9bmeLG0hxGEwsEhgUwl85HeNQ=")))
                 .build();
  }

  record TestCase(User user,
                  byte[] clientNonce,
                  byte[] nonce,
                  byte[] clientProof,
                  byte[] serverSignature) {
    TestCase(User user, byte[] nonce, byte[] clientProof, byte[] serverSignature) {
      this(user, Arrays.copyOf(nonce, 8), nonce, clientProof, serverSignature);
    }

    void inject() {
      UserDB users = new UserDB();
      users.addUser(user);
      users.putNonce(user.team() + "," + user.username() + "," + base64Encode(nonce));
      Controller.setUserDB(users);
    }
  }
}
