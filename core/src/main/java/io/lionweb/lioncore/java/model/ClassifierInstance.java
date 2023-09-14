package io.lionweb.lioncore.java.model;

import java.util.List;

public interface ClassifierInstance extends HasFeatureValues {
  List<AnnotationInstance> getAnnotations();

  String getID();
}
