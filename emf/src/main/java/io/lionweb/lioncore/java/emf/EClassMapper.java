package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.Concept;
import io.lionweb.lioncore.java.metamodel.Metamodel;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

public class EClassMapper {

  private Map<EPackage, Metamodel> ePackagesToMetamodels = new HashMap<>();
  private Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private EcoreImporter ecoreImporter = new EcoreImporter();

  private void processEPackage(EPackage ePackage) {
    Metamodel metamodel = ecoreImporter.importEPackage(ePackage);
    ePackagesToMetamodels.put(ePackage, metamodel);
    ePackage
        .eAllContents()
        .forEachRemaining(
            eObject -> {
              if (eObject instanceof EClass) {
                EClass eClass = (EClass) eObject;
                if (!eClass.isInterface()) {
                  eClassesToConcepts.put(eClass, metamodel.getConceptByName(eClass.getName()));
                }
              }
            });
  }

  public Concept getCorrespondingConcept(EClass eClass) {
    if (!eClassesToConcepts.containsKey(eClass)) {
      if (ePackagesToMetamodels.containsKey(eClass.getEPackage())) {
        throw new IllegalStateException();
      }
      processEPackage(eClass.getEPackage());
    }
    if (!eClassesToConcepts.containsKey(eClass)) {
      throw new IllegalStateException();
    }
    return eClassesToConcepts.get(eClass);
  }
}
