package com.an0mas.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll()
			)
			.csrf(csrf -> csrf.disable())
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/") // ← ★ログアウト後にリダイレクトするURLを明示！
			);
		return http.build();
	}
}
