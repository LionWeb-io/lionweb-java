package io.lionweb.lioncore.java.utils;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModelComparator {

    public class ComparisonResult {
        private List<String> differences = new ArrayList<>();

        public List<String> getDifferences() {
            return differences;
        }

        public boolean areEquivalent() {
            return differences.isEmpty();
        }

        public void markDifferentIDs(String context, String idA, String idB) {
            differences.add(context+": different ids, a=" + idA+", b="+idB);
        }

        public void markDifferentConcept(String context, String nodeID, String conceptIDa, String conceptIDb) {
            differences.add(context + " (id="+nodeID+") : different concepts, a=" + conceptIDa+", b="+conceptIDb);
        }

        public void markDifferentPropertyValue(String context, String nodeID, String propertyName, Object valueA, Object valueB) {
            differences.add(context + " (id="+nodeID+") : different property value for " + propertyName + ", a=" + valueA+", b="+valueB);
        }

        public void markDifferentReferenceValue(String context, String nodeID,String propertyName, Object valueA, Object valueB) {
            differences.add(context + " (id="+nodeID+") : different reference value for " + propertyName + ", a=" + valueA+", b="+valueB);
        }

        public void markDifferentNumberOfChildren(String context, String nodeID,String containmentName, int childrenA, int childrenB) {
            differences.add(context + " (id="+nodeID+") : different number of children for " + containmentName + ", a=" + childrenA+", b="+childrenB);
        }

        public void markDifferentNumberOfReferences(String context, String nodeID,String referenceName, int childrenA, int childrenB) {
            differences.add(context + " (id="+nodeID+") : different number of referred for " + referenceName + ", a=" + childrenA+", b="+childrenB);
        }

        public void markDifferentReferredID(String context, String nodeID,String referenceName, int index, String referredA, String referredB) {
            differences.add(context + " (id="+nodeID+") : different referred id for " + referenceName + " index " + index+ ", a=" + referredA+", b="+referredB);
        }

        public void markDifferentResolveInfo(String context, String nodeID,String referenceName, int index, String resolveInfoA, String resolveInfoB) {
            differences.add(context + " (id="+nodeID+") : different resolve info for " + referenceName + " index " + index+ ", a=" + resolveInfoA+", b="+resolveInfoB);
        }

        @Override
        public String toString() {
            return "ComparisonResult: " + differences;
        }
    }

    public ComparisonResult compare(Node nodeA, Node nodeB) {
        ComparisonResult comparisonResult = new ComparisonResult();
        compare(nodeA, nodeB, comparisonResult, "<root>");
        return comparisonResult;
    }

    private void compare(Node nodeA, Node nodeB, ComparisonResult comparisonResult, String context) {
        if (!Objects.equals(nodeA.getID(), nodeB.getID())) {
            comparisonResult.markDifferentIDs(context, nodeA.getID(), nodeB.getID());
        } else {
            if (Objects.equals(nodeA.getConcept().getID(), nodeB.getConcept().getID())) {
                Concept concept = nodeA.getConcept();
                for (Property property : concept.allProperties()) {
                    Object valueA = nodeA.getPropertyValue(property);
                    Object valueB = nodeB.getPropertyValue(property);
                    if (!Objects.equals(valueA, valueB)) {
                        comparisonResult.markDifferentPropertyValue(context, nodeA.getID(), property.qualifiedName(), valueA, valueB);
                    }
                }
                for (Reference reference : concept.allReferences()) {
                    List<ReferenceValue> valueA = nodeA.getReferenceValues(reference);
                    List<ReferenceValue> valueB = nodeB.getReferenceValues(reference);
                    if (valueA.size() != valueB.size()) {
                        comparisonResult.markDifferentNumberOfReferences(context, nodeA.getID(), reference.qualifiedName(), valueA.size(), valueB.size());
                    } else {
                        for (int i = 0; i < valueA.size(); i++) {
                            ReferenceValue refA = valueA.get(i);
                            ReferenceValue refB = valueB.get(i);
                            if (!Objects.equals(refA.getReferredID(), refB.getReferredID())) {
                                comparisonResult.markDifferentReferredID(context, nodeA.getID(), reference.qualifiedName(), i, refA.getReferredID(), refB.getReferredID());
                            }
                            if (!Objects.equals(refA.getResolveInfo(), refB.getResolveInfo())) {
                                comparisonResult.markDifferentResolveInfo(context, nodeA.getID(), reference.qualifiedName(), i, refA.getResolveInfo(), refB.getResolveInfo());
                            }
                        }
                    }
                }
                for (Containment containment : concept.allContainments()) {
                    List<? extends Node> valueA = nodeA.getChildren(containment);
                    List<? extends Node> valueB = nodeB.getChildren(containment);
                    if (valueA.size() != valueB.size()) {
                        comparisonResult.markDifferentNumberOfChildren(context, nodeA.getID(), containment.qualifiedName(), valueA.size(), valueB.size());
                    } else {
                        for (int i = 0; i < valueA.size(); i++) {
                            compare(valueA.get(i), valueB.get(i), comparisonResult, context + "/" + containment.getName() + "[" + i + "]");
                        }
                    }
                }
            } else {
                comparisonResult.markDifferentConcept(context, nodeA.getID(), nodeA.getConcept().getID(), nodeB.getConcept().getID());
            }
        }
    }
}
