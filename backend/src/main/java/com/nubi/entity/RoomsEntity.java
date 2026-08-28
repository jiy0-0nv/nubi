package com.nubi.entity;

import com.nubi.entity.converter.RoomStatusConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor
public class RoomsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UsersEntity owner;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String street;

    @Column(name = "rating_average", nullable = false)
    private double ratingAverage;

    @Column(name = "checkin_time", nullable = false)
    private LocalTime checkinTime;

    @Column(name = "checkout_time", nullable = false)
    private LocalTime checkoutTime;

    @Column(name = "weekend_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal weekendPrice;

    @Column(name = "weekday_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal weekdayPrice;

    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    @Convert(converter = RoomStatusConverter.class)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public RoomsEntity(UsersEntity owner, String name, String description, String country, String city,
                       String street, LocalTime checkinTime, LocalTime checkoutTime,
                       BigDecimal weekendPrice, BigDecimal weekdayPrice, int maxGuests) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.country = country;
        this.city = city;
        this.street = street;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
        this.weekendPrice = weekendPrice;
        this.weekdayPrice = weekdayPrice;
        this.maxGuests = maxGuests;
        this.ratingAverage = 0.0;
        this.status = RoomStatus.ACTIVE;
    }

    public enum RoomStatus {
        ACTIVE, INACTIVE
    }
}