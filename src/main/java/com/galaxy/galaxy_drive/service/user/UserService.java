package com.galaxy.galaxy_drive.service.user;

import com.galaxy.galaxy_drive.exception.minio.MinioCreateException;
import com.galaxy.galaxy_drive.exception.user.UserAlreadyExistsException;
import com.galaxy.galaxy_drive.exception.user.UserNotFoundException;
import com.galaxy.galaxy_drive.model.dto.user.UserCreateDto;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.user.Role;
import com.galaxy.galaxy_drive.model.entity.user.SignupMethod;
import com.galaxy.galaxy_drive.model.entity.user.User;
import com.galaxy.galaxy_drive.model.mapper.user.UserMapper;
import com.galaxy.galaxy_drive.model.repository.UserRepository;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements UserDetailsService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    MinioService minioService;
    MessageSource messageSource;

    public UserReadDto findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .map(userMapper::userToUserReadDto)
                .orElseThrow(() -> new UserNotFoundException(getUserNotFoundMessage(userName)));
    }

    @Transactional
    public void updateUser(String userName, String name, String lastName) {
        var user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException(getUserNotFoundMessage(userName)));
        user.setFirstName(name);
        user.setLastName(lastName);
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public UserReadDto create(UserCreateDto userCreateDto) {
        var newUser = userMapper.userCreateDtoToUser(userCreateDto);
        try {
            var user = userRepository.save(newUser);
            minioService.createUserFolder(user.getId());
            return userMapper.userToUserReadDto(user);
        } catch (MinioCreateException minioCreateException) {
            throw minioCreateException;
        } catch (Exception e) {
            throw new UserAlreadyExistsException(messageSource.getMessage(
                    "error.message.userExist",
                    new String[]{newUser.getUserName()},
                    LocaleContextHolder.getLocale()));
        }
    }

    @Transactional
    public Optional<User> createUserIfNotExist(OidcUserRequest userRequest) {
        return createUserAndFolderIfNotExist(userRequest.getIdToken().getEmail(), SignupMethod.GOOGLE);
    }

    @Transactional
    public Optional<User> createUserIfNotExist(OAuth2User oAuth2User) {
        return createUserAndFolderIfNotExist(oAuth2User.getAttribute("login").toString(), SignupMethod.GITGUB);
    }

    @Override
    public UserDetails loadUserByUsername(String userName) {
        return userRepository.findByUserName(userName)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUserName(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                ))
                .orElseThrow(() -> new UserNotFoundException(getUserNotFoundMessage(userName)));
    }

    @Transactional
    public Optional<User> createUserAndFolderIfNotExist(String userName, SignupMethod signupMethod) {
        if (!userRepository.existsByUserName(userName)) {
            try {
                var user = userRepository.save(User.builder()
                        .userName(userName)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(Role.USER)
                        .signupMethod(signupMethod)
                        .build());
                minioService.createUserFolder(user.getId());
                return Optional.of(user);
            } catch (MinioCreateException minioCreateException) {
                throw minioCreateException;
            }
        }
        return Optional.empty();
    }

    private String getUserNotFoundMessage(String userName) {
        return messageSource.getMessage(
                "error.message.userNotFound",
                new String[]{userName},
                LocaleContextHolder.getLocale());
    }
}
