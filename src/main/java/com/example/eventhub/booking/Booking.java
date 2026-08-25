package com.example.eventhub.booking;

import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.event.Event;
import com.example.eventhub.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne@JoinColumn(name = "event_id",nullable = false)
    private Event event;
    @ManyToOne@JoinColumn(name = "user_id",nullable = false)
    private User user;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "total_price",nullable = false)
    private BigDecimal totalPrice;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(name = "booked_at",nullable = false)
    private LocalDateTime bookedAt;

    public Booking() {}

    public Booking(Event event, User user, int quantity, BigDecimal totalPrice) {
        this.event = event;
        this.user = user;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status=BookingStatus.CONFIRMED;
        this.bookedAt=LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }
}
