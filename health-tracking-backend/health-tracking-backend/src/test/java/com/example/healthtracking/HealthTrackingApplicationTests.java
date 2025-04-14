package com.example.healthtracking;

import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class HealthTrackingApplicationTests {

	@MockBean
    private Firestore firestore;

    @Test
    void contextLoads() {
        // Test sırasında test profili yüklenir
        System.out.println("Test profili yüklendi!");
    }
}