package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;

public class GuideBookWriter extends Writer {
    public GuideBookWriter(String id, String name) {
        super(id, name, LibraryMetamodel.GUIDE_BOOK_WRITER);
    }

    public void setCountries(String countries) {
        setPropertyValue(getConcept().getPropertyByName("countries"), countries);
    }

    @Override
    public Concept getConcept() {
        return LibraryMetamodel.GUIDE_BOOK_WRITER;
    }
}
