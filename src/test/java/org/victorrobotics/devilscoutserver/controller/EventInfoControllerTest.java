package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.EventInfoCache;
import org.victorrobotics.devilscoutserver.data.EventInfo;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.TeamConfig;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.TeamConfigDB;

import io.javalin.http.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EventInfoControllerTest {
  private static final String SESSION_ID = "vcOVI8k869c=";
  private static SessionDB    SESSIONS;

  @BeforeAll
  static void injectSession() {
    Session session = new Session(SESSION_ID, 5, 1559, UserAccessLevel.ADMIN);
    SESSIONS = mock(SessionDB.class);
    when(SESSIONS.getSession(SESSION_ID)).thenReturn(session);
    Controller.setSessionDB(SESSIONS);
  }

  @ParameterizedTest
  @CsvSource("2023nyrr")
  void testLogin(String eventKey) {
    TeamConfig config = new TeamConfig(1559);
    config.setEventKey(eventKey);
    TeamConfigDB teams = mock(TeamConfigDB.class);
    when(teams.get(1559)).thenReturn(config);
    Controller.setTeamDB(teams);

    Controller.setEventInfoCache(new EventInfoCache());

    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(SESSION_ID);
    EventInfoController.eventInfo(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(ctx).json(any(EventInfo.class));
  }
}
