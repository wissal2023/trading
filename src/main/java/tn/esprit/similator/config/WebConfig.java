package tn.esprit.similator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {


  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // Adjust the path as needed
      .allowedOrigins("http://localhost:4200") // Your Angular app's URL
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed methods
      .allowedHeaders("*")
      .allowCredentials(true)
      .maxAge(3600); // Cache preflight response for 1 hour
  }
}
