package app.promocode.repository;
//
//import app.promocode.model.PromoCode;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface PromoCodeRepository extends JpaRepository<PromoCode, UUID> {
//    Optional<PromoCode> findByCodeAndActiveTrueAndValidFromBeforeAndValidToAfter(String code, LocalDateTime from, LocalDateTime to);
//    boolean existByCode(String code);
//}
