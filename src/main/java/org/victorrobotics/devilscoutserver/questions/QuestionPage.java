package org.victorrobotics.devilscoutserver.questions;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

@SuppressWarnings("java:S6218") // consider array content
public record QuestionPage(@OpenApiRequired @OpenApiExample("auto") String key,
                           @OpenApiRequired @OpenApiExample("Autonomous") String title,
                           @OpenApiRequired @OpenApiExample("[]") Question[] questions) {}
