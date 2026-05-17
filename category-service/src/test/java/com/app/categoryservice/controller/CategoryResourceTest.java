package com.app.categoryservice.controller;

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
void categoryReadEndpointsDelegateToService() {
    CategoryResource resource = new CategoryResource(categoryService);
    when(categoryService.getAllCategories()).thenReturn(List.of());
    when(categoryService.getBySlug("tech")).thenReturn(com.app.categoryservice.dto.CategoryResponse.builder().categoryId(1L).build());

    resource.getAllCategories();
    resource.getCategoryBySlug("tech");

    verify(categoryService).getAllCategories();
    verify(categoryService).getBySlug("tech");
}

@Test
void categoryAdminEndpointsRequireAdminAndDelegate() {
    CategoryResource resource = new CategoryResource(categoryService);

    CategoryRequest request = new CategoryRequest();
    request.setName("Tech");

    // forbidden
    assertThatThrownBy(() -> resource.updateCategory(1L, request, "AUTHOR"))
            .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> resource.deleteCategory(1L, "AUTHOR"))
            .isInstanceOf(ResponseStatusException.class);
// allowed
    resource.updateCategory(1L, request, "ADMIN");
    resource.deleteCategory(1L, "ADMIN");

    verify(categoryService).updateCategory(1L, request);
    verify(categoryService).deleteCategory(1L);
}

@Test
void tagReadAndUpdateEndpointsDelegateAndRequireRoles() {
    CategoryResource resource = new CategoryResource(categoryService);

    TagRequest tagRequest = new TagRequest();
    tagRequest.setName("Java");

    when(categoryService.getAllTags()).thenReturn(List.of());
    when(categoryService.getTagBySlug("java")).thenReturn(TagResponse.builder().tagId(10L).build());
    when(categoryService.getTrendingTags()).thenReturn(List.of());
    when(categoryService.getTagsByPost(5L)).thenReturn(List.of());

    // read endpoints
    resource.getAllTags();
    resource.getTagBySlug("java");
    resource.getTrendingTags();
    resource.getTagsByPost(5L);

    // update requires admin
    assertThatThrownBy(() -> resource.updateTag(10L, tagRequest, "AUTHOR"))
            .isInstanceOf(ResponseStatusException.class);
    resource.updateTag(10L, tagRequest, "ADMIN");

    verify(categoryService).getAllTags();
    verify(categoryService).getTagBySlug("java");
    verify(categoryService).getTrendingTags();
    verify(categoryService).getTagsByPost(5L);
     verify(categoryService).updateTag(10L, tagRequest);
    }
    
    @Test
    void postTaxonomyEndpointsRequireAuthorOrAdminAndDelegate() {
        CategoryResource resource = new CategoryResource(categoryService);
    
        PostTaxonomyRequest request = new PostTaxonomyRequest();
        request.setPostId(1L);
        request.setTaxonomyId(2L);
    
        // forbidden role
        assertThatThrownBy(() -> resource.addTagToPost(request, "READER"))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> resource.removeCategoryFromPost(request, "READER"))
                .isInstanceOf(ResponseStatusException.class);
    
        // allowed
        resource.addTagToPost(request, "AUTHOR");
        resource.removeTagFromPost(request, "ADMIN");
        resource.addCategoryToPost(request, "ADMIN");
        resource.removeCategoryFromPost(request, "AUTHOR");
    
        verify(categoryService).addTagToPost(request);
        verify(categoryService).removeTagFromPost(request);
        verify(categoryService).addCategoryToPost(request);
        verify(categoryService).removeCategoryFromPost(request);
        }


}


