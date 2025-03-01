package csci2020u.assignment01;

import java.io.*;
import java.util.*;

public class SpamDetector {
    // Map to store spam probabilities for each word
    private final Map<String, Double> spamProbabilities = new HashMap<>();
    private int spamCount = 0;

    // Laplace Smoothing constant (to avoid zero probability issues)
    private static final double LAPLACE_SMOOTHING = 1.0;

    // Method to train the spam detector using training data
    public void train(File trainDir) {
        Map<String, Integer> spamFreq = new HashMap<>(), hamFreq = new HashMap<>();
        Set<String> uniqueWords = new HashSet<>(); // Track all unique words across spam and ham

        // Process spam and ham emails
        spamCount = processFiles(new File(trainDir, "spam"), spamFreq);
        int hamCount = processFiles(new File(trainDir, "ham"), hamFreq) + processFiles(new File(trainDir, "ham2"), hamFreq);

        // Collect all unique words
        uniqueWords.addAll(spamFreq.keySet());
        uniqueWords.addAll(hamFreq.keySet());

        // Compute spam probability for each word using Laplace Smoothing
        for (String word : uniqueWords) {
            double pWordSpam = (spamFreq.getOrDefault(word, 0) + LAPLACE_SMOOTHING) / (spamCount + 2 * LAPLACE_SMOOTHING);
            double pWordHam = (hamFreq.getOrDefault(word, 0) + LAPLACE_SMOOTHING) / (hamCount + 2 * LAPLACE_SMOOTHING);
            spamProbabilities.put(word, pWordSpam / (pWordSpam + pWordHam));
        }

        // Print training statistics
        System.out.println("Training completed:");
        System.out.println("Spam emails processed: " + spamCount);
        System.out.println("Ham emails processed: " + hamCount);
    }

    // Method to classify an email file as spam or ham
    public double classify(File file) {
        Set<String> words = extractWords(file);
        double eta = 0.0;

        // Calculate probability using logarithmic transformation
        for (String word : words) {
            double p = spamProbabilities.getOrDefault(word, 0.5);
            eta += (Math.log(1 - p) - Math.log(p));
        }

        return 1 / (1 + Math.exp(eta)); // Return probability of being spam
    }

    // Method to process files in a given directory
    private int processFiles(File dir, Map<String, Integer> freq) {
        int fileCount = 0;
        System.out.println("Processing directory: " + dir.getName());

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            fileCount++;
            System.out.println("Processing file: " + file.getName());

            Set<String> words = extractWords(file);
            for (String word : words) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }

        System.out.println("Total files processed in " + dir.getName() + ": " + fileCount);
        return fileCount;
    }

    // Method to extract words from a given file
    private Set<String> extractWords(File file) {
        Set<String> words = new HashSet<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase().replaceAll("[^a-zA-Z]", "");
                words.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
}
