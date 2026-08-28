package com.nubi.entity;

import com.nubi.entity.converter.BookingStatusConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor
public class BookingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomsEntity room;

    @Column(name = "check_in_date", nullable = false)
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDateTime checkOutDate;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Convert(converter = BookingStatusConverter.class)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder
    public BookingsEntity(UserEntity user, RoomsEntity room, LocalDateTime checkInDate,
                          LocalDateTime checkOutDate, int guestCount, BigDecimal totalPrice) {
        this.user = user;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
        this.status = BookingStatus.CONFIRMED;
    }

    public enum BookingStatus {
        CONFIRMED, COMPLETED, CANCELLED
    }
}