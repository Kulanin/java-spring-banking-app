package com.demo.user;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.demo.account.AccountMapper;
import com.demo.common.PageResponseDto;
import com.demo.user.dto.UserCreateDto;
import com.demo.user.dto.UserDetailResponseDto;
import com.demo.user.dto.UserResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Component
public class UserMapper {

    final private AccountMapper accountMapper;

    UserMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public User toEntity(UserCreateDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        return user;
    }

    public UserResponseDto toRerResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

    }

    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public List<UserResponseDto> toResponseDtoList(List<User> users) {
        return users.stream()
                .map(this::toResponseDto)
                .toList();
    }

    public UserDetailResponseDto toDetailDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .accounts(accountMapper.toSummaryDtoList(user.getAccounts()))
                .build();
    }

    public PageResponseDto<UserResponseDto> toPageResponseDto(Page<User> userPage) {

        if (userPage == null) {
            return PageResponseDto.<UserResponseDto>builder()
                    .content(Collections.emptyList())
                    .totalElements(0)
                    .totalPages(0)
                    .pageNumber(0)
                    .pageSize(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }

        return PageResponseDto.<UserResponseDto>builder()
                .content(toResponseDtoList(userPage.getContent()))
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();

    }

}
