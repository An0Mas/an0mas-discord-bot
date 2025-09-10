package com.an0mas.bot.webui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.an0mas.bot.config.ConfigLoader;
import com.an0mas.bot.model.DiscordUser;



@Controller
public class OAuthController {

	private final String clientId = ConfigLoader.get("DISCORD_CLIENT_ID");
	private final String redirectUri = ConfigLoader.get("DISCORD_REDIRECT_URI");

	@GetMapping("/login")
	public void redirectToDiscord(HttpServletResponse response) throws IOException {
		String url = "https://discord.com/api/oauth2/authorize" +
				"?client_id=" + clientId +
				"&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
				"&response_type=code" +
				"&scope=identify";

		response.sendRedirect(url);
	}

	@GetMapping("/login/callback")
	public String handleCallback(@RequestParam("code") String code, HttpSession session, Model model)
			throws IOException {
		// 🔑 トークンを取得
		String token = exchangeCodeForToken(code);

		// 🧑‍💼 ユーザー情報を取得
		DiscordUser user = fetchUserInfo(token);

		// ✅ セッションに保存
		session.setAttribute("loggedIn", true);
		session.setAttribute("username", user.getUsername());
		session.setAttribute("userAvatarUrl", user.getAvatarUrl());

		return "redirect:/";
	}

	private String exchangeCodeForToken(String code) throws IOException {
		String clientId = ConfigLoader.get("DISCORD_CLIENT_ID");
		String clientSecret = ConfigLoader.get("DISCORD_CLIENT_SECRET");
		String redirectUri = ConfigLoader.get("DISCORD_REDIRECT_URI");

		URL url = new URL("https://discord.com/api/oauth2/token");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		String data = "client_id=" + URLEncoder.encode(clientId, "UTF-8")
				+ "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8")
				+ "&grant_type=authorization_code"
				+ "&code=" + URLEncoder.encode(code, "UTF-8")
				+ "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

		try (OutputStream os = conn.getOutputStream()) {
			os.write(data.getBytes(StandardCharsets.UTF_8));
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String response = br.lines().collect(Collectors.joining());
			JSONObject json = new JSONObject(response);
			return json.getString("access_token");
		}
	}

	private DiscordUser fetchUserInfo(String accessToken) throws IOException {
		URL url = new URL("https://discord.com/api/users/@me");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Bearer " + accessToken);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String response = br.lines().collect(Collectors.joining());
			JSONObject json = new JSONObject(response);
			String username = json.getString("username") + "#" + json.getString("discriminator");
			String id = json.getString("id");
			String avatar = json.getString("avatar");

			String avatarUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png";
			return new DiscordUser(username, avatarUrl);
		}
	}
}
