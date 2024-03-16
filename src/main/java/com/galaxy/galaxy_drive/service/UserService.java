package com.galaxy.galaxy_drive.service;

import com.galaxy.galaxy_drive.model.dto.UserCreateDto;
import com.galaxy.galaxy_drive.model.dto.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.Role;
import com.galaxy.galaxy_drive.model.entity.SignupMethod;
import com.galaxy.galaxy_drive.model.entity.User;
import com.galaxy.galaxy_drive.model.mapper.CreateUserMapper;
import com.galaxy.galaxy_drive.model.mapper.ReadUserMapper;
import com.galaxy.galaxy_drive.model.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ReadUserMapper readUserMapper;
    private final CreateUserMapper createUserMapper;
    private final PasswordEncoder passwordEncoder;

    public Optional<UserReadDto> findByid(Long id) {
        return userRepository.findById(id)
                .map(readUserMapper::map);
    }
    public UserReadDto findByUserName(String userName){
        return userRepository.findByUserName(userName)
                .map(readUserMapper::map).get();
    }

    @Transactional
    public void updateUser(String userName, String name, String lastName){
        var user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + userName));
        user.setFirstName(name);
        user.setLastName(lastName);
        userRepository.saveAndFlush(user);
    }


    @Transactional
    public UserReadDto create(UserCreateDto userCreateDto) {
        return Optional.of(userCreateDto)
                .map(createUserMapper::map)
                .map(userRepository::save)
                .map(readUserMapper::map)
                .orElseThrow();
        // TODO: 12.03.2024 Выбросить исключение
    }

    @Transactional
    public void createUserIfNotExist(OidcUserRequest userRequest){
        var email = userRequest.getIdToken().getEmail();
        if (!userRepository.findByUserName(email).isPresent()){
            var user = User.builder()
                  .userName(email)
                  .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                  .role(Role.USER)
                  .signupMethod(SignupMethod.GOOGLE)
                  .build();
          userRepository.save(user);
      }
    }
    @Transactional
    public void createUserIfNotExist(OAuth2User oAuth2User){
        var login = oAuth2User.getAttribute("login").toString();
        if (!userRepository.findByUserName(login).isPresent()){
            var user = User.builder()
                  .userName(login)
                  .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                  .role(Role.USER)
                  .signupMethod(SignupMethod.GITGUB)
                  .build();
          userRepository.save(user);
      }
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserName(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUserName(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user " + username));
    }
}
