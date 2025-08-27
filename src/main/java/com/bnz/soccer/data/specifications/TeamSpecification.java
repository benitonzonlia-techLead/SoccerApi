package com.bnz.soccer.data.specifications;

import com.bnz.soccer.data.entity.Team;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class TeamSpecification {

    public static Specification<Team> nameContainsIgnoreCase(String name) {
        return (root, query, cb) ->
                name == null || name.isBlank()
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Team> budgetGreaterThanOrEqualTo(BigDecimal minBudget) {
        return (root, query, cb) ->
                minBudget == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get("budget"), minBudget);
    }

    public static Specification<Team> fetchPlayers() {
        return (root, query, cb) -> {
            root.fetch("players", JoinType.LEFT);
            query.distinct(true);
            return null;
        };
    }
}
