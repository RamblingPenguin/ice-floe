package com.ramblingpenguin.icefloe.core;

public interface IceFloeVisitor {
    void visitSequence(Sequence<?, ?> sequence);
    void visitPredicate(PredicateNode<?, ?> node);
    void visitFork(ForkSequence<?, ?, ?, ?> node);
    void visitNode(Node<?, ?> node);
}