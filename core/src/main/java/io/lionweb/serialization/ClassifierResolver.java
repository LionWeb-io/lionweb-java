package io.lionweb.serialization;

import io.lionweb.language.Annotation;
import io.lionweb.language.Classifier;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.serialization.data.MetaPointer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * This class is responsible to resolve the Classifier associate with a given Classifier ID.
 *
 * <p>While initially just know classifiers which have been explicitly registered, in the future it
 * could adopt more advanced resolution strategies.
 */
public class ClassifierResolver {
  private final Map<MetaPointer, Concept> registeredConcepts = new HashMap<>();
  private final Map<MetaPointer, Annotation> registeredAnnotations = new HashMap<>();

  @Nonnull
  public Classifier<?> resolveClassifier(@Nonnull MetaPointer conceptMetaPointer) {
    if (registeredConcepts.containsKey(conceptMetaPointer)) {
      return registeredConcepts.get(conceptMetaPointer);
    } else if (registeredAnnotations.containsKey(conceptMetaPointer)) {
      return registeredAnnotations.get(conceptMetaPointer);
    } else {
      throw new RuntimeException(
          "Unable to resolve classifier with metaPointer " + conceptMetaPointer);
    }
  }

  @Nonnull
  public Concept resolveConcept(@Nonnull MetaPointer conceptMetaPointer) {
    if (registeredConcepts.containsKey(conceptMetaPointer)) {
      return registeredConcepts.get(conceptMetaPointer);
    } else {
      throw new RuntimeException(
          "Unable to resolve concept with metaPointer " + conceptMetaPointer);
    }
  }

  @Nonnull
  public Annotation resolveAnnotation(@Nonnull MetaPointer metaPointer) {
    if (registeredAnnotations.containsKey(metaPointer)) {
      return registeredAnnotations.get(metaPointer);
    } else {
      throw new RuntimeException("Unable to resolve annotation with metaPointer " + metaPointer);
    }
  }

  @Nonnull
  public ClassifierResolver registerLanguage(@Nonnull Language language) {
    language
        .getElements()
        .forEach(
            e -> {
              if (e instanceof Concept) {
                registerConcept((Concept) e);
              } else if (e instanceof Annotation) {
                registerAnnotation((Annotation) e);
              }
            });
    return this;
  }

  private void registerConcept(Concept concept) {
    registeredConcepts.put(MetaPointer.from(concept), concept);
  }

  private void registerAnnotation(Annotation annotation) {
    registeredAnnotations.put(MetaPointer.from(annotation), annotation);
  }
}
