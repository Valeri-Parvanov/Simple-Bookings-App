package app.web.dto;

import app.booking.model.BookingStatus;
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
public class BookingDetailsResponse {

    private UUID id;
    private UUID userId;
    private String username;
    private UUID roomId;
    private String roomName;
    private String roomLocation;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private String promoCode;
    private LocalDateTime createdAt;
}

