package app.config;

import app.room.model.Room;
import app.room.repository.RoomRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private record RoomData(String name, String location, int capacity, BigDecimal price, String description) {}

    @Bean
    public CommandLineRunner initializeData(UserRepository userRepository,
                                             RoomRepository roomRepository,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                logger.info("Initializing default admin user...");
                User admin = User.builder()
                        .username("admin")
                        .email("admin@simplebookings.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                logger.info("Default admin user created: username=admin, password=admin123");
            }

            if (roomRepository.count() == 0) {
                logger.info("Initializing sample rooms...");
                
                List<RoomData> roomDataList = Arrays.asList(
                    new RoomData("Conference Hall", "First Floor - Building A", 24,
                            new BigDecimal("80.00"),
                            "A modern and fully equipped conference hall featuring a large rectangular meeting table surrounded by comfortable office chairs. The room includes a mounted flat-screen display, whiteboard, and elegant wooden paneling. Large windows provide ample natural light, making it perfect for professional gatherings, presentations, and business meetings."),
                    new RoomData("Luxury Suite", "Top Floor - Ocean View Wing", 4,
                            new BigDecimal("150.00"),
                            "An elegant and spacious luxury suite featuring a plush king-size bed with a padded headboard and premium linens. The room includes a separate lounge area with comfortable sofa and armchair, perfect for relaxation. Warm ambient lighting from bedside lamps and elegant decor create a sophisticated atmosphere ideal for extended stays."),
                    new RoomData("Deluxe Single Room", "Second Floor - Standard Wing", 2,
                            new BigDecimal("60.00"),
                            "A cozy and functional single room perfect for solo travelers or business guests. Features a comfortable single bed with a modern headboard, a dedicated work area with desk and chair, and a wall-mounted television. The room includes warm wall-mounted lighting and a large window with natural light, creating an inviting space for both rest and work."),
                    new RoomData("Twin Room", "Second Floor - Standard Wing", 3,
                            new BigDecimal("70.00"),
                            "A comfortable twin room featuring two single beds with crisp white linens and modern headboards. The room includes a shared nightstand with a lamp and telephone between the beds, creating a cozy atmosphere. Tasteful botanical wall art adds elegance to the warm, inviting ambiance, making it perfect for friends or colleagues traveling together."),
                    new RoomData("Ocean View Suite", "Top Floor - Premium Wing", 4,
                            new BigDecimal("180.00"),
                            "Experience breathtaking panoramic ocean views from this bright and spacious suite. Features a large, comfortable king-size bed with elegant furnishings, bedside lamps, and a private balcony with sliding glass doors. The balcony overlooks pristine beaches and crystal-clear waters, while a cozy armchair near the window offers the perfect spot to enjoy the view. Ideal for a romantic getaway or peaceful retreat.")
                );

                List<Room> rooms = roomDataList.stream()
                        .map(data -> createRoom(data.name(), data.location(), data.capacity(), data.price(), data.description()))
                        .collect(Collectors.toList());
                
                roomRepository.saveAll(rooms);
                logger.info("Sample rooms initialization completed. Created {} rooms.", rooms.size());
            }
        };
    }

    private Room createRoom(String name, String location, int capacity, BigDecimal price, String description) {
        return Room.builder()
                .name(name)
                .location(location)
                .capacity(capacity)
                .basePricePerHour(price)
                .description(description)
                .visible(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

