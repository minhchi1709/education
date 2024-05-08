package com.mchis;

import com.mchis.course.CourseRepository;
import com.mchis.role.Role;
import com.mchis.role.RoleRepository;
import com.mchis.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean CommandLineRunner commandLineRunner(
			UserRepository userRepository,
			CourseRepository courseRepository,
			RoleRepository roleRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(Role.builder()
						.name("USER")
						.createdDate(LocalDateTime.now()).build());
			}
		};
	}
}
