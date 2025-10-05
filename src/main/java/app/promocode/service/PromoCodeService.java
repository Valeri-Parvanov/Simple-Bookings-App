package app.promocode.service;

import app.web.dto.PromoCodeCreateRequest;
import app.web.dto.PromoCodeUpdateRequest;
import app.promocode.model.PromoCode;
import app.promocode.repository.PromoCodeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoCodeService {

    private static final Logger logger = LoggerFactory.getLogger(PromoCodeService.class);
    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional
    public PromoCode createPromoCode(PromoCodeCreateRequest createRequest) {
        logger.info("Creating promo code with code: {}", createRequest.getCode());

        if (promoCodeRepository.existsByCode(createRequest.getCode())) {
            logger.warn("Promo code creation failed: code {} already exists", createRequest.getCode());
            throw new IllegalArgumentException("Promo code already exists");
        }

        if (createRequest.getValidFrom().isAfter(createRequest.getValidTo()) ||
                createRequest.getValidFrom().isEqual(createRequest.getValidTo())) {
            logger.warn("Promo code creation failed: invalid date range");
            throw new IllegalArgumentException("Valid from date must be before valid to date");
        }

        PromoCode promoCode = PromoCode.builder()
                .code(createRequest.getCode())
                .percent(createRequest.getPercent())
                .validFrom(createRequest.getValidFrom())
                .validTo(createRequest.getValidTo())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);
        logger.info("Promo code created successfully with ID: {}", savedPromoCode.getId());
        return savedPromoCode;
    }

    @Transactional(readOnly = true)
    public PromoCode findById(UUID id) {
        logger.debug("Finding promo code by ID: {}", id);
        return promoCodeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Promo code not found with ID: {}", id);
                    return new IllegalArgumentException("Promo code not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public PromoCode findByCode(String code) {
        logger.debug("Finding promo code by code: {}", code);
        return promoCodeRepository.findByCode(code)
                .orElseThrow(() -> {
                    logger.warn("Promo code not found with code: {}", code);
                    return new IllegalArgumentException("Promo code not found with code: " + code);
                });
    }

    @Transactional(readOnly = true)
    public PromoCode validateAndGetPromoCode(String code) {
        logger.debug("Validating promo code: {}", code);
        LocalDateTime now = LocalDateTime.now();

        PromoCode promoCode = promoCodeRepository
                .findByCodeAndActiveTrueAndValidFromBeforeAndValidToAfter(code, now, now)
                .orElseThrow(() -> {
                    logger.warn("Invalid or expired promo code: {}", code);
                    return new IllegalArgumentException("Promo code is invalid, inactive, or expired");
                });

        logger.info("Promo code validated successfully: {}", code);
        return promoCode;
    }

    @Transactional(readOnly = true)
    public List<PromoCode> getAllPromoCodes() {
        logger.debug("Getting all promo codes");
        return promoCodeRepository.findAll();
    }

    @Transactional
    public PromoCode updatePromoCode(UUID id, PromoCodeUpdateRequest updateRequest) {
        logger.info("Updating promo code with ID: {}", id);
        PromoCode promoCode = findById(id);

        if (updateRequest.getValidFrom().isAfter(updateRequest.getValidTo()) ||
                updateRequest.getValidFrom().isEqual(updateRequest.getValidTo())) {
            logger.warn("Update failed: invalid date range");
            throw new IllegalArgumentException("Valid from date must be before valid to date");
        }

        promoCode.setPercent(updateRequest.getPercent());
        promoCode.setValidFrom(updateRequest.getValidFrom());
        promoCode.setValidTo(updateRequest.getValidTo());

        PromoCode updatedPromoCode = promoCodeRepository.save(promoCode);
        logger.info("Promo code updated successfully with ID: {}", id);
        return updatedPromoCode;
    }

    @Transactional
    public void deactivatePromoCode(UUID id) {
        logger.info("Deactivating promo code with ID: {}", id);
        PromoCode promoCode = findById(id);
        promoCode.setActive(false);
        promoCodeRepository.save(promoCode);
        logger.info("Promo code deactivated successfully with ID: {}", id);
    }

    @Transactional
    public void activatePromoCode(UUID id) {
        logger.info("Activating promo code with ID: {}", id);
        PromoCode promoCode = findById(id);
        promoCode.setActive(true);
        promoCodeRepository.save(promoCode);
        logger.info("Promo code activated successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return promoCodeRepository.existsByCode(code);
    }
}
