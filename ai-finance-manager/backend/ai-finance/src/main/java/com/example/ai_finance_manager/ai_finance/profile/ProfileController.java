package com.example.ai_finance_manager.ai_finance.profile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> get() {
        return ResponseEntity.ok(profileService.get());
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> update(@Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.update(request));
    }
}