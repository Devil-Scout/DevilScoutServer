package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.EventInfo;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.TeamConfig;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public final class EventInfoController extends Controller {
  private EventInfoController() {}

  @OpenApi(path = "/event_info", methods = HttpMethod.GET, tags = "Configuration",
           description = "Get information about the current event.",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = EventInfo.class)),
                         @OpenApiResponse(status = "401") })
  public static void eventInfo(Context ctx) {
    Session session = getValidSession(ctx);
    TeamConfig config = teamDB().get(session.getTeam());
    EventInfo eventInfo = eventInfoCache().get(config.getEventKey());

    synchronized (eventInfo) {
      String etag = Long.toString(eventInfo.getTimestamp());
      checkIfNoneMatch(ctx, etag);

      ctx.json(eventInfo);
      setResponseETag(ctx, etag);
    }
  }
}
