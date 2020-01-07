package com.lombardrisk.ignis.server.security;

import com.lombardrisk.ignis.server.security.exception.UserWithBlankPasswordException;
import com.lombardrisk.ignis.server.user.UserRepository;
import com.lombardrisk.ignis.server.user.UserService;
import com.lombardrisk.ignis.server.user.model.SecUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    public void save_saveUsernameInLowercase () throws Exception {
        userService.save(new SecUser("AnDerson", "anderson"));

        ArgumentCaptor<SecUser> captor = ArgumentCaptor.forClass(SecUser.class);
        verify(userRepository).save(captor.capture());
        SecUser retrievedUser = captor.getValue();

        assertThat(retrievedUser.getUsername()).isEqualTo("anderson");
    }

    @Test
    public void findByUsername_ExistentUser_ReturnsSecUser() {
        when(userRepository.findByUsername(anyString()))
                .thenReturn(new SecUser("anderson", "anderson"));

        assertThat(userService.findByUsername("anderson").get().getUsername())
                .isEqualTo("anderson");
    }

    @Test
    public void findByUsername_ExistentUserWithoutPassword_ThrowsException() {
        when(userRepository.findByUsername(anyString()))
                .thenReturn(new SecUser("anderson", null));

        assertThatThrownBy(() -> userService.findByUsername("anderson"))
                .isInstanceOf(UserWithBlankPasswordException.class)
                .hasMessage("User with username [anderson] has a blank password");
    }

    @Test
    public void findByUsername_NonExistentUser_ReturnsEmpty() {
        assertThat(userService.findByUsername("neo").isPresent())
                .isFalse();
    }
}
