// ✅ ステップ②：Spring Boot 側で Discord OAuth2 を扱えるように準備する

// 🔧 今回は ConfigLoader.get("DISCORD_CLIENT_ID") などで環境変数を使う方式だよ！
// なので自前で OAuth2 を構成する必要があるけど、ちゃんと整理してやっていくね。

// 📁 構成（ファイル追加）
// ├── config/
// │   └── DiscordOAuth2Config.java   ← SpringのOAuth設定
// ├── controller/
// │   └── AuthController.java        ← ログイン・コールバック処理
// ├── service/
// │   └── DiscordOAuth2Service.java ← 認証フロー用ユーティリティ
//
// あと必要に応じて：
// └── model/DiscordUser.java        ← APIから取得したユーザー情報格納用

// =============================
// 📌 DiscordOAuth2Config.java
// =============================
package com.an0mas.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class DiscordOAuth2Config {

	@Bean
	public ClientRegistration discordClientRegistration() {
		return ClientRegistration.withRegistrationId("discord")
				.clientId(ConfigLoader.get("DISCORD_CLIENT_ID"))
				.clientSecret(ConfigLoader.get("DISCORD_CLIENT_SECRET"))
				.redirectUri(ConfigLoader.get("DISCORD_REDIRECT_URI"))
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.scope("identify")
				.authorizationUri("https://discord.com/api/oauth2/authorize")
				.tokenUri("https://discord.com/api/oauth2/token")
				.userInfoUri("https://discord.com/api/users/@me")
				.userNameAttributeName("id")
				.clientName("Discord")
				.build();
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository repo,
			OAuth2AuthorizedClientRepository clientRepo) {
		OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
				.authorizationCode()
				.build();

		DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(repo, clientRepo);
		manager.setAuthorizedClientProvider(provider);
		return manager;
	}
	
	public static HttpHeaders getTokenHeaders(String accessToken) {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Authorization", "Bearer " + accessToken);
	    return headers;
	}

	public static String getClientId() {
		return ConfigLoader.get("DISCORD_CLIENT_ID");
	}

	public static String getClientSecret() {
		return ConfigLoader.get("DISCORD_CLIENT_SECRET");
	}

	public static String getRedirectUri() {
		return ConfigLoader.get("DISCORD_REDIRECT_URI");
	}
	
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(discordClientRegistration());
	}

	@Bean
	public OAuth2AuthorizedClientRepository authorizedClientRepository(
			OAuth2AuthorizedClientService authorizedClientService) {

		return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
	}
}
