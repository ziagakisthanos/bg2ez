package com.lolcompanion.bg2ez.account.controller;

import com.lolcompanion.bg2ez.account.converter.SummonerConverter;
import com.lolcompanion.bg2ez.account.model.SummonerModel;
import com.lolcompanion.bg2ez.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountService accountService;
    private final SummonerConverter summonerConverter;

    @GetMapping("/me")
    public ResponseEntity<SummonerModel> getMe() {
        return accountService.getLinkedAccount()
                .map(summonerConverter::entityToModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/account/link/{gameName}/{tagLine}")
    public SummonerModel linkAccount(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        return summonerConverter.entityToModel(accountService.linkAccount(gameName, tagLine));
    }

    @DeleteMapping("/account/unlink")
    public ResponseEntity<Void> unlinkAccount() {
        accountService.unlinkAccount();
        return ResponseEntity.ok().build();
    }
}
