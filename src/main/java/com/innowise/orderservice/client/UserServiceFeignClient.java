package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8080"
)
public interface UserServiceFeignClient {
    @GetMapping("/api/users/{id}")
    UserResponseDto getUser(@PathVariable Long userId);
}
