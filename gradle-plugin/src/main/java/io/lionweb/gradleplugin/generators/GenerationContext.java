package io.lionweb.gradleplugin.generators;

import static io.lionweb.gradleplugin.generators.CommonClassNames.*;
import static io.lionweb.gradleplugin.generators.NamingUtils.*;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.Node;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides a context for language generation, containing configurations for language generation and
 * primitive type mappings. This context is used to manage and resolve language-related information,
 * and to generate names and types for elements such as enumerations, concepts, and interfaces.
 */
class GenerationContext {

  private final Set<LanguageGenerationConfiguration> languageConfs;
  private final Map<String, String> primitiveTypes;
  private final Map<String, String> mappings;

  /**
   * Constructs a new {@code GenerationContext} instance with the specified set of language
   * generation configurations.
   *
   * @param languageConfs a set of {@link LanguageGenerationConfiguration} instances representing
   *     the configurations for language generation; must not be null.
   * @throws NullPointerException if the provided {@code languageConfs} is null.
   */
  GenerationContext(@Nonnull Set<LanguageGenerationConfiguration> languageConfs) {
    this(languageConfs, Collections.emptyMap(), Collections.emptyMap());
  }

  /**
   * Constructs a new {@code GenerationContext} instance with the specified language, generation
   * package, primitive type mappings, language class names, and additional mappings.
   *
   * @param language the programming language for which the context is being generated; must not be
   *     null.
   * @param generationPackage the base package name where the generated classes will reside; must
   *     not be null.
   * @param primitiveTypes a map where the keys are primitive type identifiers and the values are
   *     their corresponding fully qualified names; must not be null.
   * @param languageClassNames a map where the keys are language IDs and the values are the
   *     associated language class names; must not be null.
   * @param mappings a map containing additional key-value pairs to be used during code generation;
   *     must not be null.
   * @throws NullPointerException if any of the provided parameters is null.
   */
  GenerationContext(
      @Nonnull Language language,
      @Nonnull String generationPackage,
      @Nonnull Map<String, String> primitiveTypes,
      @Nonnull Map<String, String> languageClassNames,
      @Nonnull Map<String, String> mappings) {
    this(
        new HashSet<>(
            Arrays.asList(
                new LanguageGenerationConfiguration(
                    language, generationPackage, languageClassNames.get(language.getID())))),
        primitiveTypes,
        mappings);
  }

  /**
   * Constructs a new {@code GenerationContext} instance with the specified language and generation
   * package, using default empty mappings for primitive types, language class names, and additional
   * mappings.
   *
   * @param language the programming language for which the context is being generated; must not be
   *     null
   * @param generationPackage the base package name where the generated classes will reside; must
   *     not be null
   * @throws NullPointerException if either the {@code language} or {@code generationPackage} is
   *     null
   */
  GenerationContext(@Nonnull Language language, @Nonnull String generationPackage) {
    this(
        language,
        generationPackage,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  /**
   * Constructs a new {@code GenerationContext} instance with the specified configurations,
   * primitive type mappings, and additional mappings.
   *
   * @param languageConfs a set of {@link LanguageGenerationConfiguration} instances representing
   *     the configurations for language generation; must not be null.
   * @param primitiveTypes a map where the keys are primitive type identifiers and the values are
   *     their respective qualified names; must not be null.
   * @param mappings a map containing additional key-value pairs used during generation; must not be
   *     null.
   * @throws NullPointerException if any of the provided parameters is null.
   */
  GenerationContext(
      @Nonnull Set<LanguageGenerationConfiguration> languageConfs,
      @Nonnull Map<String, String> primitiveTypes,
      @Nonnull Map<String, String> mappings) {
    Objects.requireNonNull(languageConfs, "languageConfs should not be null");
    Objects.requireNonNull(primitiveTypes, "primitiveTypes should not be null");
    this.languageConfs = languageConfs;
    this.primitiveTypes = primitiveTypes;
    this.mappings = mappings;
  }

  /**
   * Determines whether the given {@code language} has an overridden name defined in the associated
   * language generation configurations.
   *
   * @param language the language to check for an overridden name; must not be null
   * @return {@code true} if the specified language has an overridden class name in the language
   *     generation configurations, otherwise {@code false}
   */
  public boolean hasOverridenName(@Nonnull Language language) {
    Objects.requireNonNull(language, "language should not be null");
    LanguageGenerationConfiguration languageGenerationConfiguration =
        languageConfs.stream()
            .filter(entry -> entry.language.equals(language))
            .findFirst()
            .orElse(null);
    if (languageGenerationConfiguration == null) {
      return false;
    }
    return languageGenerationConfiguration.overriddenClassName != null;
  }

  /**
   * Retrieves the overridden name associated with the specified language by looking up the
   * corresponding language generation configuration.
   *
   * @param language the language for which the overridden name is to be retrieved; must not be null
   * @return the overridden name defined in the language generation configuration for the specified
   *     language
   * @throws NullPointerException if the provided {@code language} is null
   * @throws IllegalArgumentException if no configuration for the specified {@code language} exists
   */
  public @Nonnull String getOverriddenName(@Nonnull Language language) {
    Objects.requireNonNull(language, "language should not be null");
    LanguageGenerationConfiguration languageGenerationConfiguration =
        languageConfs.stream()
            .filter(entry -> entry.language.equals(language))
            .findFirst()
            .orElse(null);
    if (languageGenerationConfiguration == null) {
      throw new IllegalArgumentException("Language not generated: " + language.getName());
    }
    return languageGenerationConfiguration.overriddenClassName;
  }

  Set<Language> ambiguousLanguages() {
    Map<Language, String> languageToNames = new HashMap<>();
    this.languageConfs.forEach(
        languageConf ->
            languageToNames.put(
                languageConf.language,
                languageConf.generationPackage
                    + "."
                    + toLanguageClassName(languageConf.language, null).toLowerCase()));
    Map<String, Long> nameCount =
        languageToNames.values().stream()
            .collect(Collectors.groupingBy(name -> name, Collectors.counting()));
    return languageConfs.stream()
        .map(languageConf -> languageConf.language)
        .filter(language -> nameCount.get(languageToNames.get(language)) > 1)
        .collect(Collectors.toSet());
  }

  /**
   * Resolves the appropriate {@link CodeBlock} representation for a given language in the context
   * of code generation.
   *
   * @param language the language to resolve; must not be null
   * @param languageBeingGenerated the language currently being generated; must not be null
   * @return a {@link CodeBlock} representing the resolved language in the generation process
   * @throws RuntimeException if the language is not found or is unsupported
   */
  CodeBlock resolveLanguage(Language language, Language languageBeingGenerated) {
    if (language.equals(languageBeingGenerated)) {
      return CodeBlock.of("this");
    }
    if (language.equals(LionCoreBuiltins.getInstance(LionWebVersion.v2023_1))) {
      return CodeBlock.of("$T.getInstance($T.v2023_1)", lionCoreBuiltins, lionWebVersion);
    } else if (language.equals(LionCoreBuiltins.getInstance(LionWebVersion.v2024_1))) {
      return CodeBlock.of("$T.getInstance($T.v2024_1)", lionCoreBuiltins, lionWebVersion);
    } else if (language.equals(LionCore.getInstance(LionWebVersion.v2023_1))) {
      return CodeBlock.of("$T.getInstance($T.v2023_1)", lionCore, lionWebVersion);
    } else if (language.equals(LionCore.getInstance(LionWebVersion.v2024_1))) {
      return CodeBlock.of("$T.getInstance($T.v2024_1)", lionCore, lionWebVersion);
    } else {
      if (isGeneratedLanguage(language)) {
        return CodeBlock.of(
            "$T.getInstance()",
            ClassName.get(generationPackage(language), toLanguageClassName(language, this)));
      }
      throw new RuntimeException("Language not found: " + language.getName());
    }
  }

  String getGeneratedName(Interface interf) {
    return getGeneratedName(interf, true);
  }

  String getGeneratedName(Classifier<?> classifier) {
    if (classifier instanceof Concept) {
      return getGeneratedName((Concept) classifier);
    } else if (classifier instanceof Interface) {
      return getGeneratedName((Interface) classifier);
    }
    throw new UnsupportedOperationException("Not yet implemented");
  }

  String getGeneratedName(Interface interf, boolean versionedIfNecessary) {
    if (isGeneratedLanguage(interf.getLanguage())) {
      String interfName = capitalize(interf.getName());
      if (versionedIfNecessary && ambiguousLanguages().contains(interf.getLanguage())) {
        interfName += "V" + interf.getLanguage().getVersion();
      }
      return interfName;
    } else {
      throw new IllegalArgumentException("Interface not found: " + interf.getName());
    }
  }

  String getGeneratedName(Concept concept) {
    return getGeneratedName(concept, true);
  }

  String getGeneratedName(io.lionweb.language.Enumeration enumeration) {
    return getGeneratedName(enumeration, true);
  }

  String getGeneratedName(Enumeration enumeration, boolean versionedIfNecessary) {
    if (isGeneratedLanguage(enumeration.getLanguage())) {
      String interfName = capitalize(enumeration.getName());
      if (versionedIfNecessary && ambiguousLanguages().contains(enumeration.getLanguage())) {
        interfName += "V" + enumeration.getLanguage().getVersion();
      }
      return interfName;
    } else {
      throw new IllegalArgumentException("Enumeration not found: " + enumeration.getName());
    }
  }

  String getGeneratedName(Concept concept, boolean versionedIfNecessary) {
    if (isGeneratedLanguage(concept.getLanguage())) {
      String interfName = capitalize(concept.getName());
      if (versionedIfNecessary && ambiguousLanguages().contains(concept.getLanguage())) {
        interfName += "V" + concept.getLanguage().getVersion();
      }
      return interfName;
    } else {
      throw new IllegalArgumentException("Concept not found: " + concept.getName());
    }
  }

  TypeName typeNameFor(Interface interf) {
    if (interf.equals(LionCoreBuiltins.getINamed(interf.getLionWebVersion()))) {
      return ClassName.get(INamed.class);
    } else if (isGeneratedLanguage(interf.getLanguage())) {
      return ClassName.get(generationPackage(interf.getLanguage()), getGeneratedName(interf));
    } else {
      throw new UnsupportedOperationException("Implemented interfaces are not yet implemented");
    }
  }

  TypeName getConceptType(Concept concept) {
    if (isGeneratedLanguage(concept.getLanguage())) {
      return ClassName.get(generationPackage(concept.getLanguage()), getGeneratedName(concept));
    } else {
      throw new UnsupportedOperationException("Extended concepts are not yet implemented");
    }
  }

  TypeName typeNameFor(DataType<?> dataType) {
    TypeName fieldType;
    String mappedQName = this.primitiveTypeQName(dataType.getID());
    int index = mappedQName == null ? -1 : mappedQName.lastIndexOf(".");
    String _packageName = index == -1 ? null : mappedQName.substring(0, index);
    String _simpleName = index == -1 ? mappedQName : mappedQName.substring(index + 1);
    if (mappedQName != null) {
      fieldType = ClassName.get(_packageName, _simpleName);
    } else if (dataType.equals(LionCoreBuiltins.getString(dataType.getLionWebVersion()))) {
      fieldType = ClassName.get(String.class);
    } else if (dataType.equals(LionCoreBuiltins.getInteger(dataType.getLionWebVersion()))) {
      fieldType = TypeName.INT;
    } else if (dataType instanceof io.lionweb.language.Enumeration) {
      fieldType = typeNameFor((Enumeration) dataType);
    } else {
      throw new UnsupportedOperationException("Unknown data type: " + dataType + ". Qualified name: " + this.primitiveTypeQName(dataType.getID()));
    }
    return fieldType;
  }

  TypeName typeNameFor(Classifier<?> classifier) {
    if (isGeneratedLanguage(classifier.getLanguage())) {
      return ClassName.get(
          generationPackage(classifier.getLanguage()), getGeneratedName(classifier));
    } else if (classifier.equals(LionCoreBuiltins.getNode(classifier.getLionWebVersion()))) {
      return TypeName.get(Node.class);
    } else if (mappings.containsKey(classifier.qualifiedName())) {
      String mappedTypeName = mappings.get(classifier.qualifiedName());
      String packageName = mappedTypeName.substring(0, mappedTypeName.lastIndexOf('.'));
      String simpleName = mappedTypeName.substring(mappedTypeName.lastIndexOf('.') + 1);
      return ClassName.get(packageName, simpleName);
    } else {
      throw new UnsupportedOperationException("Not yet implemented: " + classifier.qualifiedName());
    }
  }

  /**
   * Generates a {@link TypeName} for a given {@link Enumeration}.
   *
   * <p>The method constructs a fully qualified {@link TypeName} by combining the generation package
   * of the associated language and a formatted name derived from the enumeration’s name. If the
   * language of the enumeration is ambiguous, its version is appended to the name. Throws an {@link
   * UnsupportedOperationException} if the enumeration is not in a generated language.
   *
   * @param enumeration the {@link Enumeration} for which the {@link TypeName} is to be created;
   *     must not be null
   * @return the constructed {@link TypeName} representing the fully qualified name of the
   *     enumeration
   * @throws UnsupportedOperationException if the enumeration's language is not supported for
   *     generation
   */
  TypeName typeNameFor(io.lionweb.language.Enumeration enumeration) {
    if (isGeneratedLanguage(enumeration.getLanguage())) {
      String name = capitalize(enumeration.getName());
      if (ambiguousLanguages().contains(enumeration.getLanguage())) {
        name += "V" + enumeration.getLanguage().getVersion();
      }
      return ClassName.get(generationPackage(enumeration.getLanguage()), name);
    } else {
      throw new UnsupportedOperationException("Not yet implemented");
    }
  }

  String primitiveTypeQName(String primitiveTypeID) {
    return primitiveTypes.getOrDefault(primitiveTypeID, null);
  }

  String generationPackage(Language language) {
    return languageConfs.stream()
        .filter(entry -> entry.language.equals(language))
        .findFirst()
        .get()
        .generationPackage;
  }

  private boolean isGeneratedLanguage(Language language) {
    return languageConfs.stream().map(entry -> entry.language).anyMatch(l -> l.equals(language));
  }

  /**
   * Represents the configuration required for language generation. This class is used to specify
   * details about a programming language and the associated package information, along with an
   * optional custom class name override for the generated output.
   */
  static class LanguageGenerationConfiguration {
    final @Nonnull Language language;
    final @Nonnull String generationPackage;
    final @Nullable String overriddenClassName;

    LanguageGenerationConfiguration(
        @Nonnull Language language, @Nonnull String generationPackage, @Nullable String className) {
      Objects.requireNonNull(language, "language should not be null");
      Objects.requireNonNull(generationPackage, "generationPackage should not be null");
      this.language = language;
      this.generationPackage = generationPackage;
      this.overriddenClassName = className;
    }

    LanguageGenerationConfiguration(@Nonnull Language language, @Nonnull String generationPackage) {
      this(language, generationPackage, null);
    }
  }
}
