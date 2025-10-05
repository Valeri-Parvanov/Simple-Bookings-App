package app.web.controller;

import app.web.dto.RoomCreateRequest;
import app.web.dto.RoomDetailsResponse;
import app.web.dto.RoomUpdateRequest;
import app.room.service.RoomService;
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
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ModelAndView getAllRooms() {
        ModelAndView modelAndView = new ModelAndView("room/list");
        modelAndView.addObject("rooms", roomService.getVisibleRooms());
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getRoomDetails(@PathVariable UUID id) {
        RoomDetailsResponse room = roomService.getRoomDetails(id);
        ModelAndView modelAndView = new ModelAndView("room/details");
        modelAndView.addObject("room", room);
        return modelAndView;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("room/create");
        modelAndView.addObject("createRequest", new RoomCreateRequest());
        return modelAndView;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ModelAndView createRoom(@Valid @ModelAttribute RoomCreateRequest createRequest,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("room/create");
            modelAndView.addObject("createRequest", createRequest);
            return modelAndView;
        }

        roomService.createRoom(createRequest);
        redirectAttributes.addFlashAttribute("success", "Room created successfully");
        return new ModelAndView("redirect:/rooms");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public ModelAndView showEditForm(@PathVariable UUID id) {
        RoomDetailsResponse room = roomService.getRoomDetails(id);
        RoomUpdateRequest updateRequest = new RoomUpdateRequest();
        updateRequest.setName(room.getName());
        updateRequest.setLocation(room.getLocation());
        updateRequest.setCapacity(room.getCapacity());
        updateRequest.setBasePricePerHour(room.getBasePricePerHour());
        updateRequest.setDescription(room.getDescription());

        ModelAndView modelAndView = new ModelAndView("room/edit");
        modelAndView.addObject("updateRequest", updateRequest);
        modelAndView.addObject("roomId", id);
        return modelAndView;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public ModelAndView updateRoom(@PathVariable UUID id,
                                   @Valid @ModelAttribute RoomUpdateRequest updateRequest,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("room/edit");
            modelAndView.addObject("updateRequest", updateRequest);
            modelAndView.addObject("roomId", id);
            return modelAndView;
        }

        roomService.updateRoom(id, updateRequest);
        redirectAttributes.addFlashAttribute("success", "Room updated successfully");
        return new ModelAndView("redirect:/rooms/" + id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/toggle-visibility")
    public ModelAndView toggleVisibility(@PathVariable UUID id,
                                         RedirectAttributes redirectAttributes) {
        roomService.toggleVisibility(id);
        redirectAttributes.addFlashAttribute("success", "Room visibility updated");
        return new ModelAndView("redirect:/rooms/" + id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ModelAndView getAllRoomsAdmin() {
        ModelAndView modelAndView = new ModelAndView("room/admin-list");
        modelAndView.addObject("rooms", roomService.getAllRooms());
        return modelAndView;
    }
}

