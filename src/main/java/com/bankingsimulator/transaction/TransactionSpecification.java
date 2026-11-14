package com.bankingsimulator.transaction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

public class TransactionSpecification {

    public static Specification<TransactionRecord> filterBy(
            Long accountId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("account").get("id"), accountId));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        endDate.atTime(LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
