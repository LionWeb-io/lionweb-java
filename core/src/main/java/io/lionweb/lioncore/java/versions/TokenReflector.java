package io.lionweb.lioncore.java.versions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class TokenReflector {

    private static Optional<LionWebVersionToken> retrieveLionWebVersionToken(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (LionWebVersionToken.class.isAssignableFrom(clazz)) {
                try {
                    Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
                    Object invoked = getInstanceMethod.invoke(clazz);
                    if (invoked instanceof LionWebVersionToken) {
                        return Optional.of((LionWebVersionToken) invoked);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    // (can only happen if Vyyyy_1 classes have changed)
                    throw new RuntimeException("couldn't retrieve LionWeb version token", e);
                }
            }
        }
        return Optional.empty();
    }

    private static <T, R> Optional<R> firstNonEmptyMappedFrom(T[] ts, Function<T, Optional<R>> mapper) {
        return Arrays.stream(ts)
                .map(mapper)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    private static Optional<LionWebVersion> retrieveLionWebVersion(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() == LionWebVersionDependent.class) {
                return firstNonEmptyMappedFrom(parameterizedType.getActualTypeArguments(), TokenReflector::retrieveLionWebVersionToken)
                        .map(LionWebVersionToken::getVersion);
            }
        }
        return Optional.empty();
    }

    public static <V extends LionWebVersionToken> Optional<LionWebVersion> retrieveLionWebVersion(LionWebVersionDependent<V> dependentInstance) {
        return firstNonEmptyMappedFrom(dependentInstance.getClass().getGenericInterfaces(), TokenReflector::retrieveLionWebVersion);
    }

}