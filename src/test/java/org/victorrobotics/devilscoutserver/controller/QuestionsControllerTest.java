package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.controller.Controller.Session;
import org.victorrobotics.devilscoutserver.controller.QuestionController.DriveTeamQuestions;
import org.victorrobotics.devilscoutserver.controller.QuestionController.MatchQuestions;
import org.victorrobotics.devilscoutserver.controller.QuestionController.PitQuestions;
import org.victorrobotics.devilscoutserver.database.User.AccessLevel;
import org.victorrobotics.devilscoutserver.database.UserDatabase;

import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.javalin.http.Context;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QuestionsControllerTest {
  @BeforeAll
  static void injectSession() throws SQLException {
    Controller.sessions()
              .put("test", new Session("test", -1, 1559));

    UserDatabase users = mock(UserDatabase.class);
    when(users.getAccessLevel(-1)).thenReturn(AccessLevel.SUDO);
    Controller.setUserDB(users);
  }

  @Test
  void testMatchQuestions() {
    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn("test");

    QuestionController.matchQuestions(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(ctx).json(argThat((String s) -> {
      try {
        new JsonMapper().readValue(s, MatchQuestions.class);
        return true;
      } catch (JsonProcessingException e) {}
      return false;
    }));
  }

  @Test
  void testPitQuestions() {
    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn("test");

    QuestionController.pitQuestions(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(ctx).json(argThat((String s) -> {
      try {
        new JsonMapper().readValue(s, PitQuestions.class);
        return true;
      } catch (JsonProcessingException e) {}
      return false;
    }));
  }

  @Test
  void testDriveTeamQuestions() throws SQLException {
    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn("test");

    QuestionController.driveTeamQuestions(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(ctx).json(argThat((String s) -> {
      try {
        new JsonMapper().readValue(s, DriveTeamQuestions.class);
        return true;
      } catch (JsonProcessingException e) {}
      return false;
    }));
  }
}
