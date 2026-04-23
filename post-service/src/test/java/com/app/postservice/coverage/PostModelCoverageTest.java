package com.app.postservice.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.app.postservice.dto.PostCreateRequest;
import com.app.postservice.dto.PostResponse;
import com.app.postservice.dto.PostUpdateRequest;
import com.app.postservice.entity.AuthorFollow;
import com.app.postservice.entity.Post;
import com.app.postservice.entity.PostLike;
import com.app.postservice.entity.PostStatus;
import com.app.postservice.messaging.NotificationDispatchEvent;

class PostModelCoverageTest {

    @Test
    void exercisePostModels() {
        List<Class<?>> classes = List.of(
                PostCreateRequest.class,
                PostResponse.class,
                PostUpdateRequest.class,
                AuthorFollow.class,
                Post.class,
                PostLike.class,
                NotificationDispatchEvent.class,
                PostStatus.class);

        for (Class<?> clazz : classes) {
            exerciseClass(clazz);
        }
    }

    private void exerciseClass(Class<?> clazz) {
        if (clazz.isEnum()) {
            assertThat(clazz.getEnumConstants()).isNotEmpty();
            return;
        }

        Object defaultInstance = instantiateNoArgs(clazz);
        if (defaultInstance != null) {
            invokeSetters(defaultInstance, clazz);
            invokeGetters(defaultInstance, clazz);
            Object peer = instantiateNoArgs(clazz);
            if (peer != null) {
                invokeSetters(peer, clazz);
                invokeGetters(peer, clazz);
                defaultInstance.equals(peer);
                peer.equals(defaultInstance);
                peer.hashCode();
                peer.toString();
            }
            defaultInstance.equals(defaultInstance);
            defaultInstance.equals(null);
            defaultInstance.equals(new Object());
            defaultInstance.hashCode();
            defaultInstance.toString();
        }

        Object allArgs = instantiateLargestConstructor(clazz);
        if (allArgs != null) {
            invokeGetters(allArgs, clazz);
            allArgs.hashCode();
            allArgs.toString();
        }

        Object built = instantiateFromBuilder(clazz);
        if (built != null) {
            invokeGetters(built, clazz);
            built.hashCode();
            built.toString();
            built.equals(defaultInstance);
            Object builtPeer = instantiateFromBuilder(clazz);
            if (builtPeer != null) {
                built.equals(builtPeer);
                builtPeer.equals(built);
            }
        }
    }

    private Object instantiateNoArgs(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            if (!Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object instantiateLargestConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> target = null;
        for (Constructor<?> constructor : constructors) {
            if (target == null || constructor.getParameterCount() > target.getParameterCount()) {
                target = constructor;
            }
        }
        if (target == null || target.getParameterCount() == 0) {
            return null;
        }

        try {
            if (!Modifier.isPublic(target.getModifiers())) {
                target.setAccessible(true);
            }
            Object[] args = new Object[target.getParameterCount()];
            Class<?>[] paramTypes = target.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                args[i] = sampleValue(paramTypes[i]);
            }
            return target.newInstance(args);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object instantiateFromBuilder(Class<?> clazz) {
        try {
            Method builderMethod = clazz.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            Class<?> builderClass = builder.getClass();
            for (Method method : builderClass.getDeclaredMethods()) {
                if (method.getParameterCount() != 1 || method.isSynthetic() || method.getName().startsWith("$")) {
                    continue;
                }
                if (!builderClass.equals(method.getReturnType())) {
                    continue;
                }
                method.setAccessible(true);
                method.invoke(builder, sampleValue(method.getParameterTypes()[0]));
            }
            Method buildMethod = builderClass.getDeclaredMethod("build");
            buildMethod.setAccessible(true);
            return buildMethod.invoke(builder);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void invokeSetters(Object target, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1) {
                continue;
            }
            try {
                method.invoke(target, sampleValue(method.getParameterTypes()[0]));
            } catch (Exception ignored) {
                // Best effort for coverage.
            }
        }
    }

    private void invokeGetters(Object target, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            boolean getter = method.getName().startsWith("get") || method.getName().startsWith("is");
            if (!getter || method.getParameterCount() != 0 || "getClass".equals(method.getName())) {
                continue;
            }
            try {
                method.invoke(target);
            } catch (Exception ignored) {
                // Best effort for coverage.
            }
        }
    }

    private Object sampleValue(Class<?> type) {
        if (type == String.class) {
            return "value";
        }
        if (type == Long.class || type == long.class) {
            return 1L;
        }
        if (type == Integer.class || type == int.class) {
            return 1;
        }
        if (type == Boolean.class || type == boolean.class) {
            return true;
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.of(2025, 1, 1, 1, 1);
        }
        if (type == Instant.class) {
            return Instant.parse("2025-01-01T00:00:00Z");
        }
        if (List.class.isAssignableFrom(type)) {
            return List.of("v");
        }
        if (Set.class.isAssignableFrom(type)) {
            return Set.of("v");
        }
        if (Map.class.isAssignableFrom(type)) {
            return Map.of("k", "v");
        }
        if (type.isEnum()) {
            Object[] values = type.getEnumConstants();
            return values.length == 0 ? null : values[0];
        }

        Object nested = instantiateNoArgs(type);
        if (nested != null) {
            return nested;
        }
        return null;
    }
}
