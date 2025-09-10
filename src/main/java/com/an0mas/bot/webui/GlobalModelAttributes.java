package com.an0mas.bot.webui;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 🌐 全てのテンプレートに共通の属性を渡すためのアドバイスクラス
 */
@ControllerAdvice
public class GlobalModelAttributes {

	/**
	 * 📍 現在のパス（/feedbacks など）をテンプレートに渡す
	 */
	@ModelAttribute("currentPath")
	public String currentPath(HttpServletRequest request) {
		return request.getRequestURI();
	}
}
