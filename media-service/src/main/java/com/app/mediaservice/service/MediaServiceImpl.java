package com.app.mediaservice.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.mediaservice.dto.LinkPostRequest;
import com.app.mediaservice.dto.MediaResponse;
import com.app.mediaservice.dto.UpdateAltTextRequest;
import com.app.mediaservice.entity.Media;
import com.app.mediaservice.repository.MediaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf");

    private final MediaRepository mediaRepository;

    @Value("${inkwell.media.base-url}")
    private String mediaBaseUrl;

    @Override
    @Transactional
    public MediaResponse uploadMedia(MultipartFile file, Long uploaderId, String altText) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (uploaderId == null) {
            throw new IllegalArgumentException("uploaderId is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("File exceeds 10MB limit");
        }
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        String extension = extension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        String url = mediaBaseUrl + "/" + filename;

        Media media = Media.builder()
                .uploaderId(uploaderId)
                .filename(filename)
                .originalName(file.getOriginalFilename() == null ? filename : file.getOriginalFilename())
                .url(url)
                .mimeType(type)
                .sizeKb(Math.max(1L, file.getSize() / 1024))
                .altText(altText)
                .isDeleted(false)
                .build();

        media = mediaRepository.save(media);
        return toResponse(media);
    }

    @Override
    public MediaResponse getMediaById(Long mediaId) {
        return toResponse(findMedia(mediaId));
    }

    @Override
    public List<MediaResponse> getMediaByUploader(Long uploaderId) {
        return mediaRepository.findByUploaderId(uploaderId).stream()
                .filter(media -> !media.getIsDeleted())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MediaResponse> getMediaByPost(Long postId) {
        return mediaRepository.findByLinkedPostId(postId).stream()
                .filter(media -> !media.getIsDeleted())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMedia(Long mediaId) {
        Media media = findMedia(mediaId);
        media.setIsDeleted(true);
        media.setLinkedPostId(null);
        mediaRepository.save(media);
    }

    @Override
    @Transactional
    public MediaResponse updateAltText(Long mediaId, UpdateAltTextRequest request) {
        Media media = findMedia(mediaId);
        media.setAltText(request.getAltText());
        media = mediaRepository.save(media);
        return toResponse(media);
    }

    @Override
    @Transactional
    public MediaResponse linkToPost(LinkPostRequest request) {
        Media media = findMedia(request.getMediaId());
        media.setLinkedPostId(request.getPostId());
        media = mediaRepository.save(media);
        return toResponse(media);
    }

    @Override
    @Transactional
    public MediaResponse unlinkFromPost(Long mediaId) {
        Media media = findMedia(mediaId);
        media.setLinkedPostId(null);
        media = mediaRepository.save(media);
        return toResponse(media);
    }

    @Override
    public List<MediaResponse> getAllMedia(boolean includeDeleted) {
        List<Media> mediaList = includeDeleted ? mediaRepository.findAll() : mediaRepository.findByIsDeleted(false);
        return mediaList.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public long cleanupDeleted() {
        List<Media> deleted = mediaRepository.findByIsDeleted(true);
        long size = deleted.size();
        mediaRepository.deleteAll(deleted);
        return size;
    }

    private Media findMedia(Long mediaId) {
        return mediaRepository.findByMediaId(mediaId).orElseThrow(() -> new IllegalArgumentException("Media not found"));
    }

    private String extension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot == -1 || dot == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(dot).toLowerCase();
    }

    private MediaResponse toResponse(Media media) {
        return MediaResponse.builder()
                .mediaId(media.getMediaId())
                .uploaderId(media.getUploaderId())
                .filename(media.getFilename())
                .originalName(media.getOriginalName())
                .url(media.getUrl())
                .mimeType(media.getMimeType())
                .sizeKb(media.getSizeKb())
                .altText(media.getAltText())
                .linkedPostId(media.getLinkedPostId())
                .uploadedAt(media.getUploadedAt())
                .deleted(media.getIsDeleted())
                .build();
    }
}
