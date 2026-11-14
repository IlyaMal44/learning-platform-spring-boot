package com.example.learningplatform.service;

import com.example.learningplatform.domain.Profile;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.CreateUserRequest;
import com.example.learningplatform.dto.request.UpdateUserRequest;
import com.example.learningplatform.dto.request.UpsertProfileRequest;
import com.example.learningplatform.dto.response.ProfileResponse;
import com.example.learningplatform.dto.response.UserResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.UserMapper;
import com.example.learningplatform.repository.CourseRepository;
import com.example.learningplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public UserService(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(request.role());
        if (request.profile() != null) {
            user.setProfile(profileFrom(request.profile()));
        }
        User saved = userRepository.save(user);
        log.info("Создан пользователь id={} email={}", saved.getId(), saved.getEmail());
        return UserMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = requireUser(id);
        String email = normalizeEmail(request.email());
        if (!user.getEmail().equals(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }
        if (request.role() == Role.STUDENT && courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("Нельзя сменить роль: пользователь ведёт курсы");
        }
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(request.role());
        log.info("Обновлён пользователь id={}", id);
        return UserMapper.toResponse(user);
    }

    public UserResponse get(Long id) {
        return UserMapper.toResponse(requireUser(id));
    }

    public List<UserResponse> list(Role role) {
        List<User> users = role == null ? userRepository.findAll() : userRepository.findByRole(role);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        User user = requireUser(id);
        if (courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("Нельзя удалить пользователя: он назначен преподавателем курса");
        }
        userRepository.delete(user);
        log.info("Удалён пользователь id={}", id);
    }

    @Transactional
    public ProfileResponse upsertProfile(Long userId, UpsertProfileRequest request) {
        User user = requireUser(userId);
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            user.setProfile(profile);
        }
        profile.setBio(Texts.trimToNull(request.bio()));
        profile.setAvatarUrl(Texts.trimToNull(request.avatarUrl()));
        userRepository.save(user);
        log.info("Обновлён профиль пользователя id={}", userId);
        return UserMapper.toProfile(user.getProfile());
    }

    public User requireUser(Long id) {
        return Lookups.required(userRepository.findById(id), "Пользователь с id=" + id + " не найден");
    }

    private Profile profileFrom(UpsertProfileRequest request) {
        Profile profile = new Profile();
        profile.setBio(Texts.trimToNull(request.bio()));
        profile.setAvatarUrl(Texts.trimToNull(request.avatarUrl()));
        return profile;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
