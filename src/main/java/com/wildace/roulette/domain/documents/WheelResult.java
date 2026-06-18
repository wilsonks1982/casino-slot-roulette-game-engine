package com.wildace.roulette.domain.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wheel_results")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WheelResult {

    @Id
    private String id;
    private String roundId;
    private String tableId;
    private Integer number;
    private String color;
    private String parity;
    private Integer dozen;
    private Integer column;
}