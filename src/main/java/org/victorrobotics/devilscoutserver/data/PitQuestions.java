package org.victorrobotics.devilscoutserver.data;

import java.util.List;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record PitQuestions(@OpenApiRequired List<Question> specs,
                           @OpenApiRequired @OpenApiExample("[]") List<Question> auto,
                           @OpenApiRequired @OpenApiExample("[]") List<Question> teleop,
                           @OpenApiRequired @OpenApiExample("[]") List<Question> endgame,
                           @OpenApiRequired @OpenApiExample("[]") List<Question> general) {}
