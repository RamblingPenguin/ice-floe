package com.ramblingpenguin.icefloe.core;

import java.util.function.Predicate;

/**
 * A specialized {@link Node} that implements conditional branching logic.
 * <p>
 * This node evaluates a {@link Predicate} against the input. If the predicate returns {@code true},
 * the {@code matchSequence} is executed. Otherwise, the {@code elseSequence} is executed.
 * <p>
 * Both branches must accept the same input type and produce the same output type to ensure
 * type safety in the pipeline.
 *
 * @param <INPUT>  The type of the input.
 * @param <OUTPUT> The type of the output.
 */
public class PredicateNode<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private final Predicate<INPUT> predicate;
    private final Node<INPUT, OUTPUT> nestedSequence;
    private final Node<INPUT, OUTPUT> elseSequence;

    /**
     * Creates a new {@code PredicateNode}.
     *
     * @param predicate      The condition to evaluate.
     * @param matchSequence  The node to execute if the predicate is true.
     * @param elseSequence   The node to execute if the predicate is false.
     */
    public PredicateNode(Predicate<INPUT> predicate, Node<INPUT, OUTPUT> matchSequence, Node<INPUT, OUTPUT> elseSequence) {
        this.predicate = predicate;
        this.nestedSequence = matchSequence;
        this.elseSequence = elseSequence;
    }

    /**
     * Applies the conditional logic to the given input.
     *
     * @param input the function argument
     * @return the result of either the {@code matchSequence} or the {@code elseSequence}.
     */
    @Override
    public OUTPUT apply(INPUT input) {
        if (this.predicate.test(input)) {
            return this.nestedSequence.apply(input);
        } else {
            return this.elseSequence.apply(input);
        }
    }

    public Node<INPUT, OUTPUT> getMatchSequence() {
        return nestedSequence;
    }

    public Node<INPUT, OUTPUT> getElseSequence() {
        return elseSequence;
    }
}