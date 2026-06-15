package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${user.service.url}",
        fallback = UserServiceFeignClientFallback.class
)
public interface UserServiceFeignClient {
    @GetMapping("/api/users/email")
    UserResponseDto getUser(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String email );

    @GetMapping("/api/users/{id}")
    UserResponseDto getUser(
            @PathVariable("id") UUID userId,
            @RequestHeader("Authorization")String authorization);
}

