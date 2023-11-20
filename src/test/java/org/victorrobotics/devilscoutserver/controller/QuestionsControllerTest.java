package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.DriveTeamQuestions;
import org.victorrobotics.devilscoutserver.data.MatchQuestions;
import org.victorrobotics.devilscoutserver.data.PitQuestions;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;

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
  private static final String SESSION_ID = "vcOVI8k869c=";
  private static SessionDB    SESSIONS;

  @BeforeAll
  static void injectSession() {
    Session session = new Session(SESSION_ID, 5, 1559, UserAccessLevel.ADMIN);
    SESSIONS = mock(SessionDB.class);
    when(SESSIONS.getSession(SESSION_ID)).thenReturn(session);
    Controller.setSessionDB(SESSIONS);
  }

  @Test
  void testMatchQuestions() {
    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(SESSION_ID);

    QuestionsController.matchQuestions(ctx);

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
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(SESSION_ID);

    QuestionsController.pitQuestions(ctx);

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
  void testDriveTeamQuestions() {
    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(SESSION_ID);

    QuestionsController.driveTeamQuestions(ctx);

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
