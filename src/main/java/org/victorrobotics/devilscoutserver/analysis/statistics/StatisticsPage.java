package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.List;

public record StatisticsPage(String title,
                             List<Statistic> statistics) {}
