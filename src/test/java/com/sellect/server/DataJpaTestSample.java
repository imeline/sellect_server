package com.sellect.server;

import com.sellect.server.config.JpaConfig;
import com.sellect.server.config.JsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
    JpaConfig.class,
    JsonConfig.class,
})
class DataJpaTestSample {

    @Autowired
    private TestEntityManager em;

//    @Test
//    void contextLoads() {
//    }

}
