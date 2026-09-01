package com.example.eventhub.review;

import com.example.eventhub.event.Event;
import com.example.eventhub.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(
        name = "uq_reviews_event_user",
        columnNames = {"event_id", "user_id"}
))
public class Review {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne@JoinColumn(name ="event_id",nullable = false)
    private Event event;
    @ManyToOne@JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private int rating;
    @Column(nullable = false)
    private String comment;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    public Review(){}

    public Review(Event event, User user, int rating, String comment) {
        this.event = event;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.createdAt=LocalDateTime.now();
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
