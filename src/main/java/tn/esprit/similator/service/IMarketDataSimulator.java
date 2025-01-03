package tn.esprit.similator.service;

import java.util.List;

public interface IMarketDataSimulator {
    List<Double> simulateReturns(int days);
}
