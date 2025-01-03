package tn.esprit.similator.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MarketDataSimulator  implements IMarketDataSimulator{

    @Override
    public List<Double> simulateReturns(int days) {
        List<Double> returns = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < days; i++) {
            // Simuler un rendement quotidien entre 1% et 5%
            double dailyReturn = 0.01 + (0.04 * random.nextDouble());
            returns.add(dailyReturn);
        }

        return returns;
    }
}
