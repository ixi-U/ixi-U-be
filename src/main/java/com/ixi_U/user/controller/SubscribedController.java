package com.ixi_U.user.controller;

import com.ixi_U.user.dto.request.CreateSubscribedRequest;
import com.ixi_U.user.service.SubscribedService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscribed")
public class SubscribedController {

    private final SubscribedService subscribedService;

    public SubscribedController(SubscribedService subscribedService) {
        this.subscribedService = subscribedService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Void> updateSubscribed(
            @PathVariable("userId") String userId,
            @RequestBody @Valid CreateSubscribedRequest request) {
        subscribedService.updateSubscribed(userId, request);

        return ResponseEntity.ok().build();
    }
}
