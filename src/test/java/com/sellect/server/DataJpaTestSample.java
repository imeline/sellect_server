package com.sellect.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
class DataJpaTestSample {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void contextLoads() {
    }

}
