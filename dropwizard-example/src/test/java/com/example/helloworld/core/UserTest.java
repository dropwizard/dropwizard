package com.example.helloworld.core;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {
    @Test
    void testUser() {
        Set<String> roles = new HashSet<>();
        roles.add("userRole1");
        roles.add("userRole2");
        User user = new User("userName", roles);

        assertThat(user.getName()).isEqualTo("userName");
        assertThat(user.getRoles()).hasSize(2).contains("userRole1", "userRole2");
        assertThat(user.getId()).isNotNegative().isLessThan(100);

        User secondUser = new User("secondUser");
        assertThat(secondUser.getRoles()).isNull();
    }
}
