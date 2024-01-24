package org.victorrobotics.devilscoutserver.questions;

@SuppressWarnings("java:S6218") // consider array content
public record QuestionPage(String key,
                           String title,
                           Question[] questions) {}
