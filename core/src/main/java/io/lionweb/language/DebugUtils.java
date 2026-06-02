package io.lionweb.language;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Static helpers for producing human-readable representations of language elements, safe to call
 * even when the element is in an inconsistent state (e.g. inside {@code toString} methods).
 */
public class DebugUtils {

  private DebugUtils() {
    // Prevent instantiation
  }

  /**
   * This variant of qualified name can be obtained also for invalid states. This is intended to be
   * used in methods which should not throw exceptions, like toString methods.
   */
  public static @Nonnull String qualifiedName(@Nonnull NamespacedEntity namespacedEntity) {
    Objects.requireNonNull(namespacedEntity, "namespacedEntity cannot be null");
    String qualifier = "<no language>";
    if (namespacedEntity.getContainer() != null) {
      if (namespacedEntity.getContainer().namespaceQualifier() != null) {
        qualifier = namespacedEntity.getContainer().namespaceQualifier();
      } else {
        qualifier = "<unnamed language>";
      }
    }
    String qualified = "<unnamed>";
    if (namespacedEntity.getName() != null) {
      qualified = namespacedEntity.getName();
    }
    return qualifier + "." + qualified;
  }
}
