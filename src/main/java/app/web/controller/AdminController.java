package app.web.controller;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ModelAndView getAllUsers() {
        ModelAndView modelAndView = new ModelAndView("admin/users");
        modelAndView.addObject("users", userService.getAllUsers());
        return modelAndView;
    }

    @GetMapping("/users/{id}")
    public ModelAndView getUserDetails(@PathVariable UUID id) {
        User user = userService.findById(id);
        ModelAndView modelAndView = new ModelAndView("admin/user-details");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/users/{id}/change-role")
    public ModelAndView changeUserRole(@PathVariable UUID id,
                                      @RequestParam UserRole newRole,
                                      RedirectAttributes redirectAttributes) {
        userService.changeUserRole(id, newRole);
        redirectAttributes.addFlashAttribute("success", "User role changed successfully");
        return new ModelAndView("redirect:/admin/users/" + id);
    }

    @PostMapping("/users/{id}/block")
    public ModelAndView blockUser(@PathVariable UUID id,
                                 RedirectAttributes redirectAttributes) {
        userService.blockUser(id);
        redirectAttributes.addFlashAttribute("success", "User blocked successfully");
        return new ModelAndView("redirect:/admin/users/" + id);
    }

    @PostMapping("/users/{id}/unblock")
    public ModelAndView unblockUser(@PathVariable UUID id,
                                    RedirectAttributes redirectAttributes) {
        userService.unblockUser(id);
        redirectAttributes.addFlashAttribute("success", "User unblocked successfully");
        return new ModelAndView("redirect:/admin/users/" + id);
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard() {
        return new ModelAndView("admin/dashboard");
    }
}

