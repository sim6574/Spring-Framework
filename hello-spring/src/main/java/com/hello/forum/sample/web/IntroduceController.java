package com.hello.forum.sample.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IntroduceController {
	
	@GetMapping("/introduce")
	public String viewIntroducePage(Model model) {
		model.addAttribute("name", "심규태");
		model.addAttribute("age", 31);
		model.addAttribute("residence", "서울");	
		return "introduce";
	}
}
