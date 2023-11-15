package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.Utils.base64Decode;
import static org.victorrobotics.devilscoutserver.controller.Controller.SESSION_HEADER;

import org.victorrobotics.devilscoutserver.data.AuthRequest;
import org.victorrobotics.devilscoutserver.data.AuthResponse;
import org.victorrobotics.devilscoutserver.data.LoginChallenge;
import org.victorrobotics.devilscoutserver.data.LoginRequest;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.User;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.javalin.http.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SessionControllerTest {
  @ParameterizedTest
  @MethodSource("userTestCases")
  void testLogin(Supplier<UserTestCase> testCaseSupplier) {
    UserTestCase testCase = testCaseSupplier.get();
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
  @MethodSource("userTestCases")
  void testAuth(Supplier<UserTestCase> testCaseSupplier)
      throws InvalidKeyException, NoSuchAlgorithmException {
    SessionDB sessions = mock(SessionDB.class);
    Controller.setSessionDB(sessions);

    UserTestCase testCase = testCaseSupplier.get();
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

  @ParameterizedTest
  @MethodSource("sessionTestCases")
  void testLogout(String sessionID) {
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

  static Stream<Supplier<UserTestCase>> userTestCases() {
    return Stream.<Supplier<UserTestCase>>builder()
                 .add(mockUser(5, 1559, "xander", "Xander Bhalla", UserAccessLevel.SUDO,
                               "YmFkLXNhbHQ=", "EjRWeJCrze8AAAAAAAAAAA==",
                               "jMeQaCzoJs81MobCQfcMSq4W298aAnSsF5WRGRf7U1s=",
                               "hsEcMmcap9WWLv+XYoT/gamB6b/P3tgOoOOIgbi26W8=",
                               "UFXKbLn2qYTfZ8clOs0g4DQARrl+M505nIPADfX7zwI=",
                               "HLgptJ2+owkHw73MIA9bmeLG0hxGEwsEhgUwl85HeNQ="))
                 .build();
  }

  static Stream<String> sessionTestCases() {
    return Stream.<String>builder()
                 .add("vcOVI8k869c=")
                 .build();
  }

  static Supplier<UserTestCase> mockUser(long userID, int team, String username, String fullName,
                                         UserAccessLevel accessLevel, String salt, String nonce,
                                         String storedKey, String serverKey, String clientProof,
                                         String serverSignature) {
    return () -> {
      User user = new User(userID, username, fullName, team, accessLevel, base64Decode(salt),
                           base64Decode(storedKey), base64Decode(serverKey));
      UserDB users = mock(UserDB.class);
      when(users.getUser(team, username)).thenReturn(user);
      when(users.getSalt(team, username)).thenReturn(user.salt());
      when(users.containsNonce(team + "," + username + "," + nonce)).thenReturn(true);
      Controller.setUserDB(users);

      return new UserTestCase(user, base64Decode(nonce), base64Decode(clientProof),
                              base64Decode(serverSignature));
    };
  }

  record UserTestCase(User user,
                      byte[] clientNonce,
                      byte[] nonce,
                      byte[] clientProof,
                      byte[] serverSignature) {
    UserTestCase(User user, byte[] nonce, byte[] clientProof, byte[] serverSignature) {
      this(user, Arrays.copyOf(nonce, 8), nonce, clientProof, serverSignature);
    }
  }
}
