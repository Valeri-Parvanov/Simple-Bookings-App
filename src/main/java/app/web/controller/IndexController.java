package app.web.controller;

import app.web.dto.UserRegisterRequest;
import app.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("auth/login");
    }

    @GetMapping("/register")
    public ModelAndView register() {
        ModelAndView modelAndView = new ModelAndView("auth/register");
        modelAndView.addObject("registerRequest", new UserRegisterRequest());
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerPost(@Valid @ModelAttribute UserRegisterRequest registerRequest,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("auth/register");
            modelAndView.addObject("registerRequest", registerRequest);
            return modelAndView;
        }

        try {
            userService.register(registerRequest);
            logger.info("User registered successfully: {}", registerRequest.getUsername());
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return new ModelAndView("redirect:/login");
        } catch (Exception e) {
            logger.error("Registration failed", e);
            ModelAndView modelAndView = new ModelAndView("auth/register");
            modelAndView.addObject("registerRequest", registerRequest);
            modelAndView.addObject("error", e.getMessage());
            return modelAndView;
        }
    }
}

