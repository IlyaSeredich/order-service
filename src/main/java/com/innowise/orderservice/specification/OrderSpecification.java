package com.innowise.orderservice.specification;

import com.innowise.orderservice.dto.SearchOrderDto;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enumtype.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderSpecification {
    public Specification<Order> build(SearchOrderDto searchOrderDto) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

//            addDeletedPredicate(
//                    root,
//                    criteriaBuilder,
//                    predicates
//            );

            addCreatedAtPredicate(
                    searchOrderDto.from(),
                    searchOrderDto.to(),
                    root,
                    criteriaBuilder,
                    predicates
            );

            addStatusPredicate(
                    searchOrderDto.statuses(),
                    root,
                    criteriaBuilder,
                    predicates
            );

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

//    private static void addDeletedPredicate(
//            Root<Order> root,
//            CriteriaBuilder cb,
//            List<Predicate> predicates) {
//
//        predicates.add(cb.equal(root.get("deleted"), false));
//    }

    private static void addCreatedAtPredicate(
            LocalDateTime from,
            LocalDateTime to,
            Root<Order> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        Path<LocalDateTime> createdAt = root.get("createdAt");

        if (from != null && to != null) {
            predicates.add(criteriaBuilder.between(createdAt, from, to));
            return;
        }

        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(createdAt, from));
        }

        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(createdAt, to));
        }
    }


    private static void addStatusPredicate(
            List<OrderStatus> statuses,
            Root<Order> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (statuses != null && !statuses.isEmpty()) {
            Predicate predicate = root.get("status").in(statuses);
            predicates.add(predicate);
        }
    }
}

