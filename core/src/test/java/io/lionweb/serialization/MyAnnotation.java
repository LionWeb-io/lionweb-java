package io.lionweb.serialization;

import io.lionweb.language.*;
import io.lionweb.model.impl.DynamicAnnotationInstance;

public class MyAnnotation extends DynamicAnnotationInstance {
  public static final Language LANGUAGE =
      new Language()
          .setID("myLanguageWithAnnotations-id")
          .setKey("myLanguageWithAnnotations-key")
          .setName("LWA")
          .setVersion("1");
  public static final Annotation ANNOTATION =
      new Annotation()
          .setID("MyAnnotation-id")
          .setKey("MyAnnotation-key")
          .setName("MyAnnotation")
          .setParent(LANGUAGE);
  public static final Concept VALUE =
      new Concept().setID("Value-id").setKey("Value-key").setName("Value").setParent(LANGUAGE);
  public static final Concept ANNOTATED =
      new Concept()
          .setID("Annotated-id")
          .setKey("Annotated-key")
          .setName("Annotated")
          .setParent(LANGUAGE);

  static {
    VALUE.addFeature(
        Property.createRequired("amount", LionCoreBuiltins.getInteger())
            .setKey("my-amount")
            .setID("my-amount"));
    ANNOTATION.setAnnotates(ANNOTATED);
    ANNOTATION.addFeature(
        Containment.createMultiple("values", VALUE).setKey("my-values").setID("my-values"));
    LANGUAGE.addElement(ANNOTATION);
    LANGUAGE.addElement(VALUE);
    LANGUAGE.addElement(ANNOTATED);
  }

  public MyAnnotation(String id) {
    super(id, ANNOTATION);
  }
}
