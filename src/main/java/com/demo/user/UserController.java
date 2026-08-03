package com.demo.user;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.account.dto.AccountSummaryDto;
import com.demo.common.ApiResponse;
import com.demo.user.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUsers() {

        try {
            // use dto
            List<User> users = userService.findAll();

            List<UserResponseDto> userDtos = users.stream().map(user -> new UserResponseDto(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getAccounts().stream().map(account -> new AccountSummaryDto(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getAccountName(),
                            account.getBalance(),
                            account.getAccountType(),
                            account.getStatus()

                    )).toList()

            )).toList();
            return ResponseEntity.ok(ApiResponse.success(userDtos, "Users retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve users", "SERVER_ERROR"));
        }

    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {

        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedUser, "User created successfully"));

    }

}