package com.app.categoryservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.app.categoryservice.dto.CategoryRequest;
import com.app.categoryservice.dto.CategoryResponse;
import com.app.categoryservice.dto.PostTaxonomyRequest;
import com.app.categoryservice.dto.TagRequest;
import com.app.categoryservice.dto.TagResponse;
import com.app.categoryservice.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryResourceTest {

    @Mock
    private CategoryService categoryService;

    @Test
    void createCategoryRequiresAuthorOrAdmin() {
        CategoryResource resource = new CategoryResource(categoryService);
        CategoryRequest request = new CategoryRequest();
        request.setName("Technology");

        assertThatThrownBy(() -> resource.createCategory(request, "READER"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createAndDeleteTagDelegateToService() {
        CategoryResource resource = new CategoryResource(categoryService);
        TagRequest request = new TagRequest();
        request.setName("Java");
        when(categoryService.createTag(request)).thenReturn(TagResponse.builder().tagId(1L).build());

        resource.createTag(request, "AUTHOR");
        resource.deleteTag(1L, "ADMIN");

        verify(categoryService).createTag(request);
        verify(categoryService).deleteTag(1L);
    }

    @Test
    void taxonomyEndpointsDelegateToService() {
        CategoryResource resource = new CategoryResource(categoryService);
        PostTaxonomyRequest request = new PostTaxonomyRequest();
        request.setPostId(1L);
        request.setTaxonomyId(2L);
        when(categoryService.getCategoriesByPost(1L)).thenReturn(List.of());
        when(categoryService.getTagsByPost(1L)).thenReturn(List.of());

        resource.addCategoryToPost(request, "ADMIN");
        resource.removeTagFromPost(request, "AUTHOR");
        resource.getCategoriesByPost(1L);
        resource.getTagsByPost(1L);

        verify(categoryService).addCategoryToPost(request);
        verify(categoryService).removeTagFromPost(request);
    }

    @Test
    void readEndpointsDelegateToService() {
        CategoryResource resource = new CategoryResource(categoryService);
        when(categoryService.getAllCategories()).thenReturn(List.of(CategoryResponse.builder().categoryId(1L).build()));
        when(categoryService.getBySlug("tech")).thenReturn(CategoryResponse.builder().categoryId(2L).build());
        when(categoryService.getAllTags()).thenReturn(List.of(TagResponse.builder().tagId(3L).build()));
        when(categoryService.getTagBySlug("java")).thenReturn(TagResponse.builder().tagId(4L).build());
        when(categoryService.getTrendingTags()).thenReturn(List.of(TagResponse.builder().tagId(5L).build()));

        assertThat(resource.getAllCategories()).hasSize(1);
        assertThat(resource.getCategoryBySlug("tech").getCategoryId()).isEqualTo(2L);
        assertThat(resource.getAllTags()).hasSize(1);
        assertThat(resource.getTagBySlug("java").getTagId()).isEqualTo(4L);
        assertThat(resource.getTrendingTags()).hasSize(1);
    }

    @Test
    void adminEndpointsRequireAdminAndDelegate() {
        CategoryResource resource = new CategoryResource(categoryService);
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("Science");
        TagRequest tagRequest = new TagRequest();
        tagRequest.setName("Kotlin");

        when(categoryService.updateCategory(9L, categoryRequest))
                .thenReturn(CategoryResponse.builder().categoryId(9L).build());
        when(categoryService.updateTag(8L, tagRequest))
                .thenReturn(TagResponse.builder().tagId(8L).build());

        assertThatThrownBy(() -> resource.updateCategory(9L, categoryRequest, "AUTHOR"))
                .isInstanceOf(ResponseStatusException.class);

        assertThat(resource.updateCategory(9L, categoryRequest, "ADMIN").getCategoryId()).isEqualTo(9L);
        assertThat(resource.deleteCategory(9L, "ADMIN").get("message")).contains("deleted");
        assertThat(resource.updateTag(8L, tagRequest, "ADMIN").getTagId()).isEqualTo(8L);
        assertThat(resource.deleteTag(8L, "ADMIN").get("message")).contains("deleted");
    }

    @Test
    void taxonomyEndpointsReturnMessagesForAuthorRole() {
        CategoryResource resource = new CategoryResource(categoryService);
        PostTaxonomyRequest request = new PostTaxonomyRequest();
        request.setPostId(3L);
        request.setTaxonomyId(4L);

        assertThat(resource.addTagToPost(request, "AUTHOR").get("message")).contains("added");
        assertThat(resource.removeTagFromPost(request, "AUTHOR").get("message")).contains("removed");
        assertThat(resource.addCategoryToPost(request, "AUTHOR").get("message")).contains("added");
        assertThat(resource.removeCategoryFromPost(request, "AUTHOR").get("message")).contains("removed");
    }
}
