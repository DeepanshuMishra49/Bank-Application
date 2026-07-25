package com.banking.controller.web;

import com.banking.dto.request.ChangePasswordRequest;
import com.banking.dto.request.DepositWithdrawRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.request.UpdateProfileRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.CustomerResponse;
import com.banking.dto.response.DashboardResponse;
import com.banking.dto.response.TransactionResponse;
import com.banking.security.CustomUserDetails;
import com.banking.service.*;
import com.banking.util.BankingConstants;
import com.banking.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for all customer-facing banking portal pages.
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        DashboardResponse dashboard = dashboardService.getCustomerDashboard(customer.id());
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customer.id());

        model.addAttribute("customer", customer);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "My Dashboard");
        return "customer/dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        model.addAttribute("customer", customer);
        model.addAttribute("updateRequest", new UpdateProfileRequest(
                customer.firstName(), customer.lastName(), customer.phone(),
                customer.gender(), customer.occupation(), customer.annualIncome(),
                customer.street(), customer.city(), customer.state(), customer.pinCode()));
        model.addAttribute("pageTitle", "My Profile");
        return "customer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
            model.addAttribute("customer", customer);
            return "customer/profile";
        }

        try {
            CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
            customerService.updateProfile(customer.id(), request);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS, "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/profile/picture")
    public String uploadProfilePicture(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("picture") MultipartFile picture,
            RedirectAttributes redirectAttributes) {

        try {
            String url = fileStorageService.storeFile(picture, "profiles");
            userService.findById(userDetails.getUserId()).setProfilePictureUrl(url);
            // Save the user entity
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS, "Profile picture updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("changePasswordRequest",
                new ChangePasswordRequest("", "", ""));
        model.addAttribute("pageTitle", "Change Password");
        return "customer/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "customer/change-password";
        }

        try {
            userService.changePassword(userDetails.getUserId(), request);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Password changed successfully! Please log in again.");
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
            return "redirect:/customer/change-password";
        }
    }

    @GetMapping("/account")
    public String accountPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customer.id());
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "My Accounts");
        return "customer/account";
    }

    @GetMapping("/transfer")
    public String transferPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customer.id());
        model.addAttribute("transferRequest", new TransferRequest("", "", null, ""));
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "Fund Transfer");
        return "customer/transfer";
    }

    @PostMapping("/transfer")
    public String processTransfer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("transferRequest") TransferRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
            model.addAttribute("accounts", accountService.getAccountsByCustomerId(customer.id()));
            return "customer/transfer";
        }

        try {
            String ip = SecurityUtils.getClientIpAddress(httpRequest);
            TransactionResponse txn = accountService.transfer(request, ip);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Transfer successful! Reference: " + txn.referenceNumber());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/customer/transfer";
    }

    @GetMapping("/deposit")
    public String depositPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        model.addAttribute("accounts", accountService.getAccountsByCustomerId(customer.id()));
        model.addAttribute("depositRequest", new DepositWithdrawRequest("", null, ""));
        model.addAttribute("pageTitle", "Deposit Money");
        return "customer/deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(
            @Valid @ModelAttribute("depositRequest") DepositWithdrawRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, "Please correct the errors.");
            return "redirect:/customer/deposit";
        }

        try {
            String ip = SecurityUtils.getClientIpAddress(httpRequest);
            TransactionResponse txn = accountService.deposit(request, ip);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Deposit of ₹" + request.amount() + " successful! Ref: " + txn.referenceNumber());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/customer/deposit";
    }

    @GetMapping("/withdraw")
    public String withdrawPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        model.addAttribute("accounts", accountService.getAccountsByCustomerId(customer.id()));
        model.addAttribute("withdrawRequest", new DepositWithdrawRequest("", null, ""));
        model.addAttribute("pageTitle", "Withdraw Money");
        return "customer/withdraw";
    }

    @PostMapping("/withdraw")
    public String processWithdraw(
            @Valid @ModelAttribute("withdrawRequest") DepositWithdrawRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, "Please correct the errors.");
            return "redirect:/customer/withdraw";
        }

        try {
            String ip = SecurityUtils.getClientIpAddress(httpRequest);
            TransactionResponse txn = accountService.withdraw(request, ip);
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_SUCCESS,
                    "Withdrawal of ₹" + request.amount() + " successful! Ref: " + txn.referenceNumber());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(BankingConstants.FLASH_ERROR, e.getMessage());
        }
        return "redirect:/customer/withdraw";
    }

    @GetMapping("/history")
    public String transactionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customer.id());

        if (!accounts.isEmpty()) {
            AccountResponse selectedAccount = accounts.stream()
                    .filter(a -> accountNumber == null || a.accountNumber().equals(accountNumber))
                    .findFirst()
                    .orElse(accounts.get(0));

            Page<TransactionResponse> transactions = transactionService.getTransactionHistory(
                    selectedAccount.id(),
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));

            model.addAttribute("transactions", transactions);
            model.addAttribute("selectedAccount", selectedAccount);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Transaction History");
        return "customer/history";
    }

    @GetMapping("/mini-statement")
    public String miniStatement(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CustomerResponse customer = customerService.findByUserId(userDetails.getUserId());
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customer.id());

        if (!accounts.isEmpty()) {
            List<TransactionResponse> mini = transactionService.getMiniStatement(accounts.get(0).id());
            model.addAttribute("transactions", mini);
            model.addAttribute("selectedAccount", accounts.get(0));
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Mini Statement");
        return "customer/mini-statement";
    }
}
