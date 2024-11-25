package tn.esprit.similator.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomForestClassifier {
  private int numTrees = 100;
  private int maxDepth = 10;
  private int numFeatures;
  private int minSamplesSplit = 2;
  private int minSamplesLeaf = 1;
  private boolean bootstrapSamples = true;
  private List<DecisionTree> trees;
  private Random random;

  public RandomForestClassifier() {
    this.random = new Random();
    this.trees = new ArrayList<>();
  }

  // Setters for hyperparameters
  public void setNumTrees(int numTrees) {
    this.numTrees = numTrees;
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public void setNumFeatures(int numFeatures) {
    this.numFeatures = numFeatures;
  }

  public void setMinSamplesSplit(int minSamplesSplit) {
    this.minSamplesSplit = minSamplesSplit;
  }

  public void setMinSamplesLeaf(int minSamplesLeaf) {
    this.minSamplesLeaf = minSamplesLeaf;
  }

  public void setBootstrapSamples(boolean bootstrapSamples) {
    this.bootstrapSamples = bootstrapSamples;
  }

  public void train(double[][] features, int[] labels) {
    if (features.length != labels.length) {
      throw new IllegalArgumentException("Features and labels must have the same length");
    }

    if (numFeatures <= 0) {
      // If numFeatures not set, use sqrt(number of features) as default
      this.numFeatures = (int) Math.sqrt(features[0].length);
    }

    // Use parallel processing for training trees
    ExecutorService executor = Executors.newFixedThreadPool(
      Runtime.getRuntime().availableProcessors()
    );
    List<Future<DecisionTree>> futures = new ArrayList<>();

    for (int i = 0; i < numTrees; i++) {
      futures.add(executor.submit(() -> {
        // Create bootstrap sample if enabled
        int[] sampleIndices;
        if (bootstrapSamples) {
          sampleIndices = createBootstrapSample(features.length);
        } else {
          sampleIndices = new int[features.length];
          for (int j = 0; j < features.length; j++) {
            sampleIndices[j] = j;
          }
        }

        // Extract bootstrap sample
        double[][] treeFeaturesBootstrap = new double[sampleIndices.length][];
        int[] treeLabelsBootstrap = new int[sampleIndices.length];
        for (int j = 0; j < sampleIndices.length; j++) {
          treeFeaturesBootstrap[j] = features[sampleIndices[j]];
          treeLabelsBootstrap[j] = labels[sampleIndices[j]];
        }

        // Train individual tree
        DecisionTree tree = new DecisionTree(maxDepth, numFeatures, minSamplesSplit, minSamplesLeaf);
        tree.train(treeFeaturesBootstrap, treeLabelsBootstrap);
        return tree;
      }));
    }

    // Collect trained trees
    trees.clear();
    for (Future<DecisionTree> future : futures) {
      try {
        trees.add(future.get());
      } catch (Exception e) {
        throw new RuntimeException("Error training decision tree", e);
      }
    }

    executor.shutdown();
  }

  private int[] createBootstrapSample(int dataSize) {
    int[] indices = new int[dataSize];
    for (int i = 0; i < dataSize; i++) {
      indices[i] = random.nextInt(dataSize);
    }
    return indices;
  }

  public int predict(double[] features) {
    if (trees.isEmpty()) {
      throw new IllegalStateException("Model must be trained before making predictions");
    }

    // Count votes from each tree
    AtomicInteger upVotes = new AtomicInteger(0);
    trees.parallelStream().forEach(tree -> {
      if (tree.predict(features) == 1) {
        upVotes.incrementAndGet();
      }
    });

    // Return majority vote
    return upVotes.get() >= trees.size() / 2.0 ? 1 : 0;
  }

  public double predictProbability(double[] features) {
    if (trees.isEmpty()) {
      throw new IllegalStateException("Model must be trained before making predictions");
    }

    // Count positive predictions
    AtomicInteger upVotes = new AtomicInteger(0);
    trees.parallelStream().forEach(tree -> {
      if (tree.predict(features) == 1) {
        upVotes.incrementAndGet();
      }
    });

    // Return probability of positive class
    return upVotes.get() / (double) trees.size();
  }

  private static class DecisionTree {
    private Node root;
    private final int maxDepth;
    private final int numFeatures;
    private final int minSamplesSplit;
    private final int minSamplesLeaf;
    private final Random random;

    public DecisionTree(int maxDepth, int numFeatures, int minSamplesSplit, int minSamplesLeaf) {
      this.maxDepth = maxDepth;
      this.numFeatures = numFeatures;
      this.minSamplesSplit = minSamplesSplit;
      this.minSamplesLeaf = minSamplesLeaf;
      this.random = new Random();
    }

    public void train(double[][] features, int[] labels) {
      root = buildTree(features, labels, 0);
    }

    private Node buildTree(double[][] features, int[] labels, int depth) {
      if (features.length < minSamplesSplit || depth >= maxDepth) {
        return new LeafNode(calculateMajorityClass(labels));
      }

      // Find best split
      Split bestSplit = findBestSplit(features, labels);
      if (bestSplit == null) {
        return new LeafNode(calculateMajorityClass(labels));
      }

      // Create child nodes
      List<Integer> leftIndices = new ArrayList<>();
      List<Integer> rightIndices = new ArrayList<>();

      for (int i = 0; i < features.length; i++) {
        if (features[i][bestSplit.featureIndex] <= bestSplit.threshold) {
          leftIndices.add(i);
        } else {
          rightIndices.add(i);
        }
      }

      // Check minimum samples leaf condition
      if (leftIndices.size() < minSamplesLeaf || rightIndices.size() < minSamplesLeaf) {
        return new LeafNode(calculateMajorityClass(labels));
      }

      // Create split datasets
      double[][] leftFeatures = new double[leftIndices.size()][];
      int[] leftLabels = new int[leftIndices.size()];
      double[][] rightFeatures = new double[rightIndices.size()][];
      int[] rightLabels = new int[rightIndices.size()];

      for (int i = 0; i < leftIndices.size(); i++) {
        leftFeatures[i] = features[leftIndices.get(i)];
        leftLabels[i] = labels[leftIndices.get(i)];
      }

      for (int i = 0; i < rightIndices.size(); i++) {
        rightFeatures[i] = features[rightIndices.get(i)];
        rightLabels[i] = labels[rightIndices.get(i)];
      }

      // Recursively build child nodes
      Node leftChild = buildTree(leftFeatures, leftLabels, depth + 1);
      Node rightChild = buildTree(rightFeatures, rightLabels, depth + 1);

      return new SplitNode(bestSplit.featureIndex, bestSplit.threshold, leftChild, rightChild);
    }

    private Split findBestSplit(double[][] features, int[] labels) {
      if (features.length == 0 || features[0].length == 0) {
        return null;
      }

      Split bestSplit = null;
      double bestGini = Double.POSITIVE_INFINITY;

      // Randomly select features to consider
      List<Integer> featureIndices = new ArrayList<>();
      for (int i = 0; i < features[0].length; i++) {
        featureIndices.add(i);
      }

      for (int i = 0; i < Math.min(numFeatures, features[0].length); i++) {
        int randomIndex = random.nextInt(featureIndices.size());
        int featureIndex = featureIndices.get(randomIndex);
        featureIndices.remove(randomIndex);

        // Find min and max values for the feature
        double minVal = Double.POSITIVE_INFINITY;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (double[] feature : features) {
          minVal = Math.min(minVal, feature[featureIndex]);
          maxVal = Math.max(maxVal, feature[featureIndex]);
        }

        // Try different thresholds
        for (int j = 0; j < 10; j++) {
          double threshold = minVal + (maxVal - minVal) * random.nextDouble();
          double gini = calculateGiniImpurity(features, labels, featureIndex, threshold);

          if (gini < bestGini) {
            bestGini = gini;
            bestSplit = new Split(featureIndex, threshold);
          }
        }
      }

      return bestSplit;
    }

    private double calculateGiniImpurity(double[][] features, int[] labels,
                                         int featureIndex, double threshold) {
      int leftCount = 0, leftPositive = 0;
      int rightCount = 0, rightPositive = 0;

      for (int i = 0; i < features.length; i++) {
        if (features[i][featureIndex] <= threshold) {
          leftCount++;
          if (labels[i] == 1) leftPositive++;
        } else {
          rightCount++;
          if (labels[i] == 1) rightPositive++;
        }
      }

      if (leftCount < minSamplesLeaf || rightCount < minSamplesLeaf) {
        return Double.POSITIVE_INFINITY;
      }

      double leftGini = 1.0;
      double rightGini = 1.0;

      if (leftCount > 0) {
        double leftProp = (double) leftPositive / leftCount;
        leftGini = leftProp * (1 - leftProp);
      }

      if (rightCount > 0) {
        double rightProp = (double) rightPositive / rightCount;
        rightGini = rightProp * (1 - rightProp);
      }

      return (leftCount * leftGini + rightCount * rightGini) / features.length;
    }

    private int calculateMajorityClass(int[] labels) {
      int positiveCount = 0;
      for (int label : labels) {
        if (label == 1) positiveCount++;
      }
      return positiveCount >= labels.length / 2.0 ? 1 : 0;
    }

    public int predict(double[] features) {
      return root.predict(features);
    }

    private interface Node {
      int predict(double[] features);
    }

    private static class SplitNode implements Node {
      private final int featureIndex;
      private final double threshold;
      private final Node leftChild;
      private final Node rightChild;

      public SplitNode(int featureIndex, double threshold, Node leftChild, Node rightChild) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
      }

      @Override
      public int predict(double[] features) {
        if (features[featureIndex] <= threshold) {
          return leftChild.predict(features);
        } else {
          return rightChild.predict(features);
        }
      }
    }

    private static class LeafNode implements Node {
      private final int prediction;

      public LeafNode(int prediction) {
        this.prediction = prediction;
      }

      @Override
      public int predict(double[] features) {
        return prediction;
      }
    }

    private static class Split {
      private final int featureIndex;
      private final double threshold;

      public Split(int featureIndex, double threshold) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
      }
    }
  }
}
