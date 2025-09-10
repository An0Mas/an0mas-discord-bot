package com.an0mas.bot.webui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 🏠 トップページ表示用のコントローラー
 */
@Controller
public class IndexController {

	@GetMapping("/")
	public String showHomePage() {
		return "index"; // templates/index.html を返す
	}
}
