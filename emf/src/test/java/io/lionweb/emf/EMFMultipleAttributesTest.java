package io.lionweb.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.LionWebVersion;
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the M1 mapping of EAttributes with upper bound greater than 1, which in LionWeb are
 * represented through a multiple Containment of auxiliary holder nodes.
 */
public class EMFMultipleAttributesTest {

  private EPackage ePackage;
  private EClass bookEClass;
  private EAttribute titleEAttribute;
  private EAttribute keywordsEAttribute;
  private EAttribute ratingsEAttribute;

  @BeforeEach
  public void setUp() {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    ePackage = factory.createEPackage();
    ePackage.setName("multiattrs");
    ePackage.setNsPrefix("multiattrs");
    ePackage.setNsURI("http://lionweb.io/test/multiattrs");

    bookEClass = factory.createEClass();
    bookEClass.setName("Book");
    ePackage.getEClassifiers().add(bookEClass);

    titleEAttribute = factory.createEAttribute();
    titleEAttribute.setName("title");
    titleEAttribute.setEType(EcorePackage.eINSTANCE.getEString());
    bookEClass.getEStructuralFeatures().add(titleEAttribute);

    keywordsEAttribute = factory.createEAttribute();
    keywordsEAttribute.setName("keywords");
    keywordsEAttribute.setEType(EcorePackage.eINSTANCE.getEString());
    keywordsEAttribute.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
    bookEClass.getEStructuralFeatures().add(keywordsEAttribute);

    ratingsEAttribute = factory.createEAttribute();
    ratingsEAttribute.setName("ratings");
    ratingsEAttribute.setEType(EcorePackage.eINSTANCE.getEInt());
    ratingsEAttribute.setLowerBound(1);
    ratingsEAttribute.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
    // Allow duplicated values, so that we can verify their order and number are preserved
    ratingsEAttribute.setUnique(false);
    bookEClass.getEStructuralFeatures().add(ratingsEAttribute);
  }

  private EObject createBook(String title, List<String> keywords, List<Integer> ratings) {
    EObject book = ePackage.getEFactoryInstance().create(bookEClass);
    book.eSet(titleEAttribute, title);
    book.eSet(keywordsEAttribute, keywords);
    book.eSet(ratingsEAttribute, ratings);
    return book;
  }

  @Test
  public void importMultipleAttributesAsHolderNodes() {
    Resource resource = new ResourceImpl();
    resource
        .getContents()
        .add(createBook("DSL Engineering", Arrays.asList("dsl", "mps"), Arrays.asList(5, 4, 5)));

    EMFModelImporter importer = new EMFModelImporter(LionWebVersion.v2023_1);
    importer.getNodeInstantiator().enableDynamicNodes();
    List<Node> nodes = importer.importResource(resource);

    assertEquals(1, nodes.size());
    Node book = nodes.get(0);
    assertEquals("Book", book.getClassifier().getName());
    assertEquals("DSL Engineering", ClassifierInstanceUtils.getPropertyValueByName(book, "title"));

    Containment keywordsContainment = book.getClassifier().requireContainmentByName("keywords");
    Concept keywordsHolder = (Concept) keywordsContainment.getType();
    assertEquals("KeywordsContainer", keywordsHolder.getName());
    List<? extends Node> keywords =
        ClassifierInstanceUtils.getChildrenByContainmentName(book, "keywords");
    assertEquals(2, keywords.size());
    for (Node keyword : keywords) {
      assertSame(keywordsHolder, keyword.getClassifier());
      assertSame(book, keyword.getParent());
    }
    assertEquals(
        Arrays.asList("dsl", "mps"),
        keywords.stream()
            .map(
                k ->
                    ClassifierInstanceUtils.getPropertyValueByName(
                        k, EMFMetamodelImporter.MULTIPLE_ATTRIBUTE_HOLDER_PROPERTY_NAME))
            .collect(Collectors.toList()));

    List<? extends Node> ratings =
        ClassifierInstanceUtils.getChildrenByContainmentName(book, "ratings");
    assertEquals(
        Arrays.asList(5, 4, 5),
        ratings.stream()
            .map(
                r ->
                    ClassifierInstanceUtils.getPropertyValueByName(
                        r, EMFMetamodelImporter.MULTIPLE_ATTRIBUTE_HOLDER_PROPERTY_NAME))
            .collect(Collectors.toList()));
  }

  @Test
  public void importEmptyMultipleAttribute() {
    Resource resource = new ResourceImpl();
    resource
        .getContents()
        .add(createBook("Empty", Collections.emptyList(), Collections.emptyList()));

    EMFModelImporter importer = new EMFModelImporter(LionWebVersion.v2023_1);
    importer.getNodeInstantiator().enableDynamicNodes();
    Node book = importer.importResource(resource).get(0);

    assertTrue(ClassifierInstanceUtils.getChildrenByContainmentName(book, "keywords").isEmpty());
    assertTrue(ClassifierInstanceUtils.getChildrenByContainmentName(book, "ratings").isEmpty());
  }

  @Test
  public void roundTripMultipleAttributes() {
    Resource original = new ResourceImpl();
    original
        .getContents()
        .add(createBook("DSL Engineering", Arrays.asList("dsl", "mps"), Arrays.asList(5, 4, 5)));
    original
        .getContents()
        .add(createBook("Empty", Collections.emptyList(), Collections.singletonList(3)));

    LanguageEntitiesToEElementsMapping mapping =
        new LanguageEntitiesToEElementsMapping(LionWebVersion.v2023_1);
    EMFModelImporter importer = new EMFModelImporter(mapping);
    importer.getNodeInstantiator().enableDynamicNodes();
    List<Node> nodes = importer.importResource(original);

    EMFModelExporter exporter = new EMFModelExporter(mapping);
    Resource exported = exporter.exportResource(nodes);

    assertEquals(2, exported.getContents().size());

    EObject book1 = exported.getContents().get(0);
    assertSame(bookEClass, book1.eClass());
    assertEquals("DSL Engineering", book1.eGet(titleEAttribute));
    assertEquals(Arrays.asList("dsl", "mps"), book1.eGet(keywordsEAttribute));
    assertEquals(Arrays.asList(5, 4, 5), book1.eGet(ratingsEAttribute));
    assertTrue(book1.eContents().isEmpty());

    EObject book2 = exported.getContents().get(1);
    assertSame(bookEClass, book2.eClass());
    assertEquals("Empty", book2.eGet(titleEAttribute));
    assertEquals(Collections.emptyList(), book2.eGet(keywordsEAttribute));
    assertEquals(Collections.singletonList(3), book2.eGet(ratingsEAttribute));
  }
}
