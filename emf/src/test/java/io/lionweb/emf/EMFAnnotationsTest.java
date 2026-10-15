package io.lionweb.emf;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.emf.support.AnnotationsEncoding;
import io.lionweb.language.*;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.junit.jupiter.api.Test;

public class EMFAnnotationsTest {

  private Language language;
  private Concept library;
  private Concept book;
  private Containment libraryBooks;
  private Property bookTitle;
  private Annotation comment;
  private Property commentText;
  private Annotation signedComment;
  private Property signedCommentAuthor;
  private Annotation approval;

  private void createLanguage() {
    language = new Language("annotated", "annotated", "annotated", "1");

    library = new Concept(language, "Library", "annotated-Library", "annotated-Library");
    book = new Concept(language, "Book", "annotated-Book", "annotated-Book");
    bookTitle = Property.createRequired("title", LionCoreBuiltins.getString(), "annotated-title");
    bookTitle.setKey("annotated-title");
    book.addFeature(bookTitle);
    libraryBooks = Containment.createMultiple("books", book, "annotated-books");
    libraryBooks.setKey("annotated-books");
    library.addFeature(libraryBooks);

    comment = new Annotation(language, "Comment", "annotated-Comment", "annotated-Comment");
    comment.setAnnotates(book);
    commentText = Property.createRequired("text", LionCoreBuiltins.getString(), "annotated-text");
    commentText.setKey("annotated-text");
    comment.addFeature(commentText);

    signedComment =
        new Annotation(
            language, "SignedComment", "annotated-SignedComment", "annotated-SignedComment");
    signedComment.setExtendedAnnotation(comment);
    signedCommentAuthor =
        Property.createOptional("author", LionCoreBuiltins.getString(), "annotated-author");
    signedCommentAuthor.setKey("annotated-author");
    signedComment.addFeature(signedCommentAuthor);

    // This annotation does not specify what it annotates, so it can annotate anything
    approval = new Annotation(language, "Approval", "annotated-Approval", "annotated-Approval");
  }

  private EClass getEClass(EPackage ePackage, String name) {
    return (EClass) ePackage.getEClassifier(name);
  }

  @Test
  public void exportLanguageWithAnnotations() {
    createLanguage();
    EPackage ePackage = new EMFMetamodelExporter().exportLanguage(language);

    EClass bookEClass = getEClass(ePackage, "Book");
    assertFalse(AnnotationsEncoding.isAnnotation(bookEClass));

    EClass commentEClass = getEClass(ePackage, "Comment");
    assertTrue(AnnotationsEncoding.isAnnotation(commentEClass));
    assertFalse(commentEClass.isInterface());
    assertFalse(commentEClass.isAbstract());
    assertEquals(
        Collections.singletonList(bookEClass),
        commentEClass.getEAnnotation(AnnotationsEncoding.ANNOTATION_SOURCE).getReferences());
    assertEquals(Collections.emptyList(), commentEClass.getESuperTypes());
    assertEquals(2, commentEClass.getEStructuralFeatures().size());
    EReference annotated = AnnotationsEncoding.getAnnotatedEReference(commentEClass);
    assertNotNull(annotated);
    assertSame(commentEClass, annotated.getEContainingClass());
    assertFalse(annotated.isContainment());
    assertFalse(annotated.isMany());
    assertEquals(bookEClass, annotated.getEType());
    EAttribute text = (EAttribute) commentEClass.getEStructuralFeature("text");
    assertEquals(EcorePackage.eINSTANCE.getEString(), text.getEType());

    EClass signedCommentEClass = getEClass(ePackage, "SignedComment");
    assertTrue(AnnotationsEncoding.isAnnotation(signedCommentEClass));
    assertEquals(
        Collections.emptyList(),
        signedCommentEClass.getEAnnotation(AnnotationsEncoding.ANNOTATION_SOURCE).getReferences());
    assertEquals(Collections.singletonList(commentEClass), signedCommentEClass.getESuperTypes());
    assertEquals(1, signedCommentEClass.getEStructuralFeatures().size());
    assertNotNull(signedCommentEClass.getEStructuralFeature("author"));
    // The reference to the annotated element is inherited
    assertSame(annotated, AnnotationsEncoding.getAnnotatedEReference(signedCommentEClass));

    EClass approvalEClass = getEClass(ePackage, "Approval");
    assertTrue(AnnotationsEncoding.isAnnotation(approvalEClass));
    assertEquals(
        EcorePackage.eINSTANCE.getEObject(),
        AnnotationsEncoding.getAnnotatedEReference(approvalEClass).getEType());
  }

  @Test
  public void exportAnnotationWithReservedFeatureName() {
    createLanguage();
    Property annotatedProperty =
        Property.createRequired("annotated", LionCoreBuiltins.getString(), "annotated-annotated");
    annotatedProperty.setKey("annotated-annotated");
    approval.addFeature(annotatedProperty);
    assertThrows(
        UnsupportedOperationException.class,
        () -> new EMFMetamodelExporter().exportLanguage(language));
  }

  @Test
  public void roundtripLanguageWithAnnotationsThroughEcore() throws IOException {
    createLanguage();
    EPackage ePackage = new EMFMetamodelExporter().exportLanguage(language);

    // We save and reload the EPackage, to verify the encoding survives serialization
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("ecore", new EcoreResourceFactoryImpl());
    Resource resource = resourceSet.createResource(URI.createURI("annotated.ecore"));
    resource.getContents().add(ePackage);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    resource.save(outputStream, new HashMap<>());

    ResourceSet reloadResourceSet = new ResourceSetImpl();
    reloadResourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("ecore", new EcoreResourceFactoryImpl());
    Resource reloaded = reloadResourceSet.createResource(URI.createURI("annotated.ecore"));
    reloaded.load(new ByteArrayInputStream(outputStream.toByteArray()), new HashMap<>());

    List<Language> languages = new EMFMetamodelImporter().importResource(reloaded);
    assertEquals(1, languages.size());
    Language imported = languages.get(0);
    assertEquals(5, imported.getElements().size());

    Concept importedBook = imported.getConceptByName("Book");
    assertNotNull(importedBook);

    Annotation importedComment = (Annotation) imported.getElementByName("Comment");
    assertSame(importedBook, importedComment.getAnnotates());
    assertNull(importedComment.getExtendedAnnotation());
    assertEquals(1, importedComment.getFeatures().size());
    Property importedText = (Property) importedComment.getFeatureByName("text");
    assertEquals(LionCoreBuiltins.getString(), importedText.getType());
    assertFalse(importedText.isOptional());

    Annotation importedSignedComment = (Annotation) imported.getElementByName("SignedComment");
    assertNull(importedSignedComment.getAnnotates());
    assertSame(importedComment, importedSignedComment.getExtendedAnnotation());
    assertSame(importedBook, importedSignedComment.getEffectivelyAnnotated());
    assertEquals(1, importedSignedComment.getFeatures().size());
    assertTrue(importedSignedComment.getFeatureByName("author").isOptional());

    Annotation importedApproval = (Annotation) imported.getElementByName("Approval");
    assertNull(importedApproval.getAnnotates());
    assertEquals(0, importedApproval.getFeatures().size());
  }

  private Node createModel() {
    DynamicNode lib = new DynamicNode("lib", library);
    DynamicNode book1 = new DynamicNode("book1", book);
    ClassifierInstanceUtils.setPropertyValueByName(book1, "title", "First book");
    DynamicNode book2 = new DynamicNode("book2", book);
    ClassifierInstanceUtils.setPropertyValueByName(book2, "title", "Second book");
    lib.addChild(libraryBooks, book1);
    lib.addChild(libraryBooks, book2);

    DynamicAnnotationInstance comment1 = new DynamicAnnotationInstance("c1", comment, book1);
    ClassifierInstanceUtils.setPropertyValueByName(comment1, "text", "Nice");
    DynamicAnnotationInstance comment2 = new DynamicAnnotationInstance("c2", signedComment, book2);
    ClassifierInstanceUtils.setPropertyValueByName(comment2, "text", "Boring");
    ClassifierInstanceUtils.setPropertyValueByName(comment2, "author", "John");
    new DynamicAnnotationInstance("a1", approval, comment2);
    new DynamicAnnotationInstance("a2", approval, lib);
    return lib;
  }

  @Test
  public void exportModelWithAnnotations() {
    createLanguage();
    Node lib = createModel();

    Resource resource = new EMFModelExporter().exportResource(Collections.singletonList(lib));
    assertEquals(5, resource.getContents().size());

    EObject libEObject = resource.getContents().get(0);
    assertEquals("Library", libEObject.eClass().getName());
    List<EObject> books =
        (List<EObject>) libEObject.eGet(libEObject.eClass().getEStructuralFeature("books"));
    assertEquals(2, books.size());
    EObject book1EObject = books.get(0);
    EObject book2EObject = books.get(1);

    EObject comment1EObject = resource.getContents().get(1);
    assertEquals("Comment", comment1EObject.eClass().getName());
    assertEquals(
        "Nice", comment1EObject.eGet(comment1EObject.eClass().getEStructuralFeature("text")));
    assertSame(
        book1EObject,
        comment1EObject.eGet(AnnotationsEncoding.getAnnotatedEReference(comment1EObject.eClass())));

    EObject comment2EObject = resource.getContents().get(2);
    assertEquals("SignedComment", comment2EObject.eClass().getName());
    assertEquals(
        "Boring", comment2EObject.eGet(comment2EObject.eClass().getEStructuralFeature("text")));
    assertEquals(
        "John", comment2EObject.eGet(comment2EObject.eClass().getEStructuralFeature("author")));
    assertSame(
        book2EObject,
        comment2EObject.eGet(AnnotationsEncoding.getAnnotatedEReference(comment2EObject.eClass())));

    EObject approval1EObject = resource.getContents().get(3);
    assertEquals("Approval", approval1EObject.eClass().getName());
    assertSame(
        comment2EObject,
        approval1EObject.eGet(
            AnnotationsEncoding.getAnnotatedEReference(approval1EObject.eClass())));

    EObject approval2EObject = resource.getContents().get(4);
    assertEquals("Approval", approval2EObject.eClass().getName());
    assertSame(
        libEObject,
        approval2EObject.eGet(
            AnnotationsEncoding.getAnnotatedEReference(approval2EObject.eClass())));
  }

  @Test
  public void exportModelWithRegisteredAnnotationPackage() {
    createLanguage();
    EPackage ePackage = new EMFMetamodelExporter().exportLanguage(language);
    EPackage.Registry registry = new EPackageRegistryImpl();
    registry.put(ePackage.getNsURI(), ePackage);
    LanguageEntitiesToEElementsMapping mapping = new LanguageEntitiesToEElementsMapping();
    mapping.setEPackageRegistry(registry);

    Resource resource =
        new EMFModelExporter(mapping).exportResource(Collections.singletonList(createModel()));

    assertEquals(5, resource.getContents().size());
    for (EObject eObject : resource.getContents()) {
      assertSame(ePackage, eObject.eClass().getEPackage());
    }
    assertSame(comment, mapping.getCorrespondingAnnotation(getEClass(ePackage, "Comment")));
    assertSame(
        signedComment, mapping.getCorrespondingAnnotation(getEClass(ePackage, "SignedComment")));
    assertSame(approval, mapping.getCorrespondingAnnotation(getEClass(ePackage, "Approval")));
    assertSame(ePackage, mapping.getKnownEPackage(language));
  }

  @Test
  public void roundtripModelWithAnnotations() {
    createLanguage();
    Node lib = createModel();

    LanguageEntitiesToEElementsMapping mapping = new LanguageEntitiesToEElementsMapping();
    Resource resource =
        new EMFModelExporter(mapping).exportResource(Collections.singletonList(lib));

    EMFModelImporter importer = new EMFModelImporter(mapping);
    importer.getNodeInstantiator().enableDynamicNodes();
    List<Node> roots = importer.importResource(resource);
    assertEquals(1, roots.size());

    Node importedLib = roots.get(0);
    assertSame(library, importedLib.getClassifier());
    assertEquals(1, importedLib.getAnnotations().size());
    assertSame(approval, importedLib.getAnnotations().get(0).getAnnotationDefinition());
    assertSame(importedLib, importedLib.getAnnotations().get(0).getParent());

    List<? extends Node> importedBooks =
        ClassifierInstanceUtils.getChildrenByContainmentName(importedLib, "books");
    assertEquals(2, importedBooks.size());

    Node importedBook1 = importedBooks.get(0);
    assertEquals(1, importedBook1.getAnnotations().size());
    AnnotationInstance importedComment1 = importedBook1.getAnnotations().get(0);
    assertSame(comment, importedComment1.getAnnotationDefinition());
    assertSame(importedBook1, importedComment1.getParent());
    assertEquals("Nice", ClassifierInstanceUtils.getPropertyValueByName(importedComment1, "text"));
    assertEquals(0, importedComment1.getAnnotations().size());

    Node importedBook2 = importedBooks.get(1);
    assertEquals(1, importedBook2.getAnnotations().size());
    AnnotationInstance importedComment2 = importedBook2.getAnnotations().get(0);
    assertSame(signedComment, importedComment2.getAnnotationDefinition());
    assertEquals(
        "Boring", ClassifierInstanceUtils.getPropertyValueByName(importedComment2, "text"));
    assertEquals(
        "John", ClassifierInstanceUtils.getPropertyValueByName(importedComment2, "author"));
    assertEquals(1, importedComment2.getAnnotations().size());
    AnnotationInstance importedApproval = importedComment2.getAnnotations().get(0);
    assertSame(approval, importedApproval.getAnnotationDefinition());
    assertSame(importedComment2, importedApproval.getParent());
  }
}
