package io.lionweb.language;

import java.util.Objects;
import javax.annotation.Nonnull;

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
