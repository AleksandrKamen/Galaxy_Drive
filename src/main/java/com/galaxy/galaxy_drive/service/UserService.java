package com.galaxy.galaxy_drive.service;

import com.galaxy.galaxy_drive.model.dto.UserCreateDto;
import com.galaxy.galaxy_drive.model.dto.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.Role;
import com.galaxy.galaxy_drive.model.entity.User;
import com.galaxy.galaxy_drive.model.mapper.CreateUserMapper;
import com.galaxy.galaxy_drive.model.mapper.ReadUserMapper;
import com.galaxy.galaxy_drive.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

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
    public void createUserIfNotExist(String userEmail, String userName, String familyName){
      if (!userRepository.findByUserName(userEmail).isPresent()){
          var user = User.builder()
                  .userName(userEmail)
                  .password(passwordEncoder.encode(userName))
                  .firstName(userName)
                  .lastName(familyName)
                  .role(Role.USER)
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
