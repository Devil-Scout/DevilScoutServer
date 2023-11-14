package org.victorrobotics.devilscoutserver;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import org.victorrobotics.devilscoutserver.controller.Controller;
import org.victorrobotics.devilscoutserver.controller.SessionController;
import org.victorrobotics.devilscoutserver.controller.StatusController;
import org.victorrobotics.devilscoutserver.database.MockSessionDB;
import org.victorrobotics.devilscoutserver.database.MockUserDB;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.openapi.ApiKeyAuth;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.SecurityComponentConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

public class Main {
  private static final Handler UNIMPLEMENTED = ctx -> ctx.result("UNIMPLEMENTED");

  private static final String API_DESCRIPTION = """
      ## Overview
      Information and statistics on FRC competitions, pooled together by all registered teams.
      ## Authentication
      All endpoints (except for login) require a session key to be passed in the header
      `X-DS-SESSION-KEY`. This will be generated by the server upon successful authentication.
      """;
  private static final String TAGS_SORTER = """
      /* INJECTED */ (a,b) => {
        const tagOrder = ["Status", "Session"];
        return tagOrder.indexOf(a) - tagOrder.indexOf(b);
      }
      """;
  // Hack to inject directly instead of serving file
  private static final String REMOVE_TOP_BAR = """
      '></script><script>
        /* INJECTED */
        let onLoad = window.onload;
        window.onload = function() {
          onLoad();
          let topbar = document.getElementsByClassName('topbar')[0];
          topbar.parentNode.removeChild(topbar);
        };
      </script><script src='""";

  @SuppressWarnings("java:S2095") // close Javalin
  public static void main(String... args) {
    Controller.setUserDB(new MockUserDB());
    Controller.setSessionDB(new MockSessionDB());

    Javalin server = Javalin.create(config -> {
      config.http.prefer405over404 = true;

      // @format:off
      config.plugins.register(new OpenApiPlugin(
        new OpenApiPluginConfiguration()
          .withDocumentationPath("/openapi/json")
          .withDefinitionConfiguration((version, definition) -> definition
            .withOpenApiInfo(openApiInfo -> {
              openApiInfo.setTitle("DevilScout Server");
              openApiInfo.setVersion("alpha");
              openApiInfo.setDescription(API_DESCRIPTION);
            })
            .withSecurity(new SecurityComponentConfiguration()
              .withSecurityScheme("ApiKeyAuth", new ApiKeyAuth() {
                @Override
                public String getName() {
                  return "X-DS-SESSION-KEY";
                }
              })
            )
          )
        )
      );
      // @format:on

      SwaggerConfiguration uiConfig = new SwaggerConfiguration();
      uiConfig.setTitle("DevilScout Server API");
      uiConfig.setDocumentationPath("/openapi/json");
      uiConfig.setUiPath("/openapi/ui");
      uiConfig.setTagsSorter(TAGS_SORTER);
      uiConfig.injectJavaScript(REMOVE_TOP_BAR);
      config.plugins.register(new SwaggerPlugin(uiConfig));
    });

    server.routes(() -> {
      get("status", StatusController::status);
      path("sessions", () -> {
        delete(SessionController::logout);
        post("login", SessionController::login);
        post("auth", SessionController::auth);
      });
      path("matches", () -> {
        get(UNIMPLEMENTED); // get status of matches at current event
        post(UNIMPLEMENTED); // upload a match scouting record for current event
        path("post", () -> {
          get(UNIMPLEMENTED); // get status of post-match feedback (ADMIN)
          post(UNIMPLEMENTED); // upload post-match feedback (ADMIN)
        });
      });
      path("pits", () -> {
        get(UNIMPLEMENTED); // get status of pits at current event
        post(UNIMPLEMENTED); // upload a pit scouting record for current event
      });
      path("analysis", () -> {
        get("teams", UNIMPLEMENTED); // summaries by team at current event
        get("match", UNIMPLEMENTED); // request match simulation
        get("alliance", UNIMPLEMENTED); // request alliance optimization
      });
      path("teams", () -> {
        get(UNIMPLEMENTED); // get list of teams at current event
        path("registered", () -> {
          get(UNIMPLEMENTED); // get list of registered teams (SUDO)
          post(UNIMPLEMENTED); // register new team (SUDO)
          delete(UNIMPLEMENTED); // unregister team (SUDO)
          put(UNIMPLEMENTED); // update team info (ADMIN) or update any team's
                              // status/info (SUDO)
        });
      });
      path("users", () -> {
        get(UNIMPLEMENTED); // get list of team's users (ADMIN)
        post(UNIMPLEMENTED); // register a new user on team (ADMIN) OR on any
                             // team (SUDO)
        get("all", UNIMPLEMENTED); // get list of all users (SUDO)
      });
    });

    server.start(80);
  }
}
