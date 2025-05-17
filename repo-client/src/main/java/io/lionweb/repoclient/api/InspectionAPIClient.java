package io.lionweb.repoclient.api;

import java.io.IOException;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public interface InspectionAPIClient {
  default Map<ClassifierKey, ClassifierResult> nodesByClassifier() throws IOException {
    return nodesByClassifier(null);
  }

  Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit)
      throws IOException;

  default Map<String, ClassifierResult> nodesByLanguage() throws IOException {
    return nodesByLanguage(null);
  }

  Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) throws IOException;
}
