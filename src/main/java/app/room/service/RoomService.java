package app.room.service;

import app.web.dto.RoomCreateRequest;
import app.web.dto.RoomDetailsResponse;
import app.web.dto.RoomUpdateRequest;
import app.room.model.Room;
import app.room.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Room createRoom(RoomCreateRequest createRequest) {
        logger.info("Creating room with name: {}", createRequest.getName());

        if (roomRepository.existsByName(createRequest.getName())) {
            logger.warn("Room creation failed: name {} already exists", createRequest.getName());
            throw new IllegalArgumentException("Room with this name already exists");
        }

        Room room = Room.builder()
                .name(createRequest.getName())
                .location(createRequest.getLocation())
                .capacity(createRequest.getCapacity())
                .basePricePerHour(createRequest.getBasePricePerHour())
                .description(createRequest.getDescription())
                .visible(true)
                .createdAt(LocalDateTime.now())
                .build();

        Room savedRoom = roomRepository.save(room);
        logger.info("Room created successfully with ID: {}", savedRoom.getId());
        return savedRoom;
    }

    @Transactional(readOnly = true)
    public Room findById(UUID id) {
        logger.debug("Finding room by ID: {}", id);
        return roomRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Room not found with ID: {}", id);
                    return new IllegalArgumentException("Room not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public RoomDetailsResponse getRoomDetails(UUID id) {
        logger.debug("Getting room details for ID: {}", id);
        Room room = findById(id);
        return RoomDetailsResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .location(room.getLocation())
                .capacity(room.getCapacity())
                .basePricePerHour(room.getBasePricePerHour())
                .description(room.getDescription())
                .visible(room.isVisible())
                .createdAt(room.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RoomDetailsResponse> getAllRooms() {
        logger.debug("Getting all rooms");
        return roomRepository.findAll().stream()
                .map(room -> RoomDetailsResponse.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .location(room.getLocation())
                        .capacity(room.getCapacity())
                        .basePricePerHour(room.getBasePricePerHour())
                        .description(room.getDescription())
                        .visible(room.isVisible())
                        .createdAt(room.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomDetailsResponse> getVisibleRooms() {
        logger.debug("Getting visible rooms");
        return roomRepository.findAllByVisibleTrueOrderByNameAsc().stream()
                .map(room -> RoomDetailsResponse.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .location(room.getLocation())
                        .capacity(room.getCapacity())
                        .basePricePerHour(room.getBasePricePerHour())
                        .description(room.getDescription())
                        .visible(room.isVisible())
                        .createdAt(room.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public Room updateRoom(UUID id, RoomUpdateRequest updateRequest) {
        logger.info("Updating room with ID: {}", id);
        Room room = findById(id);

        if (!room.getName().equals(updateRequest.getName()) &&
                roomRepository.existsByName(updateRequest.getName())) {
            logger.warn("Update failed: room name {} already exists", updateRequest.getName());
            throw new IllegalArgumentException("Room with this name already exists");
        }

        room.setName(updateRequest.getName());
        room.setLocation(updateRequest.getLocation());
        room.setCapacity(updateRequest.getCapacity());
        room.setBasePricePerHour(updateRequest.getBasePricePerHour());
        room.setDescription(updateRequest.getDescription());

        Room updatedRoom = roomRepository.save(room);
        logger.info("Room updated successfully with ID: {}", id);
        return updatedRoom;
    }

    @Transactional
    public void toggleVisibility(UUID id) {
        logger.info("Toggling visibility for room ID: {}", id);
        Room room = findById(id);
        room.setVisible(!room.isVisible());
        roomRepository.save(room);
        logger.info("Room visibility toggled successfully for ID: {}. New visibility: {}", id, room.isVisible());
    }

    @Transactional
    public void deleteRoom(UUID id) {
        logger.info("Deleting room with ID: {}", id);
        Room room = findById(id);
        roomRepository.delete(room);
        logger.info("Room deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roomRepository.existsByName(name);
    }
}
