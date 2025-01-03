package tn.esprit.similator.service;

import java.util.List;

public interface IRiskAnalysisService {
    double calculateVaR(List<Double> returns, double confidenceLevel);
}
