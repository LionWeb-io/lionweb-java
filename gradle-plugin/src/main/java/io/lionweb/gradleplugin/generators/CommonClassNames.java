package io.lionweb.gradleplugin.generators;

import com.palantir.javapoet.ClassName;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;

/**
 * A utility class providing static references to commonly used class names. This class contains
 * predefined instances of {@code ClassName} for specific classes. It simplifies access to
 * frequently referenced classes in the application, ensuring they are consistently used across the
 * codebase.
 */
class CommonClassNames {
  static final ClassName lionCore = ClassName.get(LionCore.class);
  static final ClassName lionCoreBuiltins = ClassName.get(LionCoreBuiltins.class);
  static final ClassName lionWebVersion = ClassName.get(LionWebVersion.class);
  static final ClassName conceptClass = ClassName.get(Concept.class);
  static final ClassName interfaceClass = ClassName.get(Interface.class);
  static final ClassName primitiveTypeClass = ClassName.get(PrimitiveType.class);
  static final ClassName enumerationClass = ClassName.get(Enumeration.class);
  static final ClassName enumerationLiteralClass = ClassName.get(EnumerationLiteral.class);
  static final ClassName annotationDefClass = ClassName.get(Annotation.class);
}
