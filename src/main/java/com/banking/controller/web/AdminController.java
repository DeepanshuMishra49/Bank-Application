package com.banking.controller.web;

import com.banking.dto.request.CreateAccountRequest;
import com.banking.dto.request.CreateCustomerRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.CustomerResponse;
import com.banking.dto.response.DashboardResponse;
import com.banking.dto.response.KycDetailResponse;
import com.banking.dto.response.TransactionResponse;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for all Admin-facing pages.
 * Requires ROLE_ADMIN authority for all routes.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final EmployeeService employeeService;
    private final KycService kycService;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardResponse dash = dashboardService.getAdminDashboard();
        // Load small previews for dashboard quick-view panels
        Page<CustomerResponse> pendingApprovals = customerService.getPendingApprovals(PageRequest.of(0, 5));
        Page<KycDetailResponse> pendingKyc = kycService.getPendingKyc(PageRequest.of(0, 5));

        model.addAttribute("dashboard", dash);
        model.addAttribute("recentPendingApprovals", pendingApprovals.getContent());
        model.addAttribute("recentPendingKyc", pendingKyc.getContent());
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
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
        model.addAttribute("pageTitle", "Manage Customers");
        model.addAttribute("createRequest", new CreateCustomerRequest(
                "", "", "", "", "", null, null, null, null, null, null, null));
        return "admin/customers";
    }

    @PostMapping("/customers/create")
    public String createCustomer(
            @Valid @ModelAttribute("createRequest") CreateCustomerRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Page<CustomerResponse> customers = customerService.getAllCustomers(PageRequest.of(0, 15));
            model.addAttribute("customers", customers);
            model.addAttribute("pageTitle", "Manage Customers");
            return "admin/customers";
        }

        try {
            CustomerResponse customer = customerService.createCustomer(request);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Customer " + customer.fullName() + " created successfully! ID: " + customer.customerId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/{customerId}/approve")
    public String approveCustomer(
            @PathVariable String customerId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            customerService.approveCustomer(customerId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Customer " + customerId + " approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/{customerId}/deactivate")
    public String deactivateCustomer(
            @PathVariable String customerId,
            RedirectAttributes redirectAttributes) {

        try {
            customerService.deleteCustomer(customerId);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Customer " + customerId + " deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/customers/{customerId}")
    public String customerDetail(@PathVariable String customerId, Model model) {
        CustomerResponse customer = customerService.findByCustomerId(customerId);
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Customer Details — " + customer.fullName());
        return "admin/customer-detail";
    }

    // ─── Approvals (dedicated page) ───────────────────────────────────────────

    @GetMapping("/approvals")
    public String pendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<CustomerResponse> pending = customerService.getPendingApprovals(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("pendingCustomers", pending);
        model.addAttribute("pageTitle", "Pending Customer Approvals");
        return "admin/approvals";
    }

    @PostMapping("/approvals/{customerId}/approve")
    public String approveFromApprovals(
            @PathVariable String customerId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            customerService.approveCustomer(customerId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Customer " + customerId + " approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/approvals";
    }

    @PostMapping("/approvals/{customerId}/reject")
    public String rejectFromApprovals(
            @PathVariable String customerId,
            RedirectAttributes redirectAttributes) {

        try {
            customerService.deleteCustomer(customerId);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Customer " + customerId + " rejected and deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/approvals";
    }

    // ─── KYC Management ───────────────────────────────────────────────────────

    @GetMapping("/kyc")
    public String kycManagement(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<KycDetailResponse> kycEntries;
        if ("pending".equals(filter)) {
            kycEntries = kycService.getPendingKyc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            kycEntries = kycService.getAllKyc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }

        long pendingCount = kycService.getPendingKyc(PageRequest.of(0, 1)).getTotalElements();

        model.addAttribute("kycEntries", kycEntries);
        model.addAttribute("filter", filter);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("pageTitle", "KYC Management");
        return "admin/kyc";
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
        return "redirect:/admin/kyc";
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
        return "redirect:/admin/kyc";
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
        model.addAttribute("pageTitle", "Manage Accounts");
        return "admin/accounts";
    }

    @PostMapping("/accounts/{accountNumber}/freeze")
    public String freezeAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "Admin freeze") String reason,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.freezeAccount(accountNumber, reason);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Account " + accountNumber + " frozen.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/accounts";
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
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/open")
    public String openAccount(
            @Valid @ModelAttribute("createAccountRequest") CreateAccountRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, "Invalid account data.");
            return "redirect:/admin/accounts";
        }
        try {
            AccountResponse account = accountService.openAccount(request);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Account opened: " + account.accountNumber());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    // ─── Transactions ─────────────────────────────────────────────────────────

    @GetMapping("/transactions")
    public String transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Page<TransactionResponse> txns = transactionService.getAllTransactions(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("transactions", txns);
        model.addAttribute("pageTitle", "All Transactions");
        return "admin/transactions";
    }

    // ─── Employees ────────────────────────────────────────────────────────────

    @GetMapping("/employees")
    public String employees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        model.addAttribute("employees", employeeService.getAllEmployees(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
        model.addAttribute("pageTitle", "Manage Employees");
        return "admin/employees";
    }
}
