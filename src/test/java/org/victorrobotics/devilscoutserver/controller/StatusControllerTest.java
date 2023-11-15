package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.ServerStatus;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;

import io.javalin.http.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class StatusControllerTest {
  @Test
  void testStatus() {
    String sessionID = "vcOVI8k869c=";
    Session session = new Session(sessionID, 5, 1559, UserAccessLevel.USER);

    SessionDB sessions = mock(SessionDB.class);
    when(sessions.getSession(sessionID)).thenReturn(session);
    Controller.setSessionDB(sessions);

    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(sessionID);

    StatusController.status(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(sessions).getSession(sessionID);
    verify(ctx).json(any(ServerStatus.class));
  }
}
