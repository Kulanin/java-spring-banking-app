package com.demo.user;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.demo.common.ApiResponse;
import com.demo.common.PageRequestDto;
import com.demo.common.PageResponseDto;
import com.demo.user.dto.UserCreateDto;
import com.demo.user.dto.UserDetailResponseDto;
import com.demo.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    UserController(UserService userService, UserMapper userMapper) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<UserResponseDto>>> getUsers(
            @Valid PageRequestDto pageRequest) {
        Page<User> userPage = userService.findAll(pageRequest.toPageable());
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
        User user = userService.findByIdWithAccounts(id);
        UserDetailResponseDto userDto = userMapper.toDetailDto(user);
        return ResponseEntity.ok(ApiResponse.success(userDto, "User retrieved successfully"));
    }

}