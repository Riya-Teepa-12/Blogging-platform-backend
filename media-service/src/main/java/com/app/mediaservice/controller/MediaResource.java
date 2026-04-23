package com.app.mediaservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.mediaservice.dto.LinkPostRequest;
import com.app.mediaservice.dto.MediaResponse;
import com.app.mediaservice.dto.UpdateAltTextRequest;
import com.app.mediaservice.service.MediaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaResource {

    private final MediaService mediaService;

    @PostMapping
    public MediaResponse uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploaderId") Long uploaderId,
            @RequestParam(value = "altText", required = false) String altText) {
        return mediaService.uploadMedia(file, uploaderId, altText);
    }

    @GetMapping("/{mediaId}")
    public MediaResponse getById(@PathVariable Long mediaId) {
        return mediaService.getMediaById(mediaId);
    }

    @GetMapping("/uploader/{uploaderId}")
    public List<MediaResponse> getByUploader(@PathVariable Long uploaderId) {
        return mediaService.getMediaByUploader(uploaderId);
    }

    @GetMapping("/post/{postId}")
    public List<MediaResponse> getByPost(@PathVariable Long postId) {
        return mediaService.getMediaByPost(postId);
    }

    @GetMapping("/all")
    public List<MediaResponse> getAll(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return mediaService.getAllMedia(includeDeleted);
    }

    @PutMapping("/{mediaId}/alt-text")
    public MediaResponse updateAltText(@PathVariable Long mediaId, @Valid @RequestBody UpdateAltTextRequest request) {
        return mediaService.updateAltText(mediaId, request);
    }

    @PostMapping("/link")
    public MediaResponse linkToPost(@Valid @RequestBody LinkPostRequest request) {
        return mediaService.linkToPost(request);
    }

    @PostMapping("/{mediaId}/unlink")
    public MediaResponse unlinkFromPost(@PathVariable Long mediaId) {
        return mediaService.unlinkFromPost(mediaId);
    }

    @DeleteMapping("/{mediaId}")
    public Map<String, String> deleteMedia(@PathVariable Long mediaId) {
        mediaService.deleteMedia(mediaId);
        return Map.of("message", "Media deleted");
    }

    @DeleteMapping("/cleanup")
    public Map<String, Long> cleanupDeleted() {
        return Map.of("deletedCount", mediaService.cleanupDeleted());
    }
}
