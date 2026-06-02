package io.lionweb.serialization;

import io.lionweb.language.Classifier;
import io.lionweb.serialization.data.MetaPointer;
import javax.annotation.Nonnull;

/** Interface for handling inconsistent data during serialization and deserialization. */
public interface InconsistentDataHandler {
  /**
   * Handles operations when a required property is missing during data processing.
   *
   * @param classifier The {@link Classifier} instance that provides classification logic for the
   *     data.
   * @param metaPointer The {@code MetaPointer} referencing the metadata or structure of the missing
   *     property.
   */
  void handleMissingProperty(@Nonnull Classifier<?> classifier, @Nonnull MetaPointer metaPointer);

  /**
   * Handles scenarios where a required classifier is missing during serialization or
   * deserialization.
   *
   * @param serializedClassifier The {@code MetaPointer} representing the metadata or structure of
   *     the missing classifier.
   * @param id A unique identifier associated with the classifier being processed.
   */
  void handleMissingClassifier(@Nonnull MetaPointer serializedClassifier, @Nonnull String id);
}
