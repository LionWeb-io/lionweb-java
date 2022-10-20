package org.lionweb.lioncore.java;

/**
 * Measure of the number of elements targeted by a {@link Feature}.
 *
 * In Ecore there is no equivalent as lowerBound and upperBound can be set independently.
 *
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F4241665505353447567">MPS equivalent <i>Cardinality</i> in local MPS</a>
 */
public enum Multiplicity {
    Optional,
    Single,
    ZeroOrMore,
    OneOrMore
}

