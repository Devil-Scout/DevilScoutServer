package org.victorrobotics.devilscoutserver.data;

import java.util.Map;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record Question(@OpenApiRequired @OpenApiExample("Drivetrain Type") String prompt,
                       @OpenApiRequired QuestionType type,
                       @OpenApiExample("{}") Map<String, Object> config) {}
