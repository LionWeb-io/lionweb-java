package io.lionweb.gradleplugin.generators;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import io.lionweb.lioncore.LionCore;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractJavaCodeGenerator {
  protected final @Nonnull File destinationDir;

  /** It handles finding Language instances in the generated code. */
  protected class LanguageContext {
    public String generationPackage;
    public List<Language> generatedLanguages;

    public LanguageContext(String generationPackage, List<Language> generatedLanguages) {
      this.generationPackage = generationPackage;
      this.generatedLanguages = generatedLanguages;
    }

    public Set<Language> ambiguousLanguages() {
      Map<Language, String> languageToNames = new HashMap<>();
      this.generatedLanguages.forEach(
          language ->
              languageToNames.put(language, toLanguageClassName(language, null).toLowerCase()));
      Map<String, Long> nameCount =
          languageToNames.values().stream()
              .collect(Collectors.groupingBy(name -> name, Collectors.counting()));
      return generatedLanguages.stream()
          .filter(language -> nameCount.get(languageToNames.get(language)) > 1)
          .collect(Collectors.toSet());
    }

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
        if (generatedLanguages.contains(language)) {
          return CodeBlock.of(
              "$T.getInstance()",
              ClassName.get(generationPackage, toLanguageClassName(language, this)));
        }
        throw new RuntimeException("Language not found: " + language.getName());
      }
    }

      public TypeName getEnumerationTypeName(Enumeration enumeration) {
        if (generatedLanguages.contains(enumeration.getLanguage())) {
            String name = capitalize(enumeration.getName());
            if (ambiguousLanguages().contains(enumeration.getLanguage())) {
                name += "V" + enumeration.getLanguage().getVersion();
            }
            return ClassName.get(generationPackage, name);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
      }

      public String getGeneratedName(Interface interf) {
          if (generatedLanguages.contains(interf.getLanguage())) {
              String interfName = capitalize(interf.getName());
              if (ambiguousLanguages().contains(interf.getLanguage())) {
                  interfName += "V" + interf.getLanguage().getVersion();
              }
              return interfName;
          } else {
              throw new IllegalArgumentException("Interface not found: " + interf.getName());
          }
      }

      public String getGeneratedName(Concept concept) {
          if (generatedLanguages.contains(concept.getLanguage())) {
              String interfName = capitalize(concept.getName());
              if (ambiguousLanguages().contains(concept.getLanguage())) {
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
          } else if (generatedLanguages.contains(interf.getLanguage())) {
              return ClassName.get(generationPackage, getGeneratedName(interf));
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
              fieldType = ClassName.get(int.class);
          } else if (dataType instanceof Enumeration) {
              fieldType = getEnumerationTypeName((Enumeration)dataType);
          } else {
              throw new UnsupportedOperationException("Unknown data type: " + dataType);
          }
          return fieldType;
      }

      protected String primitiveTypeQName(String primitiveTypeID) {
          return primitiveTypes.getOrDefault(primitiveTypeID, null);
      }
  }

    protected  Map<String, String> primitiveTypes;

  /**
   * Constructs an AbstractJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  protected AbstractJavaCodeGenerator(@Nonnull File destinationDir, Map<String, String> primitiveTypes) {
    Objects.requireNonNull(destinationDir, "destinationDir should not be null");
    this.destinationDir = destinationDir;
    this.primitiveTypes = primitiveTypes;
  }

  protected static final List<String> JAVA_KEYWORDS =
      Arrays.asList(
          "abstract",
          "assert",
          "boolean",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "class",
          "const",
          "continue",
          "default",
          "do",
          "double",
          "else",
          "enum",
          "extends",
          "final",
          "finally",
          "float",
          "for",
          "goto",
          "if",
          "implements",
          "import",
          "instanceof",
          "int",
          "interface",
          "long",
          "native",
          "new",
          "package",
          "private",
          "protected",
          "public",
          "return",
          "short",
          "static",
          "strictfp",
          "super",
          "switch",
          "synchronized",
          "this",
          "throw",
          "throws",
          "transient",
          "try",
          "void",
          "volatile",
          "while");

  protected String toVariableName(String name) {
    String res = name.replaceAll("[^a-zA-Z0-9]", "_");
    if (JAVA_KEYWORDS.contains(res)) {
      return "_" + res;
    } else {
      return res;
    }
  }

  protected static String toLanguageClassName(
      Language language, @Nullable LanguageContext languageContext) {
    Objects.requireNonNull(language, "language should not be null");
    Objects.requireNonNull(language.getName(), "language.getName() should not be null");
    String[] parts = language.getName().split("\\.");
    String s = capitalize(parts[parts.length - 1]) + "Language";
    if (languageContext != null && languageContext.ambiguousLanguages().contains(language)) {
      s = s + "V" + language.getVersion();
    }
    s = s.replaceAll(" ", "");
    s = s.replaceAll("\\.", "_");
    return s;
  }

  protected static final ClassName lionCore = ClassName.get(LionCore.class);
  protected static final ClassName lionCoreBuiltins = ClassName.get(LionCoreBuiltins.class);
  protected static final ClassName lionWebVersion = ClassName.get(LionWebVersion.class);
  protected static final ClassName conceptClass = ClassName.get(Concept.class);
  protected static final ClassName interfaceClass = ClassName.get(Interface.class);
  protected static final ClassName primitiveType = ClassName.get(PrimitiveType.class);
  protected static final ClassName annotationDefClass = ClassName.get(Annotation.class);

  protected static String capitalize(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

    protected String primitiveTypeQName(String primitiveTypeID) {
        return primitiveTypes.getOrDefault(primitiveTypeID, null);
    }
}
