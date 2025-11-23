package io.lionweb.gradleplugin.generators;

import com.palantir.javapoet.ClassName;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;

public class CommonClassNames {
    protected static final ClassName lionCore = ClassName.get(LionCore.class);
    protected static final ClassName lionCoreBuiltins = ClassName.get(LionCoreBuiltins.class);
    protected static final ClassName lionWebVersion = ClassName.get(LionWebVersion.class);
    protected static final ClassName conceptClass = ClassName.get(Concept.class);
    protected static final ClassName interfaceClass = ClassName.get(Interface.class);
    protected static final ClassName primitiveType = ClassName.get(PrimitiveType.class);
    protected static final ClassName annotationDefClass = ClassName.get(Annotation.class);

}
