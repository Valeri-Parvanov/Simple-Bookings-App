package app.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailsResponse {

    private UUID id;
    private String name;
    private String location;
    private Integer capacity;
    private BigDecimal basePricePerHour;
    private String description;
    private Boolean visible;
    private LocalDateTime createdAt;
}

