package com.ramblingpenguin.icefloe.core.viz;

import com.ramblingpenguin.icefloe.core.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MermaidGeneratorTest {

    @Test
    public void testGenerateComplexWorkflow() {
        // Recreating the structure from ComplexWorkflowTest roughly
        
        Node<String, String> priorityPath = Sequence.Builder.of(String.class)
                .then(s -> s + " -> Priority Log")
                .then(s -> s + " -> Fee")
                .build();

        Node<String, Integer> standardPath = ForkSequence
                .<String, String, Integer>builder(
                        s -> List.of(s.split(" ")),
                        word -> word.length()
                )
                .withReducer(0, Integer::sum)
                .build(); // Simplified for brevity in graph test (Node<String, Integer>)
        
        // Wrap standardPath to match types for PredicateNode (String -> String)
        // Just for graph structure, the actual types inside lambdas don't matter much to the generator
        // but Java compiler needs them correct-ish.
        Node<String, String> standardPathWrapped = Sequence.Builder.of(String.class)
                 .then(standardPath) // Output Integer
                 .then(Object::toString) // Integer -> String
                 .build();

        Sequence<String, String> pipeline = Sequence.Builder.of(String.class)
                .then(s -> s.trim()) // Validation
                .then(new PredicateNode<>(
                        s -> s.startsWith("P"),
                        priorityPath,
                        standardPathWrapped
                ))
                .then(s -> "Final: " + s)
                .build();

        MermaidGenerator generator = new MermaidGenerator();
        String mermaid = generator.generate(pipeline);

        System.out.println("Generated Mermaid Graph:");
        System.out.println(mermaid);
        
        // Basic assertions
        assert mermaid.startsWith("flowchart TD");
        assert mermaid.contains("subgraph");
        assert mermaid.contains("Predicate");
        assert mermaid.contains("ForkSequence");
    }
}
