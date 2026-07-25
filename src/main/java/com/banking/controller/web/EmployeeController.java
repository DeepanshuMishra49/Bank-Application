package com.banking.controller.web;

import com.banking.dto.request.KycVerificationRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.CustomerResponse;
import com.banking.dto.response.DashboardResponse;
import com.banking.security.CustomUserDetails;
import com.banking.service.*;
import com.banking.util.BankingConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for all Employee-facing pages.
 * Requires ROLE_EMPLOYEE authority for all routes.
 */
@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final KycService kycService;
    private final DashboardService dashboardService;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        DashboardResponse dash = dashboardService.getEmployeeDashboard();
        Page<CustomerResponse> pendingCustomers = employeeService.getPendingCustomers(
                PageRequest.of(0, 5, Sort.by("createdAt").descending()));

        model.addAttribute("dashboard", dash);
        model.addAttribute("pendingCustomers", pendingCustomers);
        model.addAttribute("pageTitle", "Employee Dashboard");
        return "employee/dashboard";
    }

    // ─── Customers ────────────────────────────────────────────────────────────

    @GetMapping("/customers")
    public String customers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<CustomerResponse> customers = search.isBlank()
                ? customerService.getAllCustomers(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                : customerService.searchCustomers(search, PageRequest.of(page, size));

        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Customer Management");
        return "employee/customers";
    }

    @PostMapping("/customers/{customerId}/approve")
    public String approveCustomer(
            @PathVariable String customerId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            customerService.approveCustomer(customerId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Customer " + customerId + " approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/employee/customers";
    }

    // ─── Accounts ─────────────────────────────────────────────────────────────

    @GetMapping("/accounts")
    public String accounts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<AccountResponse> accounts = search.isBlank()
                ? accountService.getAllAccounts(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                : accountService.searchAccounts(search, PageRequest.of(page, size));

        model.addAttribute("accounts", accounts);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Account Management");
        return "employee/accounts";
    }

    @PostMapping("/accounts/{accountNumber}/freeze")
    public String freezeAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "Employee freeze") String reason,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.freezeAccount(accountNumber, reason);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Account " + accountNumber + " frozen.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/employee/accounts";
    }

    @PostMapping("/accounts/{accountNumber}/activate")
    public String activateAccount(
            @PathVariable String accountNumber,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.activateAccount(accountNumber);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Account " + accountNumber + " activated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/employee/accounts";
    }

    // ─── KYC ──────────────────────────────────────────────────────────────────

    @GetMapping("/kyc")
    public String kycQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<CustomerResponse> pending = employeeService.getPendingCustomers(
                PageRequest.of(page, size, Sort.by("createdAt")));

        model.addAttribute("pendingCustomers", pending);
        model.addAttribute("pageTitle", "KYC Verification");
        return "employee/kyc";
    }

    @PostMapping("/kyc/{customerId}/verify")
    public String verifyKyc(
            @PathVariable String customerId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            kycService.verifyKyc(customerId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "KYC verified for customer " + customerId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/employee/kyc";
    }

    @PostMapping("/kyc/{customerId}/reject")
    public String rejectKyc(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "Documents insufficient") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            kycService.rejectKyc(customerId, reason, userDetails.getUsername());
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "KYC rejected for customer " + customerId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/employee/kyc";
    }
}
