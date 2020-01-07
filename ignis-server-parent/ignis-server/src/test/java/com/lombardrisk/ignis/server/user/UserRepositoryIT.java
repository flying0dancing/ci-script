package com.lombardrisk.ignis.server.user;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.user.model.SecUser;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class UserRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private UserRepository repository;

    @Test
    public void saveAndFindAll() {
        SecUser user1 = Populated.user()
                .username("user1")
                .build();

        SecUser user2 = Populated.user()
                .username("user2").build();

        repository.saveAll(asList(user1, user2));

        List<SecUser> users = repository
                .findAll(PageRequest.of(0, 2))
                .getContent();

        soft.assertThat(users)
                .hasSize(2);
    }

    @Test
    public void findUserByName_isNotCaseSensitive() {
        SecUser user1 = Populated.user()
                .username("user1")
                .password("userpassword")
                .build();

        repository.saveAll(Collections.singletonList(user1));

        SecUser user = repository.findByUsername("uSer1");

        assertThat(user).isNotNull();
        soft.assertThat(user.getUsername()).isEqualTo("user1");
        soft.assertThat(user.getPassword()).isEqualTo("userpassword");
    }

}