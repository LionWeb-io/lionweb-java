package io.lionweb.lioncore.java.model;

import java.util.List;

public interface Element extends HasFeatureValues {
  List<AnnotationInstance> getAnnotations();
}
