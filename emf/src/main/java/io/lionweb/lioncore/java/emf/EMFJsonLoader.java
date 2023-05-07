package io.lionweb.lioncore.java.emf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;

public class EMFJsonLoader {
  public List<EObject> load(InputStream inputStream, Resource resource) {
    JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
    List<EObject> nodes = Collections.singletonList(readEObject(json, resource, null));
    resource.getContents().addAll(nodes);
    return nodes;
  }

  private EObject readEObject(JsonObject jsonObject, Resource resource, EClass expectedEClass) {
    Objects.requireNonNull(jsonObject);
    String eClassURI = null;
    if (jsonObject.has("eClass")) {
      eClassURI = jsonObject.get("eClass").getAsString();
    }
    EClass eClass;
    if (eClassURI == null) {
      if (expectedEClass == null) {
        throw new IllegalArgumentException();
      }
      eClass = expectedEClass;
    } else {
      String[] parts = eClassURI.split("#//");
      String packageURI = parts[0];
      String eClassName = parts[1];
      EPackage ePackage = resource.getResourceSet().getPackageRegistry().getEPackage(packageURI);
      if (ePackage == null) {
        throw new UnsupportedOperationException();
      }
      EClassifier eClassifier = ePackage.getEClassifier(eClassName);
      if (eClassifier == null) {
        throw new UnsupportedOperationException();
      }
      if (!(eClassifier instanceof EClass)) {
        throw new IllegalStateException();
      }
      eClass = (EClass) eClassifier;
    }
    EObject eObject = new DynamicEObjectImpl(eClass);
    eClass
        .getEAllStructuralFeatures()
        .forEach(
            eStructuralFeature -> {
              JsonElement jsonFeatureValue = jsonObject.get(eStructuralFeature.getName());
              if (jsonFeatureValue != null) {
                if (eStructuralFeature instanceof EAttribute) {
                  EAttribute eAttribute = (EAttribute) eStructuralFeature;
                  if (eAttribute.getEAttributeType().equals(EcorePackage.eINSTANCE.getEInt())) {
                    if (eStructuralFeature.isMany()) {
                      throw new UnsupportedOperationException();
                    } else {
                      eObject.eSet(
                          eStructuralFeature, Integer.parseInt(jsonFeatureValue.getAsString()));
                    }
                  } else if (eAttribute
                      .getEAttributeType()
                      .equals(EcorePackage.eINSTANCE.getEString())) {
                    if (eStructuralFeature.isMany()) {
                      throw new UnsupportedOperationException();
                    } else {
                      eObject.eSet(eStructuralFeature, jsonFeatureValue.getAsString());
                    }
                  } else {
                    throw new UnsupportedOperationException();
                  }
                } else if (eStructuralFeature instanceof EReference) {
                  EReference eReference = (EReference) eStructuralFeature;
                  if (eReference.isContainment()) {
                    if (eStructuralFeature.isMany()) {
                      List<EObject> children = new ArrayList<>();
                      jsonFeatureValue
                          .getAsJsonArray()
                          .forEach(
                              e ->
                                  children.add(
                                      readEObject(
                                          e.getAsJsonObject(),
                                          resource,
                                          ((EReference) eStructuralFeature).getEReferenceType())));
                      eObject.eSet(eStructuralFeature, children);
                    } else {
                      EObject child =
                          readEObject(
                              jsonFeatureValue.getAsJsonObject(),
                              resource,
                              ((EReference) eStructuralFeature).getEReferenceType());
                      eObject.eSet(eStructuralFeature, child);
                    }
                  } else {
                    throw new UnsupportedOperationException();
                  }
                } else {
                  throw new IllegalStateException();
                }
              }
            });
    return eObject;
  }
}
