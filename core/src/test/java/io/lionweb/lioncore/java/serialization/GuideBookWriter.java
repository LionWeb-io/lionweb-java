package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.Concept;

public class GuideBookWriter extends Writer {
  public GuideBookWriter(String id, String name) {
    super(id, name, LibraryLanguage.GUIDE_BOOK_WRITER);
  }

  public void setCountries(String countries) {
    setPropertyValue(getConcept().getPropertyByName("countries"), countries);
  }

  @Override
  public Concept getConcept() {
    return LibraryLanguage.GUIDE_BOOK_WRITER;
  }
}
