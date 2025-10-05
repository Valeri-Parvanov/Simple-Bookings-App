package app.web.controller;

import app.web.dto.PromoCodeCreateRequest;
import app.web.dto.PromoCodeUpdateRequest;
import app.promocode.model.PromoCode;
import app.promocode.service.PromoCodeService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/promocodes")
@PreAuthorize("hasRole('ADMIN')")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    @GetMapping
    public ModelAndView getAllPromoCodes() {
        ModelAndView modelAndView = new ModelAndView("promocode/list");
        modelAndView.addObject("promoCodes", promoCodeService.getAllPromoCodes());
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getPromoCodeDetails(@PathVariable UUID id) {
        PromoCode promoCode = promoCodeService.findById(id);
        ModelAndView modelAndView = new ModelAndView("promocode/details");
        modelAndView.addObject("promoCode", promoCode);
        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("promocode/create");
        modelAndView.addObject("createRequest", new PromoCodeCreateRequest());
        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView createPromoCode(@Valid @ModelAttribute PromoCodeCreateRequest createRequest,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("promocode/create");
            modelAndView.addObject("createRequest", createRequest);
            return modelAndView;
        }

        promoCodeService.createPromoCode(createRequest);
        redirectAttributes.addFlashAttribute("success", "Promo code created successfully");
        return new ModelAndView("redirect:/promocodes");
    }

    @GetMapping("/{id}/edit")
    public ModelAndView showEditForm(@PathVariable UUID id) {
        PromoCode promoCode = promoCodeService.findById(id);
        PromoCodeUpdateRequest updateRequest = new PromoCodeUpdateRequest();
        updateRequest.setPercent(promoCode.getPercent());
        updateRequest.setValidFrom(promoCode.getValidFrom());
        updateRequest.setValidTo(promoCode.getValidTo());

        ModelAndView modelAndView = new ModelAndView("promocode/edit");
        modelAndView.addObject("updateRequest", updateRequest);
        modelAndView.addObject("promoCodeId", id);
        return modelAndView;
    }

    @PostMapping("/{id}/edit")
    public ModelAndView updatePromoCode(@PathVariable UUID id,
                                       @Valid @ModelAttribute PromoCodeUpdateRequest updateRequest,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("promocode/edit");
            modelAndView.addObject("updateRequest", updateRequest);
            modelAndView.addObject("promoCodeId", id);
            return modelAndView;
        }

        promoCodeService.updatePromoCode(id, updateRequest);
        redirectAttributes.addFlashAttribute("success", "Promo code updated successfully");
        return new ModelAndView("redirect:/promocodes/" + id);
    }

    @PostMapping("/{id}/activate")
    public ModelAndView activatePromoCode(@PathVariable UUID id,
                                         RedirectAttributes redirectAttributes) {
        promoCodeService.activatePromoCode(id);
        redirectAttributes.addFlashAttribute("success", "Promo code activated");
        return new ModelAndView("redirect:/promocodes/" + id);
    }

    @PostMapping("/{id}/deactivate")
    public ModelAndView deactivatePromoCode(@PathVariable UUID id,
                                           RedirectAttributes redirectAttributes) {
        promoCodeService.deactivatePromoCode(id);
        redirectAttributes.addFlashAttribute("success", "Promo code deactivated");
        return new ModelAndView("redirect:/promocodes/" + id);
    }
}

