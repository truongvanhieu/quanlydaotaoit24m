package vn.edu.quanlydaotao.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/")
	public String home() {
		return "redirect:/dashboard";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("pageTitle", "Tổng quan");
		model.addAttribute("activePage", "dashboard");
		return "dashboard";
	}

	@GetMapping("/majors")
	public String majors(Model model) {
		return management(model, "majors", "majors", "Ngành đào tạo");
	}

	@GetMapping("/courses")
	public String courses(Model model) {
		return management(model, "courses", "courses", "Môn học / học phần");
	}

	@GetMapping("/training-programs")
	public String trainingPrograms(Model model) {
		return management(model, "training-programs", "training-programs", "Chương trình đào tạo");
	}

	@GetMapping("/program-courses")
	public String programCourses(Model model) {
		return management(model, "program-courses", "program-courses", "Môn học trong chương trình");
	}

	@GetMapping("/prerequisites")
	public String prerequisites(Model model) {
		return management(model, "prerequisites", "prerequisites", "Môn học tiên quyết");
	}

	private String management(Model model, String pageKey, String activePage, String pageTitle) {
		model.addAttribute("pageKey", pageKey);
		model.addAttribute("activePage", activePage);
		model.addAttribute("pageTitle", pageTitle);
		return "management";
	}
}
