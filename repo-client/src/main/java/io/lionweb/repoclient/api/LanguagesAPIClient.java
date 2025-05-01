package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.language.Language;
import javax.validation.constraints.NotNull;

import java.util.Collection;

public interface LanguagesAPIClient {
    void registerLanguages(@NotNull Collection<@NotNull Language> languages);
}
