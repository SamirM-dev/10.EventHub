package com.example.eventhub.unit;

import com.example.eventhub.helper.OneTimeCodeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OneTimeCodeStoreTest {

    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    OneTimeCodeStore store;

    @BeforeEach
    void setUp(){
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("Тестирование метода генерации одноразового кода")
    class GenerateOAuthTest{
        @Test
        void generateOAuth_StoresUserIdWithCorrectKeyAndTtl(){
            String code = store.generateOAuth(10L);

            assertThat(code).isNotBlank();
            verify(valueOperations).set("oauth2:code:"+code,"10", Duration.ofSeconds(60));
        }
    }
    @Nested
    @DisplayName("Тестирование метода извлечения айди пользователя из одноразового кода")
    class ConsumeOAuthTest{
        @Test
        void consumeOAuth_ExistingCode_ReturnsUserIdAndDeletesKey(){
            String code = store.generateOAuth(10L);
            when(valueOperations.getAndDelete(eq("oauth2:code:"+code))).thenReturn("10");

            assertThat(store.consumeOAuth(code)).isEqualTo(10L);
            verify(valueOperations).getAndDelete("oauth2:code:"+code);
        }
        @Test
        void consumeOAuth_NonExistentCode_ReturnsNull(){
            when(valueOperations.getAndDelete(eq("oauth2:code:somecode"))).thenReturn(null);

            assertThat(store.consumeOAuth("somecode")).isNull();
        }

    }

}
