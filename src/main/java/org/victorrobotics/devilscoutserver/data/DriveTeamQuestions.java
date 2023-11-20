package org.victorrobotics.devilscoutserver.data;

import java.util.List;

import io.javalin.openapi.OpenApiRequired;

public record DriveTeamQuestions(@OpenApiRequired List<Question> questions) {}
