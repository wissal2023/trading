package tn.esprit.similator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class similatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(similatorApplication.class, args);
	}

}
