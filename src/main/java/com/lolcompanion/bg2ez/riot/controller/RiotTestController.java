package com.lolcompanion.bg2ez.riot.controller;

import com.lolcompanion.bg2ez.riot.client.RiotApiClient;
import com.lolcompanion.bg2ez.riot.model.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class RiotTestController {

    private final RiotApiClient riotApiClient;

    @RequestMapping("/account/{gameName}/{tagLine}")
    public AccountDto getAccount(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        return riotApiClient.getAccountByRiotId(gameName, tagLine);
    }
}
