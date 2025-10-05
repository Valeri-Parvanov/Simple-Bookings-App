package app.booking.service;

import app.web.dto.BookingCreateRequest;
import app.web.dto.BookingDetailsResponse;
import app.web.dto.BookingUpdateRequest;
import app.booking.model.Booking;
import app.booking.model.BookingStatus;
import app.booking.repository.BookingRepository;
import app.promocode.model.PromoCode;
import app.promocode.service.PromoCodeService;
import app.room.model.Room;
import app.room.service.RoomService;
import app.user.model.User;
import app.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final PromoCodeService promoCodeService;

    public BookingService(
            BookingRepository bookingRepository,
            UserService userService,
            RoomService roomService,
            PromoCodeService promoCodeService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.promoCodeService = promoCodeService;
    }

    @Transactional
    public Booking createBooking(UUID userId, BookingCreateRequest createRequest) {
        logger.info("Creating booking for user ID: {} and room ID: {}", userId, createRequest.getRoomId());

        User user = userService.findById(userId);
        Room room = roomService.findById(createRequest.getRoomId());

        if (!room.isVisible()) {
            logger.warn("Booking failed: room {} is not visible", createRequest.getRoomId());
            throw new IllegalStateException("Room is not available for booking");
        }

        if (createRequest.getStartAt().isAfter(createRequest.getEndAt()) ||
                createRequest.getStartAt().isEqual(createRequest.getEndAt())) {
            logger.warn("Booking failed: invalid time range");
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (!isRoomAvailable(createRequest.getRoomId(), createRequest.getStartAt(), createRequest.getEndAt())) {
            logger.warn("Booking failed: room {} is already booked for this time period", createRequest.getRoomId());
            throw new IllegalStateException("Room is already booked for this time period");
        }

        PromoCode promoCode = null;
        if (createRequest.getPromoCode() != null && !createRequest.getPromoCode().isBlank()) {
            promoCode = promoCodeService.validateAndGetPromoCode(createRequest.getPromoCode());
        }

        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .promoCode(promoCode)
                .startAt(createRequest.getStartAt())
                .endAt(createRequest.getEndAt())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking created successfully with ID: {}", savedBooking.getId());
        return savedBooking;
    }

    @Transactional(readOnly = true)
    public Booking findById(UUID id) {
        logger.debug("Finding booking by ID: {}", id);
        return bookingRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Booking not found with ID: {}", id);
                    return new IllegalArgumentException("Booking not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public BookingDetailsResponse getBookingDetails(UUID id) {
        logger.debug("Getting booking details for ID: {}", id);
        Booking booking = findById(id);
        return buildDetailsResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> getAllBookingsByUserId(UUID userId) {
        logger.debug("Getting all bookings for user ID: {}", userId);
        return bookingRepository.findAllByUserId(userId).stream()
                .map(this::buildDetailsResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> getAllBookingsByRoomId(UUID roomId) {
        logger.debug("Getting all bookings for room ID: {}", roomId);
        return bookingRepository.findAllByRoomId(roomId).stream()
                .map(this::buildDetailsResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Booking updateBooking(UUID bookingId, UUID userId, BookingUpdateRequest updateRequest) {
        logger.info("Updating booking ID: {} for user ID: {}", bookingId, userId);
        Booking booking = findById(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            logger.warn("Update failed: user {} is not authorized to update booking {}", userId, bookingId);
            throw new SecurityException("You are not authorized to update this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            logger.warn("Update failed: booking {} is already canceled", bookingId);
            throw new IllegalArgumentException("Cannot update a canceled booking");
        }

        if (updateRequest.getStartAt().isAfter(updateRequest.getEndAt()) ||
                updateRequest.getStartAt().isEqual(updateRequest.getEndAt())) {
            logger.warn("Update failed: invalid time range");
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (!isRoomAvailable(booking.getRoom().getId(), updateRequest.getStartAt(), updateRequest.getEndAt(), bookingId)) {
            logger.warn("Update failed: room {} is already booked for this time period", booking.getRoom().getId());
            throw new IllegalStateException("Room is already booked for this time period");
        }

        booking.setStartAt(updateRequest.getStartAt());
        booking.setEndAt(updateRequest.getEndAt());

        Booking updatedBooking = bookingRepository.save(booking);
        logger.info("Booking updated successfully with ID: {}", bookingId);
        return updatedBooking;
    }

    @Transactional
    public void cancelBooking(UUID bookingId, UUID userId) {
        logger.info("Canceling booking ID: {} for user ID: {}", bookingId, userId);
        Booking booking = findById(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            logger.warn("Cancel failed: user {} is not authorized to cancel booking {}", userId, bookingId);
            throw new SecurityException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            logger.warn("Cancel failed: booking {} is already canceled", bookingId);
            throw new IllegalArgumentException("Booking is already canceled");
        }

        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
        logger.info("Booking canceled successfully with ID: {}", bookingId);
    }

    @Transactional(readOnly = true)
    public boolean isRoomAvailable(UUID roomId, LocalDateTime startAt, LocalDateTime endAt) {
        return !bookingRepository.existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(
                roomId, endAt, startAt);
    }

    private boolean isRoomAvailable(UUID roomId, LocalDateTime startAt, LocalDateTime endAt, UUID excludeBookingId) {
        List<Booking> conflictingBookings = bookingRepository.findAllByRoomId(roomId).stream()
                .filter(booking -> !booking.getId().equals(excludeBookingId))
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELED)
                .filter(booking -> booking.getStartAt().isBefore(endAt) && booking.getEndAt().isAfter(startAt))
                .collect(Collectors.toList());
        return conflictingBookings.isEmpty();
    }

    private BookingDetailsResponse buildDetailsResponse(Booking booking) {
        BigDecimal totalPrice = calculateTotalPrice(booking);
        BigDecimal discountAmount = calculateDiscountAmount(booking, totalPrice);

        return BookingDetailsResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .username(booking.getUser().getUsername())
                .roomId(booking.getRoom().getId())
                .roomName(booking.getRoom().getName())
                .roomLocation(booking.getRoom().getLocation())
                .startAt(booking.getStartAt())
                .endAt(booking.getEndAt())
                .status(booking.getStatus())
                .totalPrice(totalPrice)
                .discountAmount(discountAmount)
                .promoCode(booking.getPromoCode() != null ? booking.getPromoCode().getCode() : null)
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private BigDecimal calculateTotalPrice(Booking booking) {
        Duration duration = Duration.between(booking.getStartAt(), booking.getEndAt());
        long hours = duration.toHours();
        if (duration.toMinutes() % 60 > 0) {
            hours += 1;
        }
        return booking.getRoom().getBasePricePerHour().multiply(BigDecimal.valueOf(hours));
    }

    private BigDecimal calculateDiscountAmount(Booking booking, BigDecimal totalPrice) {
        if (booking.getPromoCode() != null && booking.getPromoCode().isActive()) {
            BigDecimal discountPercent = BigDecimal.valueOf(booking.getPromoCode().getPercent());
            return totalPrice.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
