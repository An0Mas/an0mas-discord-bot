package com.an0mas.bot.webui;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalNavbarInfoAdvice {

	@ModelAttribute
	public void addNavbarAttributes(Model model, HttpSession session) {
	    boolean loggedIn = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
	    String username = (String) session.getAttribute("username");
	    String userAvatarUrl = (String) session.getAttribute("userAvatarUrl");

	    model.addAttribute("loggedIn", loggedIn);
	    model.addAttribute("username", username);
	    model.addAttribute("userAvatarUrl", userAvatarUrl);
	}
}
