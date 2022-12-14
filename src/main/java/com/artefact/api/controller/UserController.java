package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.UpdateUserRequest;
import com.artefact.api.request.UploadUserImageRequest;
import com.artefact.api.response.UserResponse;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.FileNameGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private static final String RelativeImagesPath = "src/main/resources/static/images/users/";

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<UserResponse> getUserDetails() {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();
        return new ResponseEntity<>(new UserResponse(user), HttpStatus.OK);
    }


    @PutMapping("/current")
    public ResponseEntity<UserResponse> updateUserDetails(UpdateUserRequest request) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setNickname(request.getNickname());

        userRepository.save(user);

        return new ResponseEntity<>(new UserResponse(user), HttpStatus.OK);
    }

    @GetMapping("/stalkers")
    public ResponseEntity<Iterable<User>> getAllStalkers() {
        return getUsersByRole(Role.Stalker, new Role[]{Role.Huckster});
    }

    @GetMapping("/couriers")
    public ResponseEntity<Iterable<User>> getAllCouriers() {
        return getUsersByRole(Role.Courier, new Role[]{Role.Huckster, Role.WeaponDealer});
    }

    private ResponseEntity<Iterable<User>> getUsersByRole(Role selectRole, Role[] requiredRoles) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if (Arrays.stream(requiredRoles).noneMatch(r -> r.equals(user.getRole()))) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        var users = userRepository.findByRole(selectRole);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // TODO: Проверить
    @PostMapping("/image/upload")
    public ResponseEntity<String> uploadUserImage(@RequestBody UploadUserImageRequest request) {
        var userId = Auth.userId();

        User user = userRepository.findById(userId).get();

        MultipartFile image = request.getImage();
        if (image == null) {
            return new ResponseEntity<>("Изображение отсутствует", HttpStatus.BAD_REQUEST);
        }

        String contentType = image.getContentType();
        if (contentType == null) {
            return new ResponseEntity<>("Неизвестный contentType", HttpStatus.BAD_REQUEST);
        }

        if (contentType.equals("image/jpeg") || contentType.equals("image/png")) {
            return new ResponseEntity<>("Неподдерживаемый формат изображений", HttpStatus.BAD_REQUEST);
        }

        String format = contentType.substring("image/".length()); // Достаем формат изображения
        String fileName = FileNameGenerator.generateFileName(format);

        File file = new File(UserController.RelativeImagesPath + fileName);
        try {
            image.transferTo(file);
        } catch (IOException exp) {
            return new ResponseEntity<>("Внутренняя ошибка", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.removeUserImage(); // Удаляем старое изображение

        user.setImagePath(fileName);
        userRepository.save(user);

        return new ResponseEntity<>("Ок", HttpStatus.OK);
    }

    @DeleteMapping("/image/delete")
    public ResponseEntity<String> removeUserImage() {
        var userId = Auth.userId();

        User user = userRepository.findById(userId).get();

        String imagePath = user.getImagePath();
        if (imagePath == null) {
            return new ResponseEntity<>("Изображение отсутствует", HttpStatus.BAD_REQUEST);
        }

        File file = new File(UserController.RelativeImagesPath + imagePath);
        if (file.delete()) {
            return new ResponseEntity<>("Внутренняя ошибка", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>("Ок", HttpStatus.OK);
        }
    }
}
