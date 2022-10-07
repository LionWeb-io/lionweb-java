package lioncore.java;

public interface NamespacedEntity {
    String getSimpleName();
    String qualifiedName();
    NamespaceProvider getContainer();
}
