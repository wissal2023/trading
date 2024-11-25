package tn.esprit.similator.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ParameterSet {
  private Map<String, Object> parameters = new HashMap<>();
  public ParameterSet add(String key, Object value) {
    parameters.put(key, value);
    return this;
  }
  public int getIntValue(String key) {
    return ((Number) parameters.get(key)).intValue();
  }
  public double getDoubleValue(String key) {
    return ((Number) parameters.get(key)).doubleValue();
  }
  public Map<String, Object> toMap() {
    return new HashMap<>(parameters);
  }
}
