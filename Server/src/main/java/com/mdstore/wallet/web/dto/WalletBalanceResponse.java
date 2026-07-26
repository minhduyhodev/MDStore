package com.mdstore.wallet.web.dto;

import java.math.BigDecimal;

public record WalletBalanceResponse(
        BigDecimal balance,
        String currency
) {}
