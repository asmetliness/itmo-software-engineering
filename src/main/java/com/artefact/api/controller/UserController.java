package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.UpdateUserRequest;
import com.artefact.api.response.UserResponse;
import com.artefact.api.utils.ApiErrors;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.FileNameGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private static final String RelativeImagesPath = "src/main/resources/static/images/users/";

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<Object> getUserDetails() {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();
        return new ResponseEntity<>(new UserResponse(user), HttpStatus.OK);
    }


    @PutMapping("/current")
    public ResponseEntity<UserResponse> updateUserDetails(@RequestBody UpdateUserRequest request) {
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
    public ResponseEntity<Object> uploadUserImage(@RequestParam("image") MultipartFile image) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();

        if (image == null) {
            return new ResponseEntity<>(ApiErrors.User.ImageIsEmpty, HttpStatus.BAD_REQUEST);
        }

        var contentType = image.getContentType();
        if (contentType == null) {
            return new ResponseEntity<>(ApiErrors.User.UnknownImageType, HttpStatus.BAD_REQUEST);
        }

        var format = contentType.substring("image/".length()); // Достаем формат изображения
        var fileName = FileNameGenerator.generateFileName(format);

        var resultPath = Paths.get(UserController.RelativeImagesPath + fileName).toAbsolutePath();
        var folder = new File(UserController.RelativeImagesPath);
        try {
            if(!folder.exists()) {
                folder.mkdirs();
            }
            image.transferTo(resultPath);
        } catch (IOException exp) {
            return new ResponseEntity<>(ApiErrors.UnexpectedError, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.removeUserImage(); // Удаляем старое изображение

        user.setImagePath(fileName);
        userRepository.save(user);

        return getUserDetails();
    }

    @DeleteMapping("/image/delete")
    public ResponseEntity<Object> removeUserImage() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();

        var imagePath = user.getImagePath();
        if (imagePath == null) {
            return new ResponseEntity<>(ApiErrors.User.ImageIsEmpty, HttpStatus.BAD_REQUEST);
        }

        var file = new File(UserController.RelativeImagesPath + imagePath);
        if (file.delete()) {
            user.setImagePath(null);
            userRepository.save(user);
            return getUserDetails();
        } else {
            return new ResponseEntity<>(ApiErrors.UnexpectedError, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
