package app.room.repository;

import app.room.model.Room;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    boolean existsByName(String name);
    List<Room> findAllByVisibleTrueOrderByNameAsc();
}
