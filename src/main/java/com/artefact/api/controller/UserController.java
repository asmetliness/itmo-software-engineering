package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.UploadUserImage;
import com.artefact.api.response.UserResponse;
import com.artefact.api.utils.FileNameGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();
        return new ResponseEntity<>(new UserResponse(user), HttpStatus.OK);
    }

    @GetMapping("/stalkers")
    public ResponseEntity<Iterable<User>> getAllStalkers() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();
        String role = user.getRole();

        if (!role.equals(Role.Huckster)) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        Iterable<User> users = userRepository.findByRole(Role.Stalker);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // TODO: Проверить
    @PostMapping("/image/upload")
    public ResponseEntity<String> uploadUserImage(@RequestBody UploadUserImage request) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();

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
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();

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
