package com.terstredisproject1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TerstRedisProject1ApplicationTests {

    @Test
    void contextLoads() {
    }

}
