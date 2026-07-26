package com.mdstore.wallet.web;

import com.mdstore.common.web.ApiResponse;
import com.mdstore.wallet.web.dto.WalletBalanceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @GetMapping
    public ApiResponse<WalletBalanceResponse> getBalance() {
        // TODO: Gọi WalletQueryService
        throw new UnsupportedOperationException("Chưa triển khai");
    }
}
