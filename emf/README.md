# EMF

Provides converters between LionCore/LionWeb and Eclipse Modeling Framework (EMF/Ecore).

## Features

- **`EMFMetamodelExporter`** — converts a LionCore language definition to an Ecore `EPackage`
- **`EMFMetamodelImporter`** — converts an Ecore `EPackage` to a LionCore language definition
- **`EMFModelExporter`** — converts a LionWeb model (set of nodes) to EMF `EObject` instances
- **`EMFModelImporter`** — converts EMF `EObject` instances to LionWeb nodes

## Usage

```java
// Export a LionCore language to Ecore
EMFMetamodelExporter exporter = new EMFMetamodelExporter();
EPackage ePackage = exporter.exportLanguage(myLanguage);

// Import an Ecore EPackage as a LionCore language
EMFMetamodelImporter importer = new EMFMetamodelImporter();
Language language = importer.importEPackage(ePackage);
```

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-emf:$lionwebVersion")
}
```

## Notes

Some LionCore built-in elements have no direct Ecore equivalent. The `emf-builtins` module
provides the companion `EPackage` (`http://lionweb.io/lionweb-java/emf/core/builtins/2023.1`)
that fills this gap.
