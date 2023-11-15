package org.victorrobotics.devilscoutserver.data;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record AppConfig(@OpenApiRequired @OpenApiExample("1559") int team,
                        @OpenApiRequired @OpenApiExample("2023nyrr") String eventKey) {

}
