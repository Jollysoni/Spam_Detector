package csci2020u.assignment01;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.DecimalFormat;

public class SpamDetector {

    // This map stores word frequencies from ham emails.
    private Map<String, Integer> trainHamFreq;

    // This map stores word frequencies from spam emails.
    private Map<String, Integer> trainSpamFreq;

    //Probability Maps: store calculated probabilities for words given spam or ham.
    private Map<String, Double> p_WordGivenHam;
    private Map<String, Double> p_WordGivenSpam;

    //Counts for total spam and ham emails processed during training
    private int totalSpamEmails;
    private int totalHamEmails;

    public SpamDetector() {
        // Constructor
        trainHamFreq = new TreeMap<>();
        trainSpamFreq = new TreeMap<>();
        p_WordGivenHam = new TreeMap<>();
        p_WordGivenSpam = new TreeMap<>();
        totalHamEmails = 0;
        totalSpamEmails = 0;
    }
    // this method recursively scans files in train/spam and train/ham to count word occurences.
    public void parseTrainingData(File folder, boolean isSpam) throws IOException
    {
        //Checks if the given file is actually a directory. If so, it iterates through all files in that directory
        if(folder.isDirectory())
        {
            File[] files = folder.listFiles();
            if(files!=null)
            {
                for (File currentFile: files) {
                    parseTrainingData(currentFile, isSpam);
                }
            }
        }
        //Process individual email file
        else {

            //to count the number of spam and ham emails
            if (isSpam) totalSpamEmails++; else totalHamEmails++;

            //Reads the content of a file line by line to extract words
            Scanner scanner = new Scanner(folder);
            Set <String> uniqueWords = new HashSet<>(); //to avoid counting duplicate of words in the same email
            while(scanner.hasNext())
            {
                String word = scanner.next().toLowerCase(); //convert word to LowerCase
                if(isValidWord(word))
                {
                    uniqueWords.add(word);
                }
            }
            scanner.close(); //Close scanner to avoid resource leaks

            //Update frequency maps with unique words from the email
            for(String word : uniqueWords)
            {
                if(isSpam){
                    countToken(word, trainSpamFreq); //count word in spam map
                }
                else {
                    countToken(word, trainHamFreq); //count word in ham map
                }
            }
        }
    }
    //Ensures that only valid words having alphabetic characters, ignoring special characters are present.
    private boolean isValidWord(String word)
    {
        return word.matches("^[a-zA-Z]+$");
    }
    //Increments the count of a word in the respective frequency map (spam or ham)
    private void countToken(String word, Map<String, Integer> map)
    {
        map.put(word, map.getOrDefault(word,0)+1); //increment count or set to 1 if not present.
    }

    //getter methods for trainHamFreq and trainSpamFreq
    public Map<String, Integer> getTrainHamFreq()
    {
        return trainHamFreq;
    }
    public Map<String, Integer> getTrainSpamFreq()
    {
        return trainSpamFreq;
    }

    /**
     *
     * Calculates probabilities for P(word|spam) and P(word|ham) using the frequency maps.
     */
    public void calculateProbabilities()
    {
        Set <String> wordset = new HashSet<>();   // Merging all words
        wordset.addAll(trainHamFreq.keySet());
        wordset.addAll(trainSpamFreq.keySet());

        for (String word : wordset)
        {
            int spamCount = trainSpamFreq.getOrDefault(word,0);
            int hamCount = trainHamFreq.getOrDefault(word,0);

            //Laplace smoothing : add 1 to the numerator, totalEmails +2 to denominator to avoid zero probabilities
            double probSpam = (double)(spamCount + 1 ) / (totalSpamEmails +2);
            double probHam = (double)(hamCount + 1 ) / (totalHamEmails +2);

            p_WordGivenSpam.put(word, probSpam);
            p_WordGivenHam.put(word, probHam);
        }
    }
    public double classifyEmail(String[] words){
        double spamLogProbability = Math.log((double) totalSpamEmails/(totalSpamEmails + totalHamEmails));
        double hamLogProbability = Math.log((double) totalHamEmails/ (totalSpamEmails + totalHamEmails));

        for (String word : words){
            if (p_WordGivenSpam.containsKey(word)){
                spamLogProbability += Math.log(p_WordGivenSpam.get(word));
            }
            if(p_WordGivenHam.containsKey(word)){
                hamLogProbability += Math.log(p_WordGivenHam.get(word));
            }
        }
        double spamProbability = Math.exp(spamLogProbability)/(Math.exp(spamLogProbability)+ Math.exp(hamLogProbability));
        return spamProbability;
    }

    public void evaluateClassifier(TestFile[] testFiles){
        int truePositives =0;
        int falsePositives =0;
        int trueNegatives =0;
        int falseNegatives =0;
        for (TestFile testFile : testFiles){
            String[] words = testFile.getFilename().toLowerCase().split(" ");
            double spamProbability = classifyEmail(words);
            String predictedClass = (spamProbability > 0.5) ? "spam" : "ham";

            if (predictedClass.equals("spam") && testFile.getActualClass().equals("spam")) {
                truePositives++;
            } else if (predictedClass.equals("spam") && testFile.getActualClass().equals("ham")) {
                falsePositives++;

            } else if (predictedClass.equals("ham") && testFile.getActualClass().equals("ham")) {
                trueNegatives++;

            } else if (predictedClass.equals("ham") && testFile.getActualClass().equals("spam") ) {
                falseNegatives++;

            }
        }
        double accuracy = (double)(truePositives+trueNegatives)/ testFiles.length;
        double precision = (double)truePositives/(truePositives + falsePositives);
        double recall = (double) truePositives / (truePositives + falseNegatives);
        double f1Score = 2*(precision * recall)/ (precision + recall);

        System.out.println("Accuracy:" + accuracy);
        System.out.println("Precision:" + precision);
        System.out.println("Recall:" + recall);
        System.out.println("F1 Score:" + f1Score);


    }


    public Map<String, Double> getP_WordGivenHam()
    {
        return p_WordGivenHam;
    }
    public Map<String, Double> getP_WordGivenSpam()
    {
        return p_WordGivenSpam;
    }


    public static void Main(String args[])
    {
        SpamDetector ob = new SpamDetector();
        try {
            File ham_folder = new File("../../resources/data/train/ham");
            File spam_folder = new File("../../resources/data/train/spam");

            ob.parseTrainingData(ham_folder, false  );
            ob.parseTrainingData(spam_folder, true);


            System.out.println("Ham Frequencies: "+ob.getTrainHamFreq());
            System.out.println("Spam Frequencies: "+ob.getTrainSpamFreq());

            // Calculate probabilities after training data is parsed
            ob.calculateProbabilities();

            System.out.println("Calculated Probabilities ( P(word|ham) ): "+ob.getP_WordGivenHam());
            System.out.println("Calculated Probabilities ( P(word|spam) ): "+ob.getP_WordGivenSpam());

            TestFile[] testFiles = {
                    new TestFile("free win" , 0.0, "spam"),
                    new TestFile("hello world", 0.0 , "ham")
            };

            ob.evaluateClassifier(testFiles);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

