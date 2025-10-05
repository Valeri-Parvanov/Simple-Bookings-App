package app.web.controller;

import app.web.dto.UserProfileResponse;
import app.web.dto.UserUpdateRequest;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ModelAndView profile(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        UserProfileResponse profile = userService.getProfile(user.getId());
        
        ModelAndView modelAndView = new ModelAndView("user/profile");
        modelAndView.addObject("profile", profile);
        
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername(profile.getUsername());
        updateRequest.setEmail(profile.getEmail());
        modelAndView.addObject("updateRequest", updateRequest);
        
        return modelAndView;
    }

    @PostMapping("/profile")
    public ModelAndView updateProfile(@Valid @ModelAttribute UserUpdateRequest updateRequest,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            User user = userService.findByUsername(principal.getName());
            UserProfileResponse profile = userService.getProfile(user.getId());
            ModelAndView modelAndView = new ModelAndView("user/profile");
            modelAndView.addObject("profile", profile);
            modelAndView.addObject("updateRequest", updateRequest);
            return modelAndView;
        }

        User user = userService.findByUsername(principal.getName());
        userService.updateProfile(user.getId(), updateRequest);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        return new ModelAndView("redirect:/users/profile");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ModelAndView getUserDetails(@PathVariable UUID id) {
        UserProfileResponse profile = userService.getProfile(id);
        ModelAndView modelAndView = new ModelAndView("user/details");
        modelAndView.addObject("profile", profile);
        return modelAndView;
    }
}

