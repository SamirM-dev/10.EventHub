package com.example.eventhub.user;

import com.example.eventhub.auth.jwt.RefreshToken;
import com.example.eventhub.booking.Booking;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.event.Event;
import com.example.eventhub.review.Review;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true,nullable = false)
    private String email;
    private String password;
    private String provider;
    private String providerId;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "organizer")
    private List<Event> events=new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Booking> bookings = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Review> reviews= new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<RefreshToken> refreshTokens=new ArrayList<>();

    public User(){}

    public User(String name, String email, String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt=LocalDateTime.now();
    }
    public User(String name, String email, String provider, String providerId, UserRole role) {
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.createdAt=LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(List<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }
}
