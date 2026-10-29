package com.foodexpiry.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.nio.file.*;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    private final Path uploadDir =
            Paths.get("uploads").toAbsolutePath().normalize();

    public ImageController() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile image) {

        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select an image.");
            }

            String contentType = image.getContentType();

            if (contentType == null ||
                !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body("Only image files are allowed.");
            }

            String originalName = image.getOriginalFilename();
            String extension = "";

            if (originalName != null &&
                originalName.lastIndexOf(".") >= 0) {
                extension = originalName.substring(
                        originalName.lastIndexOf(".")
                ).toLowerCase();
            }

            if (!extension.matches("\\.(jpg|jpeg|png|webp)$")) {
                return ResponseEntity.badRequest()
                        .body("Use JPG, JPEG, PNG, or WEBP images.");
            }

            String fileName = UUID.randomUUID() + extension;

            Path target = uploadDir.resolve(fileName).normalize();

            if (!target.startsWith(uploadDir)) {
                return ResponseEntity.badRequest()
                        .body("Invalid filename.");
            }

            Files.copy(image.getInputStream(), target);

            return ResponseEntity.ok(
                    java.util.Map.of("imageName", fileName)
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Image upload failed.");
        }
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<?> getImage(
            @PathVariable String fileName) {

        try {
            Path file = uploadDir.resolve(fileName).normalize();

            if (!file.startsWith(uploadDir) ||
                !Files.exists(file) ||
                !Files.isRegularFile(file)) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(file);

            if (contentType == null ||
                !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(Files.readAllBytes(file));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}