package com.an0mas.bot.webui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test-feedbacks")
    public String testFeedbacks() {
        return "feedbacks"; // templates/feedbacks.html を直接返す
    }
}