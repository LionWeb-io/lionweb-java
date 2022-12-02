package org.lionweb.lioncore.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Represent something not yet agreed upon.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
public @interface Experimental {

}
