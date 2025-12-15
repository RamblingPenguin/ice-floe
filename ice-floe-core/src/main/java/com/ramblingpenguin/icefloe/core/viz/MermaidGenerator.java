package com.ramblingpenguin.icefloe.core.viz;

import com.ramblingpenguin.icefloe.core.*;

import java.util.*;

public class MermaidGenerator implements IceFloeVisitor {

    private final StringBuilder sb = new StringBuilder();
    private final Map<Node<?, ?>, String> nodeIds = new IdentityHashMap<>();
    private int idCounter = 0;

    // State to communicate between visit calls
    private String lastEntryId;
    private List<String> lastExitIds;

    public String generate(Node<?, ?> root) {
        sb.setLength(0);
        nodeIds.clear();
        idCounter = 0;
        sb.append("flowchart TD\n");
        
        root.accept(this);
        
        return sb.toString();
    }

    private String getOrAssignId(Node<?, ?> node) {
        return nodeIds.computeIfAbsent(node, k -> "node" + (++idCounter));
    }

    private String getLabel(Node<?, ?> node) {
        String simpleName = node.getClass().getSimpleName();
        if (simpleName.isEmpty()) return "Lambda/Anon";
        return simpleName;
    }

    @Override
    public void visitSequence(Sequence<?, ?> sequence) {
        String seqId = getOrAssignId(sequence);
        // Draw a subgraph for the sequence
        sb.append("  subgraph ").append(seqId).append(" [Sequence]\n");
        sb.append("    direction TB\n");

        List<? extends Node<?, ?>> nodes = sequence.getNodes();
        if (nodes.isEmpty()) {
            sb.append("    ").append(seqId).append("_empty[Empty]\n");
            this.lastEntryId = seqId + "_empty";
            this.lastExitIds = List.of(seqId + "_empty");
        } else {
            String seqEntry = null;
            List<String> prevExits = null;

            for (Node<?, ?> child : nodes) {
                child.accept(this); // Visits child, updates lastEntryId, lastExitIds
                
                String childEntry = this.lastEntryId;
                List<String> childExits = this.lastExitIds;

                if (seqEntry == null) {
                    seqEntry = childEntry;
                }

                if (prevExits != null) {
                    for (String prevExit : prevExits) {
                        sb.append("    ").append(prevExit).append(" --> ").append(childEntry).append("\n");
                    }
                }
                prevExits = childExits;
            }
            this.lastEntryId = seqEntry;
        }
        sb.append("  end\n");
    }

    @Override
    public void visitPredicate(PredicateNode<?, ?> node) {
        String id = getOrAssignId(node);
        // The diamond
        sb.append("  ").append(id).append("{Predicate}\n");

        // Visit Match Branch
        node.getMatchSequence().accept(this);
        String matchEntry = this.lastEntryId;
        List<String> matchExits = this.lastExitIds;

        // Visit Else Branch
        node.getElseSequence().accept(this);
        String elseEntry = this.lastEntryId;
        List<String> elseExits = this.lastExitIds;

        // Connections
        sb.append("  ").append(id).append(" -- True --> ").append(matchEntry).append("\n");
        sb.append("  ").append(id).append(" -- False --> ").append(elseEntry).append("\n");

        // Update state
        this.lastEntryId = id;
        List<String> exits = new ArrayList<>();
        exits.addAll(matchExits);
        exits.addAll(elseExits);
        this.lastExitIds = exits;
    }

    @Override
    public void visitFork(ForkSequence<?, ?, ?, ?> node) {
        String id = getOrAssignId(node);
        String splitId = id + "_split";
        String joinId = id + "_join";

        sb.append("  subgraph ").append(id).append(" [ForkSequence]\n");
        sb.append("    direction TB\n");
        sb.append("    ").append(splitId).append("((Split))\n");
        
        // The fork node processing (conceptually runs on each item)
        Node<?, ?> forkNode = node.getForkNode();
        forkNode.accept(this);
        String forkEntry = this.lastEntryId;
        List<String> forkExits = this.lastExitIds;

        // Connect split to fork node
        sb.append("    ").append(splitId).append(" -.-> ").append(forkEntry).append("\n");

        // Join
        sb.append("    ").append(joinId).append("((Reduce))\n");
        for (String exit : forkExits) {
            sb.append("    ").append(exit).append(" -.-> ").append(joinId).append("\n");
        }

        sb.append("  end\n");

        this.lastEntryId = splitId;
        this.lastExitIds = List.of(joinId);
    }

    @Override
    public void visitNode(Node<?, ?> node) {
        String id = getOrAssignId(node);
        String label = getLabel(node);
        // Escape label quotes? Mermaid handles simple text. 
        sb.append("  ").append(id).append("[").append(label).append("]\n");
        
        this.lastEntryId = id;
        this.lastExitIds = List.of(id);
    }
}
