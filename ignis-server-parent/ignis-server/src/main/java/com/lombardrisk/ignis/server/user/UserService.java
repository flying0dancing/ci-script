package com.lombardrisk.ignis.server.user;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.security.exception.UserExistsException;
import com.lombardrisk.ignis.server.security.exception.UserWithBlankPasswordException;
import com.lombardrisk.ignis.server.user.model.PasswordError;
import com.lombardrisk.ignis.server.user.model.SecUser;
import com.lombardrisk.ignis.server.user.model.SecUserPassword;
import io.vavr.control.Option;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.server.user.model.PasswordError.USER_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class UserService {

    private static final String INTERNAL_JOB_USER = "InternalJobUser";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SecUser save(final SecUser user) throws UserExistsException {
        if (checkUserExists(user)) {
            throw new UserExistsException();
        }

        user.setUsername(user.getUsername().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private boolean checkUserExists(final SecUser user) {
        return user.getId() == null
                && findByUsername(user.getUsername()).isPresent();
    }

    public Optional<SecUser> findByUsername(final String username) {
        SecUser secUser = userRepository.findByUsername(username.toLowerCase());
        if (secUser == null) {
            return Optional.empty();
        }
        if (isBlank(secUser.getPassword())) {
            throw new UserWithBlankPasswordException("User with username [" + username + "] has a blank password");
        }
        return Optional.of(secUser);
    }

    public List<SecUser> findAll() {
        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(INTERNAL_JOB_USER))
                .collect(Collectors.toList());
    }

    @Transactional
    public Option<ErrorResponse> changePassword(final String username, final SecUserPassword userPassword) {

        Optional<SecUser> findUser = findByUsername(username);
        if (!findUser.isPresent()) {
            return Option.of(
                    ErrorResponse.valueOf("User " + username + " not found", USER_NOT_FOUND.name()));
        }

        SecUser storedUser = findUser.get();
        if (!passwordEncoder.matches(userPassword.getOldPassword(), storedUser.getPassword())) {
            return Option.of(ErrorResponse.valueOf(
                    "Incorrect password", PasswordError.INCORRECT_PASSWORD.name()));
        }

        storedUser.setPassword(passwordEncoder.encode(userPassword.getNewPassword()));
        userRepository.save(storedUser);
        return Option.none();
    }

    @Transactional
    public void deleteUser(final Long userId) {
        userRepository.deleteById(userId);
    }
}
