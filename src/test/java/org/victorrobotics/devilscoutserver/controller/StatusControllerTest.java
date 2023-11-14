package org.victorrobotics.devilscoutserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.victorrobotics.devilscoutserver.controller.StatusController.Status;
import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.SessionDB;

import io.javalin.http.Context;

import org.junit.jupiter.api.Test;

class StatusControllerTest {
  @Test
  void testStatus() {
    Session session = mock(Session.class);
    when(session.getSessionID()).thenReturn("vcOVI8k869c=");

    SessionDB sessions = mock(SessionDB.class);
    when(sessions.getSession("vcOVI8k869c=")).thenReturn(session);
    Controller.setSessionDB(sessions);

    Context ctx = mock(Context.class);
    when(ctx.header("X-DS-SESSION-KEY")).thenReturn("vcOVI8k869c=");

    StatusController.status(ctx);

    verify(ctx).header("X-DS-SESSION-KEY");
    verify(ctx).json(any(Status.class));
    verify(sessions).getSession("vcOVI8k869c=");
  }
}
