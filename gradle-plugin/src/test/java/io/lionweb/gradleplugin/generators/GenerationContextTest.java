package io.lionweb.gradleplugin.generators;

import static org.junit.jupiter.api.Assertions.*;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;
import io.lionweb.LionWebVersion;
import io.lionweb.gradleplugin.CompanyLanguage;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.PrimitiveType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GenerationContextTest {

  @Test
  void generationPackage_returnsConfiguredPackage() {
    Language language = CompanyLanguage.getLanguage();
    GenerationContext context = new GenerationContext(language, "com.example.company");

    assertEquals("com.example.company", context.generationPackage(language));
  }

  @Test
  void hasOverridenName_returnsTrueWhenOverridePresent() {
    Language language = CompanyLanguage.getLanguage();
    Map<String, String> languageClassNames = new HashMap<>();
    languageClassNames.put(language.getID(), "CustomCompanyLanguage");
    GenerationContext context =
        new GenerationContext(
            language,
            "com.example.company",
            Collections.emptyMap(),
            languageClassNames,
            Collections.emptyMap());

    assertTrue(context.hasOverridenName(language));
    assertEquals("CustomCompanyLanguage", context.getOverriddenName(language));
  }

  @Test
  void hasOverridenName_returnsFalseWhenNoOverridePresent() {
    Language language = CompanyLanguage.getLanguage();
    GenerationContext context =
        new GenerationContext(
            language,
            "com.example.company",
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap());

    assertFalse(context.hasOverridenName(language));
    assertNull(context.getOverriddenName(language));
  }

  @Test
  void typeFor_usesPrimitiveTypeNameMappings() {
    PrimitiveType customPrimitive =
        new PrimitiveType(LionWebVersion.v2023_1, "custom.primitive.Instant");
    Map<String, String> primitiveTypes = new HashMap<>();
    primitiveTypes.put(customPrimitive.getID(), "java.time.Instant");
    GenerationContext context =
        new GenerationContext(
            CompanyLanguage.getLanguage(),
            "com.example.company",
            primitiveTypes,
            Collections.emptyMap(),
            Collections.emptyMap());

    assertEquals(ClassName.get("java.time", "Instant"), context.typeNameFor(customPrimitive));
  }

  @Test
  void typeNameFor_usesBuiltinsWhenNoPrimitiveMapping() {
    GenerationContext context =
        new GenerationContext(CompanyLanguage.getLanguage(), "com.example.company");

    assertEquals(
        ClassName.get(String.class),
        context.typeNameFor(LionCoreBuiltins.getString(LionWebVersion.v2023_1)));
    assertEquals(
        TypeName.INT, context.typeNameFor(LionCoreBuiltins.getInteger(LionWebVersion.v2023_1)));
  }

  @Test
  void typeNameFor_classifierUsesGeneratedLanguagePackage() {
    Language language = CompanyLanguage.getLanguage();
    GenerationContext context = new GenerationContext(language, "com.example.company");
    Concept company = CompanyLanguage.getCompany();

    assertEquals(ClassName.get("com.example.company", "Company"), context.typeNameFor(company));
  }

  @Test
  void typeFor_classifierUsesMappingsNameForExternalLanguage() {
    Language externalLanguage = new Language(LionWebVersion.v2023_1, "ExternalLang");
    Concept externalConcept =
        new Concept(externalLanguage, "ExternalConcept", "external.concept.id");
    String qualifiedName = externalConcept.qualifiedName();
    Map<String, String> mappings = new HashMap<>();
    mappings.put(qualifiedName, "com.example.external.ExternalConceptImpl");
    GenerationContext context =
        new GenerationContext(
            CompanyLanguage.getLanguage(),
            "com.example.company",
            Collections.emptyMap(),
            Collections.emptyMap(),
            mappings);

    assertEquals(
        ClassName.get("com.example.external", "ExternalConceptImpl"),
        context.typeNameFor(externalConcept));
  }
}
