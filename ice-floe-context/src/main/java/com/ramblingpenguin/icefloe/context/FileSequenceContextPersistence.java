package com.ramblingpenguin.icefloe.context;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A file-based implementation of {@link SequenceContextPersistence} that uses standard Java serialization.
 */
public class FileSequenceContextPersistence implements SequenceContextPersistence {

    private final String persistencePath;

    public FileSequenceContextPersistence(String persistencePath) {
        this.persistencePath = persistencePath;
        new File(persistencePath).mkdirs();
    }

    @Override
    public void saveState(String executionId, SequenceContext context) throws IOException {
        File file = new File(persistencePath, executionId + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(context);
        }
    }

    @Override
    public SequenceContext loadState(String executionId) throws IOException, ClassNotFoundException {
        File file = new File(persistencePath, executionId + ".ser");
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (SequenceContext) ois.readObject();
        }
    }

    @Override
    public List<String> listStoredExecutionIds() throws IOException {
        File dir = new File(persistencePath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".ser"));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files)
                .map(file -> file.getName().replace(".ser", ""))
                .collect(Collectors.toList());
    }
}
