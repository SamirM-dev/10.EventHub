package com.example.eventhub.integrity;

import com.example.eventhub.event.Event;
import com.example.eventhub.review.Review;
import com.example.eventhub.review.ReviewRepository;
import com.example.eventhub.user.User;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
 public class ReviewRepositoryTest {

    @Autowired
    ReviewRepository repository;

    @Test
    @Sql("/create_user_and_event.sql")
    void createReview_UniquePairOfUserIdAndEventId_ReturnsReview(){
        User user = new User();
        user.setId(1L);
        Event event=new Event();
        event.setId(1L);
        Review review = new Review(event,user,5,"It was great");

        Review saved=repository.save(review);

        SoftAssertions.assertSoftly(soft->{
            soft.assertThat(saved.getUser().getId()).isEqualTo(user.getId());
            soft.assertThat(saved.getEvent().getId()).isEqualTo(event.getId());
            soft.assertThat(saved.getRating()).isEqualTo(5);
            soft.assertThat(saved.getComment()).isEqualTo("It was great");
        });


    }
    @Test
    @Sql("/create_user_and_event.sql")
    void createReview_ExistsPairOfUserIdAndEventId_ThrowsException(){
        User user = new User();
        user.setId(1L);
        Event event=new Event();
        event.setId(1L);
        Review review = new Review(event,user,5,"It was great");
        Review review2 = new Review(event,user,1,"It was bad");

        repository.save(review);


        assertThatThrownBy(()->repository.save(review2)).isInstanceOf(DataIntegrityViolationException.class);

    }
}
