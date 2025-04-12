package com.an0mas.bot.webui;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.an0mas.bot.database.FeedbackDatabaseHelper;
import com.an0mas.bot.model.FeedbackEntry;

/**
 * 🌐 フィードバック一覧をブラウザで表示する簡易WebUI
 */
@Controller
public class FeedbackWebController {

	private static final int FEEDBACKS_PER_PAGE = 5; // ✅ 1ページあたりの表示数

	@GetMapping("/")
	public String index() {
		return "index"; // → templates/index.html
	}

	@GetMapping("/feedbacks")
	public String feedbackList(
			@RequestParam(name = "page", defaultValue = "1") int page,
			Model model) {

		final int pageSize = 5; // 1ページに表示する件数
		int total = FeedbackDatabaseHelper.getFeedbackCount();
		int totalPages = (int) Math.ceil((double) total / pageSize);

		// ページが範囲外にならないように制御
		page = Math.max(1, Math.min(page, totalPages));

		int offset = (page - 1) * pageSize;
		List<FeedbackEntry> feedbacks = FeedbackDatabaseHelper.getFeedbacksPaged(offset, pageSize);

		model.addAttribute("feedbacks", feedbacks);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", total);
		model.addAttribute("startCount", offset + 1);
		model.addAttribute("endCount", Math.min(offset + pageSize, total));

		return "feedbacks";
	}

	@PostMapping("/feedbacks/delete")
	public String deleteFeedback(@RequestParam("id") int id, Model model) {
		FeedbackDatabaseHelper.deleteFeedbackById(id);
		model.addAttribute("message", "✅ フィードバックを削除しました。");
		return "redirect:/feedbacks"; // フィードバック一覧にリダイレクト
	}
}
