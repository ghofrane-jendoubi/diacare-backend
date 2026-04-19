package tn.esprit.spring.diacarebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DiaCareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiaCareBackendApplication.class, args);
    }

}
