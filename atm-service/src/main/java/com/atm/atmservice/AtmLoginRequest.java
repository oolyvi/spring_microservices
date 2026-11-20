package com.atm.atmservice;

public record AtmLoginRequest(
        String cardNumber,
        String pin
) {
}