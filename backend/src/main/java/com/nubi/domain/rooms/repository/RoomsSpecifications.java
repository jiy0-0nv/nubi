package com.nubi.domain.rooms.repository;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.RoomsEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class RoomsSpecifications {

    private RoomsSpecifications() {
    }

    // keywords는 OR로, 나머지 조건은 AND로 묶는다. name/description/city/country 중 하나라도
    // 키워드를 포함하면 매치되고, 여러 키워드는 그중 하나만 맞아도 통과한다 (카테고리 동의어 검색용).
    public static Specification<RoomsEntity> search(List<String> keywords, Integer guests,
                                                      LocalDate checkInDate, LocalDate checkOutDate,
                                                      BookingsEntity.BookingStatus cancelledStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keywords != null && !keywords.isEmpty()) {
                List<Predicate> keywordPredicates = new ArrayList<>();
                for (String keyword : keywords) {
                    String pattern = "%" + keyword + "%";
                    keywordPredicates.add(cb.or(
                            cb.like(root.get("name"), pattern),
                            cb.like(root.get("description"), pattern),
                            cb.like(root.get("city"), pattern),
                            cb.like(root.get("country"), pattern)
                    ));
                }
                predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
            }

            if (guests != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), guests));
            }

            if (checkInDate != null && checkOutDate != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<BookingsEntity> booking = subquery.from(BookingsEntity.class);
                subquery.select(booking.get("id"));
                subquery.where(
                        cb.equal(booking.get("room"), root),
                        cb.notEqual(booking.get("status"), cancelledStatus),
                        cb.lessThan(cb.function("DATE", LocalDate.class, booking.get("checkInDate")), checkOutDate),
                        cb.greaterThan(cb.function("DATE", LocalDate.class, booking.get("checkOutDate")), checkInDate)
                );
                predicates.add(cb.not(cb.exists(subquery)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}