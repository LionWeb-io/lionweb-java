package io.lionweb.repoclient.api;

import java.io.IOException;
import java.util.Map;

public interface InspectionAPIClient {
  Map<ClassifierKey, ClassifierResult> nodesByClassifier() throws IOException;

  Map<String, ClassifierResult> nodesByLanguage() throws IOException;
}
