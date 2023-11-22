package org.victorrobotics.devilscoutserver.data;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record UserInfo(@OpenApiRequired @OpenApiExample("6536270208735686") long id,
                       @OpenApiRequired @OpenApiExample("1559") int team,
                       @OpenApiRequired @OpenApiExample("xander") String username,
                       @OpenApiRequired @OpenApiExample("Xander Bhalla") String fullName,
                       @OpenApiRequired UserAccessLevel accessLevel) {}
