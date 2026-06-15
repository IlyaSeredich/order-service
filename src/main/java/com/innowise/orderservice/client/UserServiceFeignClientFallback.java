package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserResponseDto;
import com.innowise.orderservice.exception.UserServiceNotAvailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserServiceFeignClientFallback implements UserServiceFeignClient{
    @Override
    public UserResponseDto getUser(
            String authorization,
            String email
    ) {
        throw new UserServiceNotAvailableException();
    }

    @Override
    public UserResponseDto getUser(UUID userId, String authorization) {
        throw new UserServiceNotAvailableException();
    }
}
