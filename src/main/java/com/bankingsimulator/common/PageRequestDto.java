package com.bankingsimulator.common;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {

    @Min(value = 0, message = "Page number must be at least 0")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    public Pageable toPageable(Set<String> allowedSortFields) {
        int safePage = (page != null && page >= 0) ? page : 0;
        int safeSize = (size != null && size >= 1 && size <= 100) ? size : 10;

        String safeSortBy = (sortBy != null && allowedSortFields.contains(sortBy))
                ? sortBy
                : "id";

        Sort.Direction safeDirection = (sortDirection != null)
                ? sortDirection
                : Sort.Direction.ASC;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
    }
}
