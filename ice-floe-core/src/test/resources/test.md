```mermaid
flowchart TD
  subgraph node1 [Sequence]
    direction TB
  node2[MermaidGeneratorTest$$Lambda/0x000000000e13f028]
  node3{Predicate}
  subgraph node4 [Sequence]
    direction TB
  node5[MermaidGeneratorTest$$Lambda/0x000000000e13d848]
  node6[MermaidGeneratorTest$$Lambda/0x000000000e13dab8]
    node5 --> node6
  end
  subgraph node7 [Sequence]
    direction TB
  subgraph node8 [ForkSequence]
    direction TB
    node8_split((Split))
  node9[MermaidGeneratorTest$$Lambda/0x000000000e13e1f8]
    node8_split -.-> node9
    node8_join((Reduce))
    node9 -.-> node8_join
  end
  node10[MermaidGeneratorTest$$Lambda/0x000000000e13edb8]
    node8_join --> node10
  end
  node3 -- True --> node5
  node3 -- False --> node8_split
    node2 --> node3
  node11[MermaidGeneratorTest$$Lambda/0x000000000e13f780]
    node6 --> node11
    node10 --> node11
```
