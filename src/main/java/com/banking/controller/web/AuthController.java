package com.banking.controller.web;

import com.banking.dto.request.RegisterRequest;
import com.banking.service.UserService;
import com.banking.util.BankingConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling the authentication pages: login, register.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * Displays the login page.
     *
     * @param error   present if login failed
     * @param logout  present if user just logged out
     * @param locked  present if account is locked
     * @param expired present if session expired
     * @param model   the Spring MVC model
     * @return the login view name
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String locked,
            @RequestParam(required = false) String disabled,
            @RequestParam(required = false) String expired,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. You may be locked after too many attempts.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (locked != null) {
            model.addAttribute("errorMessage",
                    "Your account is locked due to " + BankingConstants.MAX_FAILED_ATTEMPTS +
                    " failed login attempts. Please wait " + BankingConstants.LOCK_DURATION_MINUTES + " minutes.");
        }
        if (disabled != null) {
            model.addAttribute("errorMessage", "Your account has been disabled. Please contact support.");
        }
        if (expired != null) {
            model.addAttribute("warningMessage", "Your session has expired. Please log in again.");
        }
        return "auth/login";
    }

    /**
     * Displays the customer self-registration form.
     *
     * @param model the Spring MVC model
     * @return the register view name
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest("", "", "", "", "", ""));
        return "auth/register";
    }

    /**
     * Processes the customer self-registration form submission.
     *
     * @param request            the validated registration data
     * @param bindingResult      validation errors
     * @param redirectAttributes for flash messages on redirect
     * @return redirect to login on success, or back to register on error
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerCustomer(request);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Registration successful! Please wait for account approval before you can log in.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
            return "redirect:/register";
        }
    }
}
