package org.victorrobotics.devilscoutserver.data;

import java.util.List;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record MatchQuestions(@OpenApiRequired List<Question> auto,
                             @OpenApiRequired @OpenApiExample("[]") List<Question> teleop,
                             @OpenApiRequired @OpenApiExample("[]") List<Question> endgame,
                             @OpenApiRequired @OpenApiExample("[]") List<Question> general,
                             @OpenApiRequired @OpenApiExample("[]") List<Question> human) {}
