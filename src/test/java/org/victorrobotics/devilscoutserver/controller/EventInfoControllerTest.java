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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EventInfoControllerTest {
  @ParameterizedTest
  @CsvSource("2023nyrr")
  void testLogin(String eventKey) {
    String sessionID = "qhdgxgd";
    Session session = new Session(sessionID, 5, 1559, UserAccessLevel.USER);
    SessionDB sessions = mock(SessionDB.class);
    when(sessions.getSession(sessionID)).thenReturn(session);
    Controller.setSessionDB(sessions);

    TeamConfig config = new TeamConfig(1559);
    config.setEventKey(eventKey);
    TeamConfigDB teams = mock(TeamConfigDB.class);
    when(teams.get(1559)).thenReturn(config);
    Controller.setTeamDB(teams);

    Controller.setEventInfoCache(new EventInfoCache());

    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(sessionID);
    EventInfoController.eventInfo(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(sessions).getSession(sessionID);
    verify(ctx).json(any(EventInfo.class));
  }
}
