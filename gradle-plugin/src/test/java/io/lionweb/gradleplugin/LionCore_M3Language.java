package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.language.Interface;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import io.lionweb.lioncore.LionCore;

public class LionCore_M3Language extends Language {
  private static LionCore_M3Language INSTANCE;

  private LionCore_M3Language() {
    super(LionWebVersion.v2023_1);
    this.setName("LionCore_M3");
    this.setVersion("2023.1");
    this.setID("-id-LionCore-M3");
    this.setKey("LionCore-M3");
    createElements();
    initAnnotation();
    initConcept();
    initInterface();
    initContainment();
    initDataType();
    initEnumeration();
    initEnumerationLiteral();
    initFeature();
    initClassifier();
    initLink();
    initLanguage();
    initLanguageEntity();
    initPrimitiveType();
    initProperty();
    initReference();
    initIKeyed();
  }

  public static LionCore_M3Language getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new LionCore_M3Language();
    }
    return INSTANCE;
  }

  public Concept getAnnotation() {
    return this.requireConceptByName("Annotation");
  }

  private void initAnnotation() {
    Concept concept = this.requireConceptByName("Annotation");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Classifier"));
    Reference annotates = new Reference("annotates", concept, "-id-Annotation-annotates");
    annotates.setKey("Annotation-annotates");
    annotates.setType(this.requireClassifierByName("Classifier"));
    annotates.setOptional(true);
    annotates.setMultiple(false);
    Reference _extends = new Reference("extends", concept, "-id-Annotation-extends");
    _extends.setKey("Annotation-extends");
    _extends.setType(this.requireClassifierByName("Annotation"));
    _extends.setOptional(true);
    _extends.setMultiple(false);
    Reference _implements = new Reference("implements", concept, "-id-Annotation-implements");
    _implements.setKey("Annotation-implements");
    _implements.setType(this.requireClassifierByName("Interface"));
    _implements.setOptional(true);
    _implements.setMultiple(true);
  }

  public Concept getConcept() {
    return this.requireConceptByName("Concept");
  }

  private void initConcept() {
    Concept concept = this.requireConceptByName("Concept");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Classifier"));
    Property _abstract = new Property("abstract", concept, "-id-Concept-abstract");
    _abstract.setKey("Concept-abstract");
    _abstract.setType(LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1));
    _abstract.setOptional(false);
    Property partition = new Property("partition", concept, "-id-Concept-partition");
    partition.setKey("Concept-partition");
    partition.setType(LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1));
    partition.setOptional(false);
    Reference _extends = new Reference("extends", concept, "-id-Concept-extends");
    _extends.setKey("Concept-extends");
    _extends.setType(this.requireClassifierByName("Concept"));
    _extends.setOptional(true);
    _extends.setMultiple(false);
    Reference _implements = new Reference("implements", concept, "-id-Concept-implements");
    _implements.setKey("Concept-implements");
    _implements.setType(this.requireClassifierByName("Interface"));
    _implements.setOptional(true);
    _implements.setMultiple(true);
  }

  public Concept getInterface() {
    return this.requireConceptByName("Interface");
  }

  private void initInterface() {
    Concept concept = this.requireConceptByName("Interface");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Classifier"));
    Reference _extends = new Reference("extends", concept, "-id-Interface-extends");
    _extends.setKey("Interface-extends");
    _extends.setType(this.requireClassifierByName("Interface"));
    _extends.setOptional(true);
    _extends.setMultiple(true);
  }

  public Concept getContainment() {
    return this.requireConceptByName("Containment");
  }

  private void initContainment() {
    Concept concept = this.requireConceptByName("Containment");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Link"));
  }

  public Concept getDataType() {
    return this.requireConceptByName("DataType");
  }

  private void initDataType() {
    Concept concept = this.requireConceptByName("DataType");
    concept.setAbstract(true);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("LanguageEntity"));
  }

  public Concept getEnumeration() {
    return this.requireConceptByName("Enumeration");
  }

  private void initEnumeration() {
    Concept concept = this.requireConceptByName("Enumeration");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("DataType"));
    Containment literals = new Containment("literals", concept, "-id-Enumeration-literals");
    literals.setKey("Enumeration-literals");
    literals.setType(this.requireClassifierByName("EnumerationLiteral"));
    literals.setOptional(true);
    literals.setMultiple(true);
  }

  public Concept getEnumerationLiteral() {
    return this.requireConceptByName("EnumerationLiteral");
  }

  private void initEnumerationLiteral() {
    Concept concept = this.requireConceptByName("EnumerationLiteral");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.addImplementedInterface(this.requireInterfaceByName("IKeyed"));
  }

  public Concept getFeature() {
    return this.requireConceptByName("Feature");
  }

  private void initFeature() {
    Concept concept = this.requireConceptByName("Feature");
    concept.setAbstract(true);
    concept.setPartition(false);
    concept.addImplementedInterface(this.requireInterfaceByName("IKeyed"));
    Property optional = new Property("optional", concept, "-id-Feature-optional");
    optional.setKey("Feature-optional");
    optional.setType(LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1));
    optional.setOptional(false);
  }

  public Concept getClassifier() {
    return this.requireConceptByName("Classifier");
  }

  private void initClassifier() {
    Concept concept = this.requireConceptByName("Classifier");
    concept.setAbstract(true);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("LanguageEntity"));
    Containment features = new Containment("features", concept, "-id-Classifier-features");
    features.setKey("Classifier-features");
    features.setType(this.requireClassifierByName("Feature"));
    features.setOptional(true);
    features.setMultiple(true);
  }

  public Concept getLink() {
    return this.requireConceptByName("Link");
  }

  private void initLink() {
    Concept concept = this.requireConceptByName("Link");
    concept.setAbstract(true);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Feature"));
    Property multiple = new Property("multiple", concept, "-id-Link-multiple");
    multiple.setKey("Link-multiple");
    multiple.setType(LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1));
    multiple.setOptional(false);
    Reference type = new Reference("type", concept, "-id-Link-type");
    type.setKey("Link-type");
    type.setType(this.requireClassifierByName("Classifier"));
    type.setOptional(false);
    type.setMultiple(false);
  }

  public Concept getLanguage() {
    return this.requireConceptByName("Language");
  }

  private void initLanguage() {
    Concept concept = this.requireConceptByName("Language");
    concept.setAbstract(false);
    concept.setPartition(true);
    concept.addImplementedInterface(this.requireInterfaceByName("IKeyed"));
    Property version = new Property("version", concept, "-id-Language-version");
    version.setKey("Language-version");
    version.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    version.setOptional(false);
    Reference dependsOn = new Reference("dependsOn", concept, "-id-Language-dependsOn");
    dependsOn.setKey("Language-dependsOn");
    dependsOn.setType(this.requireClassifierByName("Language"));
    dependsOn.setOptional(true);
    dependsOn.setMultiple(true);
    Containment entities = new Containment("entities", concept, "-id-Language-entities");
    entities.setKey("Language-entities");
    entities.setType(this.requireClassifierByName("LanguageEntity"));
    entities.setOptional(true);
    entities.setMultiple(true);
  }

  public Concept getLanguageEntity() {
    return this.requireConceptByName("LanguageEntity");
  }

  private void initLanguageEntity() {
    Concept concept = this.requireConceptByName("LanguageEntity");
    concept.setAbstract(true);
    concept.setPartition(false);
    concept.addImplementedInterface(this.requireInterfaceByName("IKeyed"));
  }

  public Concept getPrimitiveType() {
    return this.requireConceptByName("PrimitiveType");
  }

  private void initPrimitiveType() {
    Concept concept = this.requireConceptByName("PrimitiveType");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("DataType"));
  }

  public Concept getProperty() {
    return this.requireConceptByName("Property");
  }

  private void initProperty() {
    Concept concept = this.requireConceptByName("Property");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Feature"));
    Reference type = new Reference("type", concept, "-id-Property-type");
    type.setKey("Property-type");
    type.setType(this.requireClassifierByName("DataType"));
    type.setOptional(false);
    type.setMultiple(false);
  }

  public Concept getReference() {
    return this.requireConceptByName("Reference");
  }

  private void initReference() {
    Concept concept = this.requireConceptByName("Reference");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Link"));
  }

  public Interface getIKeyed() {
    return this.requireInterfaceByName("IKeyed");
  }

  private void initIKeyed() {
    Interface interf = this.requireInterfaceByName("IKeyed");
    interf.addExtendedInterface(
        LionCore.getInstance(LionWebVersion.v2023_1).requireInterfaceByName("INamed"));
    Property key = new Property("key", interf, "-id-IKeyed-key");
    key.setKey("IKeyed-key");
    key.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    key.setOptional(false);
  }

  private void createElements() {
    new Concept(this, "Annotation", "-id-Annotation", "Annotation");
    ;
    new Concept(this, "Concept", "-id-Concept", "Concept");
    ;
    new Concept(this, "Interface", "-id-Interface", "Interface");
    ;
    new Concept(this, "Containment", "-id-Containment", "Containment");
    ;
    new Concept(this, "DataType", "-id-DataType", "DataType");
    ;
    new Concept(this, "Enumeration", "-id-Enumeration", "Enumeration");
    ;
    new Concept(this, "EnumerationLiteral", "-id-EnumerationLiteral", "EnumerationLiteral");
    ;
    new Concept(this, "Feature", "-id-Feature", "Feature");
    ;
    new Concept(this, "Classifier", "-id-Classifier", "Classifier");
    ;
    new Concept(this, "Link", "-id-Link", "Link");
    ;
    new Concept(this, "Language", "-id-Language", "Language");
    ;
    new Concept(this, "LanguageEntity", "-id-LanguageEntity", "LanguageEntity");
    ;
    new Concept(this, "PrimitiveType", "-id-PrimitiveType", "PrimitiveType");
    ;
    new Concept(this, "Property", "-id-Property", "Property");
    ;
    new Concept(this, "Reference", "-id-Reference", "Reference");
    ;
    new Interface(this, "IKeyed", "-id-IKeyed", "IKeyed");
    ;
  }
}
