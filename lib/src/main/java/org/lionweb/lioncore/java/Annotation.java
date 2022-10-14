package org.lionweb.lioncore.java;

public class Annotation extends AbstractConcept {
    private String platformSpecific;

    public Annotation(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public String getPlatformSpecific() {
        return platformSpecific;
    }

    public void setPlatformSpecific(String platformSpecific) {
        this.platformSpecific = platformSpecific;
    }
}
