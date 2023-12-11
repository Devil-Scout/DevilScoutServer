package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.Base64Util.base64Decode;
import static org.victorrobotics.devilscoutserver.Base64Util.base64Encode;
import static org.victorrobotics.devilscoutserver.controller.Controller.SESSION_HEADER;

import org.victorrobotics.devilscoutserver.controller.SessionController.AuthRequest;
import org.victorrobotics.devilscoutserver.controller.SessionController.AuthResponse;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginChallenge;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginRequest;
import org.victorrobotics.devilscoutserver.database.Team;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.User.AccessLevel;
import org.victorrobotics.devilscoutserver.database.UserDatabase;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

import io.javalin.http.Context;
import io.javalin.http.NoContentResponse;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
  void testLogin(TestCase testCase) throws SQLException {
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
  void testAuth(TestCase testCase)
      throws InvalidKeyException, NoSuchAlgorithmException, SQLException {
    testCase.inject();

    Context ctx = mock(Context.class);
    when(ctx.bodyAsClass(AuthRequest.class)).thenReturn(new AuthRequest(testCase.user.team(),
                                                                        testCase.user.username(),
                                                                        testCase.nonce,
                                                                        testCase.clientProof));

    SessionController.auth(ctx);

    verify(ctx).json(argThat((AuthResponse r) -> r.user()
                                                  .accessLevel()
        == testCase.user.accessLevel()
        && testCase.user.fullName()
                        .equals(r.user()
                                 .fullName())
        && Arrays.equals(r.serverSignature(), testCase.serverSignature) && r.session() != null));
  }

  @Test
  void testLogout() {
    Controller.sessions()
              .put(-1L, new Session(-1, -1, 1559));

    Context ctx = mock(Context.class);
    when(ctx.header(SESSION_HEADER)).thenReturn("-1");

    assertThrows(NoContentResponse.class, () -> SessionController.logout(ctx));

    verify(ctx).header(SESSION_HEADER);
    assertNull(Controller.sessions()
                         .get(-1L));
  }

  static Stream<TestCase> testCases() {
    return Stream.<TestCase>builder()
                 .add(new TestCase(new User(5, 1559, "xander", "Xander Bhalla", AccessLevel.SUDO,
                                            base64Decode("YmFkLXNhbHQ="),
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

    void inject() throws SQLException {
      TeamDatabase teams = mock(TeamDatabase.class);
      when(teams.getTeam(user.team())).thenReturn(new Team(user.team(), "Team Name", null));
      Controller.setTeamDB(teams);

      UserDatabase users = mock(UserDatabase.class);
      when(users.getUser(user.team(), user.username())).thenReturn(user);
      Controller.setUserDB(users);

      SessionController.NONCES.add(user.username() + "@" + user.team() + ":" + base64Encode(nonce));
    }
  }
}
