package org.victorrobotics.devilscoutserver.data;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record ServerStatus(@OpenApiRequired @OpenApiExample("okay") String status) {}
