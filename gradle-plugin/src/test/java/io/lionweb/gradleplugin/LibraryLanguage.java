package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;

public class LibraryLanguage extends Language {
  private static LibraryLanguage INSTANCE;

  private LibraryLanguage() {
    super(LionWebVersion.v2023_1);
    this.setName("library");
    this.setVersion("1");
    this.setID("library");
    this.setKey("library");

    createElements();
    initBook();
    initLibrary();
    initWriter();
    initGuideBookWriter();
    initSpecialistBookWriter();
  }

  public static LibraryLanguage getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new LibraryLanguage();
    }
    return INSTANCE;
  }

  public Concept getBook() {
    return this.requireConceptByName("Book");
  }

  private void initBook() {
    Concept concept = this.requireConceptByName("Book");
    concept.setAbstract(false);
    concept.setPartition(false);
    Property title = new Property("title", concept, "library-Book-title");
    title.setKey("library-Book-title");
    title.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    title.setOptional(false);
    Property pages = new Property("pages", concept, "library-Book-pages");
    pages.setKey("library-Book-pages");
    pages.setType(LionCoreBuiltins.getInteger(LionWebVersion.v2023_1));
    pages.setOptional(false);
    Reference author = new Reference("author", concept, "library-Book-author");
    author.setKey("library-Book-author");
    author.setType(this.requireClassifierByName("Writer"));
    author.setOptional(false);
    author.setMultiple(false);
  }

  public Concept getLibrary() {
    return this.requireConceptByName("Library");
  }

  private void initLibrary() {
    Concept concept = this.requireConceptByName("Library");
    concept.setAbstract(false);
    concept.setPartition(false);
    Property name = new Property("name", concept, "library-Library-name");
    name.setKey("library-Library-name");
    name.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    name.setOptional(false);
    Containment books = new Containment("books", concept, "library-Library-books");
    books.setKey("library-Library-books");
    books.setType(this.requireClassifierByName("Book"));
    books.setOptional(true);
    books.setMultiple(true);
  }

  public Concept getWriter() {
    return this.requireConceptByName("Writer");
  }

  private void initWriter() {
    Concept concept = this.requireConceptByName("Writer");
    concept.setAbstract(false);
    concept.setPartition(false);
    Property name = new Property("name", concept, "library-Writer-name");
    name.setKey("library-Writer-name");
    name.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    name.setOptional(false);
  }

  public Concept getGuideBookWriter() {
    return this.requireConceptByName("GuideBookWriter");
  }

  private void initGuideBookWriter() {
    Concept concept = this.requireConceptByName("GuideBookWriter");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Writer"));
    Property countries = new Property("countries", concept, "library-GuideBookWriter-countries");
    countries.setKey("library-GuideBookWriter-countries");
    countries.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    countries.setOptional(true);
  }

  public Concept getSpecialistBookWriter() {
    return this.requireConceptByName("SpecialistBookWriter");
  }

  private void initSpecialistBookWriter() {
    Concept concept = this.requireConceptByName("SpecialistBookWriter");
    concept.setAbstract(false);
    concept.setPartition(false);
    concept.setExtendedConcept(this.requireConceptByName("Writer"));
    Property subject = new Property("subject", concept, "library-SpecialistBookWriter-subject");
    subject.setKey("library-SpecialistBookWriter-subject");
    subject.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    subject.setOptional(true);
  }

  private void createElements() {
    PrimitiveType primitiveType = new PrimitiveType(this, "String", "library-String");
    primitiveType.setKey("library-String");
    new Concept(this, "Book", "library-Book", "library-Book");
    ;
    new Concept(this, "Library", "library-Library", "library-Library");
    ;
    new Concept(this, "Writer", "library-Writer", "library-Writer");
    ;
    new Concept(this, "GuideBookWriter", "library-GuideBookWriter", "library-GuideBookWriter");
    ;
    new Concept(
        this,
        "SpecialistBookWriter",
        "library-SpecialistBookWriter",
        "library-SpecialistBookWriter");
    ;
  }
}
