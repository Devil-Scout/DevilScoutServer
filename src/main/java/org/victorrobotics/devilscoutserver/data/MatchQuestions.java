package org.victorrobotics.devilscoutserver.data;

import java.util.List;

import io.javalin.openapi.OpenApiRequired;

public record MatchQuestions(@OpenApiRequired List<Question> auto,
                             @OpenApiRequired List<Question> teleop,
                             @OpenApiRequired List<Question> endgame,
                             @OpenApiRequired List<Question> general,
                             @OpenApiRequired List<Question> human) {}
