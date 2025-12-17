package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.Map;
import java.util.function.Function;

/**
 * A node that selects a node to execute based on a classifier.
 *
 * @param <INPUT>      the input type
 * @param <OUTPUT>     the output type
 * @param <CLASSIFIER> the classifier type
 */
public class SwitchNode<INPUT, OUTPUT, CLASSIFIER> implements Node<INPUT, OUTPUT> {

    private final Function<INPUT, CLASSIFIER> classifierExtractor;
    private final Function<CLASSIFIER, String> classificationKeyGenerator;
    private final Map<String, Node<INPUT, OUTPUT>> nodesByClassification;
    private final Node<INPUT, OUTPUT> defaultNode;

    /**
     * Constructs a new switch node.
     *
     * @param classifierExtractor      the function to extract the classifier from the input
     * @param classificationKeyGenerator the function to generate a key from the classifier
     * @param nodesByClassification    the map of nodes to select from
     * @param defaultNode              the default node to execute if no other node is selected
     */
    public SwitchNode(Function<INPUT, CLASSIFIER> classifierExtractor, Function<CLASSIFIER, String> classificationKeyGenerator, Map<String, Node<INPUT, OUTPUT>> nodesByClassification, Node<INPUT, OUTPUT> defaultNode) {
        this.classifierExtractor = classifierExtractor;
        this.classificationKeyGenerator = classificationKeyGenerator;
        this.nodesByClassification = nodesByClassification;
        this.defaultNode = defaultNode;
    }


    @Override
    public OUTPUT apply(INPUT input) {
        CLASSIFIER classifier = classifierExtractor.apply(input);
        String classification = this.classificationKeyGenerator.apply(classifier);
        Node<INPUT, OUTPUT> node = this.nodesByClassification.getOrDefault(classification, this.defaultNode);
        return node.apply(input);
    }
}
