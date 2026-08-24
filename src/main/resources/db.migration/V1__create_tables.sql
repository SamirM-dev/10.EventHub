CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(100) NOT NULL ,
    email VARCHAR(100) NOT NULL ,
    password VARCHAR(100),
    provider VARCHAR(50),
    provider_id VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_provider_provider_id UNIQUE (provider,provider_id)
);

CREATE TABLE events(
    id BIGSERIAL PRIMARY KEY ,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(200) NOT NULL ,
    category VARCHAR(20),
    venue VARCHAR(100) NOT NULL ,
    start_time TIMESTAMP NOT NULL ,
    end_time TIMESTAMP NOT NULL ,
    capacity INT NOT NULL ,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL ,
    organizer_id BIGINT NOT NULL ,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ch_events_category CHECK ( category IN ('CONCERT', 'CONFERENCE', 'WORKSHOP', 'SPORTS', 'OTHER' )),
    CONSTRAINT ch_events_capacity CHECK ( capacity>0 ),
    CONSTRAINT ch_events_price CHECK ( price>=0 ),
    CONSTRAINT ch_events_status CHECK ( status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES users(id)
);

CREATE TABLE bookings(
    id BIGSERIAL PRIMARY KEY ,
    event_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL ,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL ,
    status VARCHAR(20) NOT NULL ,
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ch_bookings_quantity CHECK ( quantity>0 ),
    CONSTRAINT ch_bookings_status CHECK ( status IN ('CONFIRMED', 'CANCELLED'))
);

CREATE TABLE reviews(
    id BIGSERIAL PRIMARY KEY ,
    event_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL ,
    rating INT NOT NULL,
    comment VARCHAR(500) NOT NULL ,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ch_reviews_rating CHECK ( rating BETWEEN 1 AND 5),
    CONSTRAINT uq_reviews_event_user UNIQUE (event_id,user_id)
)