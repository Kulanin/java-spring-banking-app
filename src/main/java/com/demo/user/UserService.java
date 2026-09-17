package com.demo.user;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.demo.user.dto.UserCreateDto;
import com.demo.user.dto.UserDetailResponseDto;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public UserDetailResponseDto findByIdWithAccounts(Long id) {
        User user = userRepository.findByIdWithAccounts(id);
        return this.userMapper.toDetailDto(user);
    }

    public User save(UserCreateDto createDto) {
        User user = userMapper.toEntity(createDto);
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

}
