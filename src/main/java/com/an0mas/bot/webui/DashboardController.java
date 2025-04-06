// 📦 パッケージ: com.an0mas.bot.webui
package com.an0mas.bot.webui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.an0mas.bot.database.DatabaseHelper;

/**
 * 🛠️ ダッシュボードコントローラー：ステータス表示やメンテナンス操作を行う
 */
@Controller
public class DashboardController {

	/**
	 * 📋 ダッシュボード表示（現在のモード状態を表示）
	 */
	@GetMapping("/dashboard")
	public String showDashboard(Model model) {
		boolean maintenanceMode = DatabaseHelper.isMaintenanceMode();
		model.addAttribute("maintenance", maintenanceMode);
		return "dashboard";
	}

	/**
	 * 🔁 メンテナンストグル切り替え
	 */
	@PostMapping("/dashboard/toggle-maintenance")
	public String toggleMaintenance() {
		boolean current = DatabaseHelper.isMaintenanceMode();
		DatabaseHelper.setMaintenanceMode(!current);
		return "redirect:/dashboard";
	}
}
