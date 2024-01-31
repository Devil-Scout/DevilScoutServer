package org.victorrobotics.devilscoutserver.questions;

import java.util.List;

public record QuestionPage(String key,
                           String title,
                           List<Question> questions) {}
