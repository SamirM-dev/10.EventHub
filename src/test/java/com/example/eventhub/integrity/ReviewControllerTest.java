package com.example.eventhub.integrity;

import com.example.eventhub.auth.details.CustomUserDetailsService;
import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.booking.BookingService;
import com.example.eventhub.config.TestSecurityConfig;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.review.Review;
import com.example.eventhub.review.ReviewController;
import com.example.eventhub.review.ReviewRepository;
import com.example.eventhub.review.ReviewService;
import com.example.eventhub.review.dto.ReviewCreateRequest;
import com.example.eventhub.review.dto.ReviewResponse;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import({TestSecurityConfig.class, HelpForService.class})
public class ReviewControllerTest {
    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper json;
    @MockitoBean
    ReviewService reviewService;
    @MockitoBean
    EventRepository eventRepository;
    @MockitoBean
    BookingRepository bookingRepository;
    @MockitoBean
    ReviewRepository reviewRepository;
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("Тестирование метода получения отзывов по мероприятию")
    class GetReviewByEventTest{
        @Test
        void getReviewByEvent_NotExistsEvent_Returns404()throws Exception{
            when(reviewService.getReviewsByEvent(any(),any())).thenThrow(new EntityNotFoundException());

            mock.perform(get("/api/v1/events/1/reviews")).andExpect(status().isNotFound());
        }
        @Test
        void getReviewByEvent_ExistsEvent_Returns200()throws Exception{
            mock.perform(get("/api/v1/events/1/reviews")).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Тестирование метода создания отзыва")
    class CreateRequest{
        @Test
        void create_InValidRequest_Returns400()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            ReviewCreateRequest request=new ReviewCreateRequest(1,"");

            mock.perform(post("/api/v1/events/1/reviews").with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isBadRequest());
        }
        @Test
        void create_ValidRequest_Returns200()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            ReviewCreateRequest request=new ReviewCreateRequest(1,"very bad");
            ReviewResponse response=new ReviewResponse(1L,1L,1L,request.rating(),request.comment(), LocalDateTime.now());
            when(reviewService.create(any(),any(),any())).thenReturn(response);

            mock.perform(post("/api/v1/events/1/reviews").with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Тестирование метода удаления отзыва")
    class DeleteTest{
        @Test
        void create_NotOwner_Returns403()throws Exception{
            User owner=new User("Owner","owner@gmail.com","12345678", UserRole.USER);
            owner.setId(1L);
            Review review =new Review();
            review.setUser(owner);
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

            mock.perform(delete("/api/v1/reviews/1").with(authentication(auth))).andExpect(status().isForbidden());
        }
        @Test
        void delete_Admin_Returns204()throws Exception{
            User owner=new User("Owner","owner@gmail.com","12345678", UserRole.USER);
            owner.setId(1L);
            Review review =new Review();
            review.setUser(owner);
            User user=new User("User","user@gmail.com","12345678", UserRole.ADMIN);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

            mock.perform(delete("/api/v1/reviews/2").with(authentication(auth))).andExpect(status().isNoContent());
        }
    }


}
