package com.artefact.api.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UploadUserImageRequest {
    MultipartFile image;
}
