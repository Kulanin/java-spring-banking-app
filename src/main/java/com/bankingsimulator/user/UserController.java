package com.bankingsimulator.user;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankingsimulator.common.ApiResponse;
import com.bankingsimulator.common.PageRequestDto;
import com.bankingsimulator.common.PageResponseDto;
import com.bankingsimulator.user.dto.UserCreateDto;
import com.bankingsimulator.user.dto.UserDetailResponseDto;
import com.bankingsimulator.user.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private static final Set<String> ALLOWED_SORTS = Set.of(
            "id", "name", "email");

    UserController(UserService userService, UserMapper userMapper) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<UserResponseDto>>> getUsers(
            @Valid PageRequestDto pageRequest) {
        Page<User> userPage = userService.findAll(pageRequest.toPageable(ALLOWED_SORTS));
        PageResponseDto<UserResponseDto> pageResponse = userMapper.toPageResponseDto(userPage);
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Users retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserCreateDto userCreateRequest) {

        User savedUser = userService.save(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedUser, "User created successfully"));

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponseDto>> getUserById(@PathVariable Long id) {
        UserDetailResponseDto user = userService.findByIdWithAccounts(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

}