package io.lionweb.lioncore.java.language;

public class DebugUtils {

  private DebugUtils() {
    // Prevent instantiation
  }

  /**
   * This variant of qualified name can be obtained also for invalid states. This is intended to be
   * used in methods which should not throw exceptions, like toString methods.
   */
  public static String qualifiedName(NamespacedEntity namespacedEntity) {
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
    String qn = qualifier + "." + qualified;
    return qn;
  }
}
