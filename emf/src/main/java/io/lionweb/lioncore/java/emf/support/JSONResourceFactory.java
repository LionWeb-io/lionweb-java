package io.lionweb.lioncore.java.emf.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

/**
 * We have issues using the JSON resource factory part of EMF-Cloud, so we provide our own
 * implementation of the logic to load Resources stored as JSON.
 */
public class JSONResourceFactory implements Resource.Factory {
  @Override
  public Resource createResource(URI uri) {
    return new ResourceImpl() {
      @Override
      protected void doLoad(InputStream inputStream, Map<?, ?> options) {
        loadIntoResource(inputStream, this);
      }
    };
  }

  // private JsonElement jsonRoot;

  private static class ReferencePostponer {

    private final EPackage.Registry packagesRegistry;

    public ReferencePostponer(EPackage.Registry packagesRegistry) {
      this.packagesRegistry = packagesRegistry;
    }

    private static class PostponedReference {
      final EObject container;
      final EStructuralFeature eStructuralFeature;
      final List<String> refs;

      public PostponedReference(
          EObject container, EStructuralFeature eStructuralFeature, String ref) {
        this.container = container;
        this.eStructuralFeature = eStructuralFeature;
        this.refs = Collections.singletonList(ref);
      }

      public PostponedReference(
          EObject container, EStructuralFeature eStructuralFeature, List<String> refs) {
        this.container = container;
        this.eStructuralFeature = eStructuralFeature;
        this.refs = refs;
      }
    }

    private final List<PostponedReference> postponedReferences = new ArrayList<>();

    public void considerReferences(List<EObject> theseNodes) {
      postponedReferences.forEach(
          postponedReference -> {
            if (postponedReference.eStructuralFeature.isMany()) {
              List<EObject> referred =
                  postponedReference.refs.stream()
                      .map(ref -> resolve(ref, theseNodes))
                      .collect(Collectors.toList());
              postponedReference.container.eSet(postponedReference.eStructuralFeature, referred);
            } else {
              if (postponedReference.refs.size() != 1) {
                throw new IllegalStateException();
              }
              EObject referred = resolve(postponedReference.refs.get(0), theseNodes);
              postponedReference.container.eSet(postponedReference.eStructuralFeature, referred);
            }
          });
    }

    private EObject resolve(String ref, List<EObject> theseNodes) {
      String[] parts = ref.split("#/");
      if (parts.length == 2 && packagesRegistry.containsKey(parts[0])) {
        EPackage ePackage = packagesRegistry.getEPackage(parts[0]);
        return resolve(ePackage, parts[1], theseNodes);
      } else if (ref.startsWith("/")) {
        String[] subparts = ref.substring(1).split("/");
        int index = Integer.parseInt(subparts[0]);
        EObject root = theseNodes.get(index);
        if (subparts.length == 1) {
          return root;
        } else {
          return resolve(root, ref.substring(("/" + subparts[0]).length()), theseNodes);
        }
      } else {
        throw new UnsupportedOperationException();
      }
    }

    private EObject resolve(EObject container, String path, List<EObject> theseNodes) {
      if (!path.startsWith("/")) {
        throw new IllegalStateException();
      }
      String[] parts = path.substring(1).split("/");
      if (parts.length == 1) {
        List<EObject> matching =
            container.eContents().stream()
                .filter(
                    child -> {
                      EStructuralFeature nameFeature = child.eClass().getEStructuralFeature("name");
                      if (nameFeature != null) {
                        return Objects.equals(child.eGet(nameFeature), path.substring(1));
                      } else {
                        return false;
                      }
                    })
                .collect(Collectors.toList());
        if (matching.size() != 1) {
          throw new IllegalStateException(
              "Matching " + matching + " for path " + path + " in " + container);
        }
        return matching.get(0);
      } else {
        String firstElementPath = "/" + parts[0];
        EObject nextContainer = resolve(container, firstElementPath, theseNodes);
        String nextPath = path.substring(firstElementPath.length());
        return resolve(nextContainer, nextPath, theseNodes);
      }
    }

    public void recordReference(
        EObject container, EStructuralFeature eStructuralFeature, String ref) {
      postponedReferences.add(new PostponedReference(container, eStructuralFeature, ref));
    }

    public void recordReference(
        EObject container, EStructuralFeature eStructuralFeature, List<String> refs) {
      postponedReferences.add(new PostponedReference(container, eStructuralFeature, refs));
    }
  }

  private List<EObject> loadIntoResource(InputStream inputStream, Resource resource) {
    JsonElement jsonRoot = JsonParser.parseReader(new InputStreamReader(inputStream));
    ReferencePostponer referencePostponer =
        new ReferencePostponer(resource.getResourceSet().getPackageRegistry());
    List<EObject> nodes;
    if (jsonRoot.isJsonObject()) {
      nodes =
          Collections.singletonList(
              readEObject(
                  jsonRoot.getAsJsonObject(),
                  null,
                  resource.getResourceSet().getPackageRegistry(),
                  referencePostponer));
    } else if (jsonRoot.isJsonArray()) {
      nodes = new ArrayList<>();
      jsonRoot
          .getAsJsonArray()
          .forEach(
              jsonElement ->
                  nodes.add(
                      readEObject(
                          jsonElement.getAsJsonObject(),
                          null,
                          resource.getResourceSet().getPackageRegistry(),
                          referencePostponer)));
    } else {
      throw new UnsupportedOperationException();
    }
    resource.getContents().addAll(nodes);
    referencePostponer.considerReferences(nodes);
    return nodes;
  }

  //  private JsonElement navigateJson(JsonElement container, String path) {
  //    throw new UnsupportedOperationException();
  //  }
  //
  //  private EObject resolveReference(JsonElement container, String path) {
  //    if (path.startsWith("/")) {
  //      path = path.substring(1);
  //    }
  //    String[] parts = path.split("/");
  //    if (parts.length == 1) {
  //      Pattern digits = Pattern.compile("\\d+");
  //      if (digits.matcher(parts[0]).matches()) {
  //        //return resource.getContents().get(Integer.parseInt(parts[0]));
  //        throw new UnsupportedOperationException();
  //      } else {
  //        throw new UnsupportedOperationException();
  //      }
  //    } else {
  //      JsonElement nextContainer = navigateJson(jsonRoot, parts[0]);
  //      return resolveReference(nextContainer, path.substring(("/" + parts[0]).length()));
  //    }
  //  }
  //
  //  private EObject resolveReference(String ref, EPackage.Registry packagesRegistry) {
  //    String[] parts = ref.split("#//");
  //    if (parts.length == 2) {
  //      EPackage ePackage = packagesRegistry.getEPackage(parts[0]);
  //      if (ePackage == null) {
  //        throw new IllegalStateException("Unable to resolve package " + parts[0]);
  //      } else {
  //        if (parts[1].contains("/")) {
  //          throw new UnsupportedOperationException();
  //        } else {
  //          Optional<EClassifier> resolved = ePackage.getEClassifiers().stream().filter(e ->
  // e.getName().equals(parts[1])).findFirst();
  //          if (resolved.isPresent()) {
  //            return resolved.get();
  //          } else {
  //            throw new IllegalStateException("Cannot find " + parts[1] + " in package " +
  // ePackage);
  //          }
  //        }
  //      }
  //    } else if (parts.length == 1) {
  //      String[] subparts = parts[0].split("/");
  //      return resolveReference(parts[0]);
  //    } else {
  //      throw new UnsupportedOperationException();
  //    }
  //  }

  private void readEAttribute(
      EObject eObject, EAttribute eAttribute, JsonElement jsonFeatureValue) {
    if (eAttribute.getEAttributeType().equals(EcorePackage.eINSTANCE.getEInt())) {
      if (eAttribute.isMany()) {
        throw new UnsupportedOperationException();
      } else {
        eObject.eSet(eAttribute, Integer.parseInt(jsonFeatureValue.getAsString()));
      }
    } else if (eAttribute.getEAttributeType().equals(EcorePackage.eINSTANCE.getEString())) {
      if (eAttribute.isMany()) {
        throw new UnsupportedOperationException();
      } else {
        eObject.eSet(eAttribute, jsonFeatureValue.getAsString());
      }
    } else if (eAttribute.getEAttributeType().equals(EcorePackage.eINSTANCE.getEBoolean())) {
      if (eAttribute.isMany()) {
        throw new UnsupportedOperationException();
      } else {
        eObject.eSet(eAttribute, Boolean.parseBoolean(jsonFeatureValue.getAsString()));
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private void readEReferenceContainment(
      EObject eObject,
      EReference eReference,
      JsonElement jsonFeatureValue,
      EPackage.Registry packagesRegistry,
      ReferencePostponer referencePostponer) {
    if (eReference.isMany()) {
      List<EObject> children = new ArrayList<>();
      jsonFeatureValue
          .getAsJsonArray()
          .forEach(
              e ->
                  children.add(
                      readEObject(
                          e.getAsJsonObject(),
                          eReference.getEReferenceType(),
                          packagesRegistry,
                          referencePostponer)));
      eObject.eSet(eReference, children);
    } else {
      EObject child =
          readEObject(
              jsonFeatureValue.getAsJsonObject(),
              eReference.getEReferenceType(),
              packagesRegistry,
              referencePostponer);
      eObject.eSet(eReference, child);
    }
  }

  private void readEReferenceNonContainment(
      EObject eObject,
      EReference eReference,
      JsonElement jsonFeatureValue,
      EPackage.Registry packagesRegistry,
      ReferencePostponer referencePostponer) {
    if (eReference.isMany()) {
      List<String> refs = new ArrayList<>();
      jsonFeatureValue
          .getAsJsonArray()
          .forEach(e -> refs.add(e.getAsJsonObject().get("$ref").getAsString()));
      referencePostponer.recordReference(eObject, eReference, refs);
    } else {
      if (jsonFeatureValue.isJsonObject()) {
        String ref = jsonFeatureValue.getAsJsonObject().get("$ref").getAsString();
        referencePostponer.recordReference(eObject, eReference, ref);
        // EObject referred = resolveReference(ref, resource.getResourceSet().getPackageRegistry());
        // eObject.eSet(eStructuralFeature, referred);
      } else {
        throw new UnsupportedOperationException(
            "Non-containment EReferences are not yet supported");
      }
    }
  }

  private EObject readEObject(
      JsonObject jsonObject,
      EClass expectedEClass,
      EPackage.Registry packagesRegistry,
      ReferencePostponer referencePostponer) {
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
      EPackage ePackage = packagesRegistry.getEPackage(packageURI);
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
    EObject eObject = eClass.getEPackage().getEFactoryInstance().create(eClass);
    eClass
        .getEAllStructuralFeatures()
        .forEach(
            eStructuralFeature -> {
              JsonElement jsonFeatureValue = jsonObject.get(eStructuralFeature.getName());
              if (jsonFeatureValue != null) {
                if (eStructuralFeature instanceof EAttribute) {
                  readEAttribute(eObject, (EAttribute) eStructuralFeature, jsonFeatureValue);
                } else if (eStructuralFeature instanceof EReference) {
                  EReference eReference = (EReference) eStructuralFeature;
                  if (eReference.isContainment()) {
                    readEReferenceContainment(
                        eObject,
                        eReference,
                        jsonFeatureValue,
                        packagesRegistry,
                        referencePostponer);
                  } else {
                    readEReferenceNonContainment(
                        eObject,
                        eReference,
                        jsonFeatureValue,
                        packagesRegistry,
                        referencePostponer);
                  }
                } else {
                  throw new IllegalStateException();
                }
              }
            });
    return eObject;
  }
}
