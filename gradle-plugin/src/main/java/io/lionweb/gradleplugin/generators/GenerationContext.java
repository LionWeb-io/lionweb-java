package io.lionweb.gradleplugin.generators;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import io.lionweb.lioncore.LionCore;

import java.util.*;
import java.util.stream.Collectors;

import static io.lionweb.gradleplugin.generators.CommonClassNames.*;
import static io.lionweb.gradleplugin.generators.NamingUtils.capitalize;
import static io.lionweb.gradleplugin.generators.NamingUtils.toLanguageClassName;

/** It handles finding Language instances in the generated code. */
class GenerationContext {

    protected static class LanguageGenerationConfiguration {
        public Language language;
        public String generationPackage;

        public LanguageGenerationConfiguration(Language language, String generationPackage) {
            this.language = language;
            this.generationPackage = generationPackage;
        }
    }

    public Set<LanguageGenerationConfiguration> languageConfs;
    protected Map<String, String> primitiveTypes;

    public GenerationContext(Language language, String generationPackage) {
        this(new HashSet<>(Arrays.asList(new LanguageGenerationConfiguration(language, generationPackage))));
    }

    public GenerationContext(Set<LanguageGenerationConfiguration> languageConfs) {
        this(languageConfs, Collections.emptyMap());
    }

    public GenerationContext(Language language, String generationPackage, Map<String, String> primitiveTypes) {
        this(new HashSet<>(Arrays.asList(new LanguageGenerationConfiguration(language, generationPackage))), primitiveTypes);
    }

    public GenerationContext(Set<LanguageGenerationConfiguration> languageConfs, Map<String, String> primitiveTypes) {
        this.languageConfs = languageConfs;
        this.primitiveTypes = primitiveTypes;
    }

    public Set<Language> ambiguousLanguages() {
        Map<Language, String> languageToNames = new HashMap<>();
        this.languageConfs.forEach(
                languageConf ->
                        languageToNames.put(languageConf.language, languageConf.generationPackage + "." + toLanguageClassName(languageConf.language, null).toLowerCase()));
        Map<String, Long> nameCount =
                languageToNames.values().stream()
                        .collect(Collectors.groupingBy(name -> name, Collectors.counting()));
        return languageConfs.stream()
                .map(languageConf -> languageConf.language)
                .filter(language -> nameCount.get(languageToNames.get(language)) > 1)
                .collect(Collectors.toSet());
    }

    private boolean isGeneratedLanguage(Language language) {
        return languageConfs.stream().map(entry -> entry.language).anyMatch(l -> l.equals(language));
    }

    String generationPackage(Language language) {
        return languageConfs.stream().filter(entry -> entry.language.equals(language)).findFirst().get().generationPackage;
    };

    public CodeBlock resolveLanguage(Language language) {
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

    public TypeName getEnumerationTypeName(io.lionweb.language.Enumeration enumeration) {
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

    public String getGeneratedName(Interface interf) {
        return getGeneratedName(interf, true);
    }

    public String getGeneratedName(Interface interf, boolean versionedIfNecessary) {
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

    public String getGeneratedName(Concept concept) {
        return getGeneratedName(concept, true);
    }

    public String getGeneratedName(io.lionweb.language.Enumeration enumeration) {
        return getGeneratedName(enumeration, true);
    }

    public String getGeneratedName(io.lionweb.language.Enumeration enumeration, boolean versionedIfNecessary) {
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

    public String getGeneratedName(Concept concept, boolean versionedIfNecessary) {
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

    public TypeName getInterfaceType(Interface interf) {
        if (interf.equals(LionCoreBuiltins.getINamed(interf.getLionWebVersion()))) {
            return ClassName.get(INamed.class);
        } else if (isGeneratedLanguage(interf.getLanguage())) {
            return ClassName.get(generationPackage(interf.getLanguage()), getGeneratedName(interf));
        } else {
            throw new UnsupportedOperationException("Implemented interfaces are not yet implemented");
        }
    }

    public TypeName typeFor(DataType<?> dataType) {
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
            fieldType = getEnumerationTypeName((Enumeration) dataType);
        } else {
            throw new UnsupportedOperationException("Unknown data type: " + dataType);
        }
        return fieldType;
    }

    protected String primitiveTypeQName(String primitiveTypeID) {
        return primitiveTypes.getOrDefault(primitiveTypeID, null);
    }

}
