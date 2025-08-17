package io.lionweb.model.impl;

import io.lionweb.language.Annotation;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.serialization.SimpleNode;
import io.lionweb.serialization.simplemath.IntLiteral;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractClassifierInstanceTest {

    @Test
    public void addAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setID("annotation-A");

        SimpleNode n1 = new IntLiteral(1);
        AnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation);
        AnnotationInstance ann2 = new DynamicAnnotationInstance("ann2", annotation);
        AnnotationInstance ann1b = new DynamicAnnotationInstance("ann1", annotation);

        assertEquals(0, n1.getAnnotations().size());

        assertEquals(true, n1.addAnnotation(ann1));
        assertEquals(1, n1.getAnnotations().size());

        assertEquals(true, n1.addAnnotation(ann2));
        assertEquals(2, n1.getAnnotations().size());

        assertEquals(false, n1.addAnnotation(ann1b));
        assertEquals(2, n1.getAnnotations().size());

        assertEquals(false, n1.addAnnotation(ann1));
        assertEquals(2, n1.getAnnotations().size());
    }
}
