package com.an0mas.bot.webui;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    // ✅ セッションを破棄してログアウト
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // セッション削除！
        return "redirect:/";  // トップページなどへリダイレクト
    }
}
