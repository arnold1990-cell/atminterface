package com.atminterface.atm.controller;

import com.atminterface.atm.dto.AmountRequest;
import com.atminterface.atm.dto.BalanceResponse;
import com.atminterface.atm.dto.ChangePinRequest;
import com.atminterface.atm.dto.TransferRequest;
import com.atminterface.atm.service.AtmService;
import com.atminterface.common.api.ApiResponse;
import com.atminterface.common.security.AuthInterceptor;
import com.atminterface.common.security.SessionPrincipal;
import com.atminterface.transactions.dto.TransactionDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atm")
@Validated
public class AtmController {

    private final AtmService atmService;

    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    @GetMapping("/balance")
    public ApiResponse<BalanceResponse> balance(HttpServletRequest request) {
        return ApiResponse.success("Balance fetched successfully.", atmService.getBalance(principal(request)));
    }

    @PostMapping("/withdraw")
    public ApiResponse<BalanceResponse> withdraw(HttpServletRequest request, @Valid @RequestBody AmountRequest amountRequest) {
        return ApiResponse.success("Withdrawal successful.", atmService.withdraw(principal(request), amountRequest.amount()));
    }

    @PostMapping("/deposit")
    public ApiResponse<BalanceResponse> deposit(HttpServletRequest request, @Valid @RequestBody AmountRequest amountRequest) {
        return ApiResponse.success("Deposit successful.", atmService.deposit(principal(request), amountRequest.amount()));
    }

    @PostMapping("/transfer")
    public ApiResponse<BalanceResponse> transfer(HttpServletRequest request, @Valid @RequestBody TransferRequest transferRequest) {
        return ApiResponse.success("Transfer successful.", atmService.transfer(principal(request), transferRequest));
    }

    @GetMapping("/statement")
    public ApiResponse<List<TransactionDto>> statement(HttpServletRequest request,
                                                       @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return ApiResponse.success("Statement fetched successfully.", atmService.statement(principal(request), limit));
    }

    @PostMapping("/change-pin")
    public ApiResponse<Void> changePin(HttpServletRequest request, @Valid @RequestBody ChangePinRequest changePinRequest) {
        atmService.changePin(principal(request), changePinRequest);
        return ApiResponse.success("PIN changed successfully.", null);
    }

    private SessionPrincipal principal(HttpServletRequest request) {
        return (SessionPrincipal) request.getAttribute(AuthInterceptor.PRINCIPAL_ATTR);
    }
}
