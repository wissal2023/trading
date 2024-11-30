package tn.esprit.similator.entity;
import java.util.*;

class Node {

  private boolean isLeaf;
  private int prediction;
  private int featureIndex;
  private double threshold;
  private Node leftChild;
  private Node rightChild;

  // Constructor for leaf node
  public Node(int prediction) {
    this.isLeaf = true;
    this.prediction = prediction;
  }

  // Constructor for internal node
  public Node(int featureIndex, double threshold) {
    this.isLeaf = false;
    this.featureIndex = featureIndex;
    this.threshold = threshold;
  }

  public boolean isLeaf() {
    return isLeaf;
  }

  public int getPrediction() {
    return prediction;
  }

  public int getFeatureIndex() {
    return featureIndex;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setLeftChild(Node leftChild) {
    this.leftChild = leftChild;
  }

  public void setRightChild(Node rightChild) {
    this.rightChild = rightChild;
  }

  public Node getLeftChild() {
    return leftChild;
  }

  public Node getRightChild() {
    return rightChild;
  }
}
