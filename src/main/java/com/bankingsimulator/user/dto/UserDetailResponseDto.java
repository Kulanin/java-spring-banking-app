package com.bankingsimulator.user.dto;

import java.util.List;

import com.bankingsimulator.account.dto.AccountSummaryDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailResponseDto {

    private Long id;
    private String email;
    private String name;
    private List<AccountSummaryDto> accounts;
    private String createdAt;
    private String updatedAt;

}
