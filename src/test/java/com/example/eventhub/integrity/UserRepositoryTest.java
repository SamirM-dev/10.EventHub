package com.example.eventhub.integrity;

import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserRepositoryTest {

    @Autowired
    UserRepository repository;

    @Test
    void createUser_UserWithUniqueEmail_ReturnsUser(){
        User user = new User("User","user@gmail.com","password", UserRole.ORGANIZER);
        User saved=repository.save(user);

        SoftAssertions.assertSoftly(soft->{
            soft.assertThat(saved.getId()).isEqualTo(1L);
            soft.assertThat(saved.getName()).isEqualTo(user.getName());
            soft.assertThat(saved.getEmail()).isEqualTo(user.getEmail());
            soft.assertThat(saved.getPassword()).isEqualTo(user.getPassword());
            soft.assertThat(saved.getRole()).isEqualTo(user.getRole());
        });
    }
    @Test
    void createUser_UserWithExistsEmail_ThrowsException(){
        User user = new User("User","user@gmail.com","password", UserRole.ORGANIZER);
        User user2 = new User("User2","user@gmail.com","password2", UserRole.ORGANIZER);
        repository.save(user);

        assertThatThrownBy(()->repository.save(user2)).isInstanceOf(DataIntegrityViolationException.class);



    }
    @Test
    void getUser_ExistsUser_ReturnsOptionalOfUser(){
        User user = new User("User","user@gmail.com","password", UserRole.ORGANIZER);
        User saved=repository.save(user);
        assertThat(saved.getId()).isEqualTo(1L);

        User got = repository.findById(1L).get();
        assertThat(saved).isEqualTo(got);
    }
    @Test
    void getUser_NotExistsUser_ReturnsEmptyOptional(){
        assertThat(repository.findById(99L)).isEmpty();
    }
    @Test
    void updateUser_ExistsUser_ReturnsUser(){
        User user = new User("User","user@gmail.com","password", UserRole.ORGANIZER);
        User saved=repository.save(user);
        assertThat(saved.getId()).isEqualTo(1L);

        user.setName("UpdatedUser");
        User updated=repository.save(user);

        assertThat(updated).isEqualTo(saved);
        assertThat(updated.getName()).isNotEqualTo("User");
    }
    @Test
    void deleteUser_ExistsUser_ReturnsNothing(){
        User user = new User("User","user@gmail.com","password", UserRole.ORGANIZER);
        User saved=repository.save(user);
        assertThat(saved.getId()).isEqualTo(1L);
        repository.delete(saved);

        assertThat(repository.findById(1L)).isEmpty();
    }
}
