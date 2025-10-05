package app.web.controller;

import app.web.dto.BookingCreateRequest;
import app.web.dto.BookingDetailsResponse;
import app.web.dto.BookingUpdateRequest;
import app.booking.service.BookingService;
import app.room.service.RoomService;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;

    public BookingController(BookingService bookingService, RoomService roomService, UserService userService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getAllBookings(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        ModelAndView modelAndView = new ModelAndView("booking/list");
        modelAndView.addObject("bookings", bookingService.getAllBookingsByUserId(user.getId()));
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getBookingDetails(@PathVariable UUID id, Principal principal) {
        BookingDetailsResponse booking = bookingService.getBookingDetails(id);
        User user = userService.findByUsername(principal.getName());
        
        if (!booking.getUserId().equals(user.getId())) {
            return new ModelAndView("redirect:/bookings");
        }

        ModelAndView modelAndView = new ModelAndView("booking/details");
        modelAndView.addObject("booking", booking);
        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm(@RequestParam(required = false) UUID roomId) {
        ModelAndView modelAndView = new ModelAndView("booking/create");
        BookingCreateRequest createRequest = new BookingCreateRequest();
        if (roomId != null) {
            createRequest.setRoomId(roomId);
        }
        modelAndView.addObject("createRequest", createRequest);
        modelAndView.addObject("rooms", roomService.getVisibleRooms());
        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView createBooking(@Valid @ModelAttribute BookingCreateRequest createRequest,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("booking/create");
            modelAndView.addObject("createRequest", createRequest);
            modelAndView.addObject("rooms", roomService.getVisibleRooms());
            return modelAndView;
        }

        User user = userService.findByUsername(principal.getName());
        bookingService.createBooking(user.getId(), createRequest);
        redirectAttributes.addFlashAttribute("success", "Booking created successfully");
        return new ModelAndView("redirect:/bookings");
    }

    @GetMapping("/{id}/edit")
    public ModelAndView showEditForm(@PathVariable UUID id, Principal principal) {
        BookingDetailsResponse booking = bookingService.getBookingDetails(id);
        User user = userService.findByUsername(principal.getName());
        
        if (!booking.getUserId().equals(user.getId())) {
            return new ModelAndView("redirect:/bookings");
        }

        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStartAt(booking.getStartAt());
        updateRequest.setEndAt(booking.getEndAt());

        ModelAndView modelAndView = new ModelAndView("booking/edit");
        modelAndView.addObject("updateRequest", updateRequest);
        modelAndView.addObject("bookingId", id);
        return modelAndView;
    }

    @PostMapping("/{id}/edit")
    public ModelAndView updateBooking(@PathVariable UUID id,
                                      @Valid @ModelAttribute BookingUpdateRequest updateRequest,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("booking/edit");
            modelAndView.addObject("updateRequest", updateRequest);
            modelAndView.addObject("bookingId", id);
            return modelAndView;
        }

        User user = userService.findByUsername(principal.getName());
        bookingService.updateBooking(id, user.getId(), updateRequest);
        redirectAttributes.addFlashAttribute("success", "Booking updated successfully");
        return new ModelAndView("redirect:/bookings/" + id);
    }

    @PostMapping("/{id}/cancel")
    public ModelAndView cancelBooking(@PathVariable UUID id,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        bookingService.cancelBooking(id, user.getId());
        redirectAttributes.addFlashAttribute("success", "Booking canceled successfully");
        return new ModelAndView("redirect:/bookings");
    }
}

