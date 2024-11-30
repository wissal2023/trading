package tn.esprit.similator.entity;

import java.util.*;

public class DecisionTree {

  private Node root;
  private final int maxDepth;
  private final int numFeatures;
  private final Random random;

  public DecisionTree(int maxDepth, int numFeatures) {
    this.maxDepth = maxDepth;
    this.numFeatures = numFeatures;
    this.random = new Random();
  }

  public void train(double[][] features, int[] labels) {
    root = buildTree(features, labels, 0);
  }

  public int predict(double[] features) {
    return predictRecursive(root, features);
  }

  private Node buildTree(double[][] features, int[] labels, int depth) {
    // Check stopping criteria
    if (depth >= maxDepth || isHomogeneous(labels)) {
      return new Node(getMajorityLabel(labels));
    }

    // Select random subset of features
    List<Integer> featureIndices = selectRandomFeatures();

    // Find best split
    SplitResult bestSplit = findBestSplit(features, labels, featureIndices);

    if (bestSplit == null) {
      return new Node(getMajorityLabel(labels));
    }

    // Create node and recursively build children
    Node node = new Node(bestSplit.featureIndex, bestSplit.threshold);

    // Split data
    List<Integer> leftIndices = new ArrayList<>();
    List<Integer> rightIndices = new ArrayList<>();

    for (int i = 0; i < features.length; i++) {
      if (features[i][bestSplit.featureIndex] <= bestSplit.threshold) {
        leftIndices.add(i);
      } else {
        rightIndices.add(i);
      }
    }

    // Build left and right subtrees
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

    node.setLeftChild(buildTree(leftFeatures, leftLabels, depth + 1));
    node.setRightChild(buildTree(rightFeatures, rightLabels, depth + 1));

    return node;
  }

  private List<Integer> selectRandomFeatures() {
    List<Integer> allFeatures = new ArrayList<>();
    for (int i = 0; i < numFeatures; i++) {
      allFeatures.add(i);
    }

    Collections.shuffle(allFeatures, random);
    return allFeatures.subList(0, Math.min(numFeatures, (int)Math.sqrt(numFeatures)));
  }

  private static class SplitResult {
    int featureIndex;
    double threshold;
    double gain;

    SplitResult(int featureIndex, double threshold, double gain) {
      this.featureIndex = featureIndex;
      this.threshold = threshold;
      this.gain = gain;
    }
  }

  private SplitResult findBestSplit(double[][] features, int[] labels, List<Integer> featureIndices) {
    SplitResult bestSplit = null;
    double bestGain = 0.0;

    for (int featureIndex : featureIndices) {
      // Get unique values for the feature
      Set<Double> uniqueValues = new HashSet<>();
      for (double[] feature : features) {
        uniqueValues.add(feature[featureIndex]);
      }

      // Try different thresholds
      for (double threshold : uniqueValues) {
        // Calculate information gain
        double gain = calculateInformationGain(features, labels, featureIndex, threshold);

        if (gain > bestGain) {
          bestGain = gain;
          bestSplit = new SplitResult(featureIndex, threshold, gain);
        }
      }
    }

    return bestSplit;
  }

  private double calculateInformationGain(double[][] features, int[] labels, int featureIndex, double threshold) {
    double parentEntropy = calculateEntropy(labels);

    List<Integer> leftIndices = new ArrayList<>();
    List<Integer> rightIndices = new ArrayList<>();

    // Split the data
    for (int i = 0; i < features.length; i++) {
      if (features[i][featureIndex] <= threshold) {
        leftIndices.add(i);
      } else {
        rightIndices.add(i);
      }
    }

    // Calculate weighted entropy of children
    double leftWeight = (double) leftIndices.size() / features.length;
    double rightWeight = (double) rightIndices.size() / features.length;

    int[] leftLabels = new int[leftIndices.size()];
    int[] rightLabels = new int[rightIndices.size()];

    for (int i = 0; i < leftIndices.size(); i++) {
      leftLabels[i] = labels[leftIndices.get(i)];
    }

    for (int i = 0; i < rightIndices.size(); i++) {
      rightLabels[i] = labels[rightIndices.get(i)];
    }

    double leftEntropy = calculateEntropy(leftLabels);
    double rightEntropy = calculateEntropy(rightLabels);

    return parentEntropy - (leftWeight * leftEntropy + rightWeight * rightEntropy);
  }

  private double calculateEntropy(int[] labels) {
    if (labels.length == 0) return 0;

    Map<Integer, Integer> counts = new HashMap<>();
    for (int label : labels) {
      counts.put(label, counts.getOrDefault(label, 0) + 1);
    }

    double entropy = 0.0;
    double n = labels.length;

    for (int count : counts.values()) {
      double p = count / n;
      entropy -= p * Math.log(p) / Math.log(2);
    }

    return entropy;
  }

  private boolean isHomogeneous(int[] labels) {
    if (labels.length == 0) return true;
    int first = labels[0];
    for (int label : labels) {
      if (label != first) return false;
    }
    return true;
  }

  private int getMajorityLabel(int[] labels) {
    Map<Integer, Integer> counts = new HashMap<>();
    for (int label : labels) {
      counts.put(label, counts.getOrDefault(label, 0) + 1);
    }

    return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
  }

  private int predictRecursive(Node node, double[] features) {
    if (node.isLeaf()) {
      return node.getPrediction();
    }

    if (features[node.getFeatureIndex()] <= node.getThreshold()) {
      return predictRecursive(node.getLeftChild(), features);
    } else {
      return predictRecursive(node.getRightChild(), features);
    }
  }
}
