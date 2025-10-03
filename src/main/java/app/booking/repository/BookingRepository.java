package app.booking.repository;

import app.booking.model.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByUserId(UUID userId);
    List<Booking> findAllByRoomId(UUID roomId);
    boolean existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(UUID roomId, LocalDateTime end, LocalDateTime start);
}
