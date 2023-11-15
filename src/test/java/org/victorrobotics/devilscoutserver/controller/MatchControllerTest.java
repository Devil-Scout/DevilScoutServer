package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.data.MatchSchedule;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.TeamConfig;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.TeamConfigDB;

import io.javalin.http.Context;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MatchControllerTest {
  @ParameterizedTest
  @CsvSource("2023nyrr")
  void testMatches(String eventKey) {
    String sessionID = "vcOVI8k869c=";
    Session session = new Session(sessionID, 5, 1559, UserAccessLevel.USER);
    SessionDB sessions = mock(SessionDB.class);
    when(sessions.getSession(sessionID)).thenReturn(session);
    Controller.setSessionDB(sessions);

    TeamConfig config = new TeamConfig(1559, "Devil Tech");
    config.setEventKey(eventKey);
    TeamConfigDB teams = mock(TeamConfigDB.class);
    when(teams.get(1559)).thenReturn(config);
    Controller.setTeamDB(teams);

    Controller.setMatchSchedules(new MatchScheduleCache());

    Context ctx = mock(Context.class);
    when(ctx.header(Controller.SESSION_HEADER)).thenReturn(sessionID);

    MatchController.matches(ctx);

    verify(ctx).header(Controller.SESSION_HEADER);
    verify(sessions).getSession(sessionID);
    verify(teams).get(1559);
    verify(ctx).json(argThat((MatchSchedule m) -> !m.getMatches()
                                                    .isEmpty()));
  }
}
