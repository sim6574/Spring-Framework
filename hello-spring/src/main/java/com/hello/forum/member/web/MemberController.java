package com.hello.forum.member.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.hello.forum.member.service.MemberService;
import com.hello.forum.member.vo.MemberVO;
import com.hello.forum.utils.AjaxResponse;
import com.hello.forum.utils.StringUtils;
import com.hello.forum.utils.ValidationUtils;
import com.hello.forum.utils.Validator;
import com.hello.forum.utils.Validator.Type;

import jakarta.servlet.http.HttpSession;

@Controller
public class MemberController {
	
	private Logger logger = LoggerFactory.getLogger(MemberController.class);
	
	@Autowired
	private MemberService memberService;
	
	@GetMapping("/member/regist")
	public String viewRegistMemberPage() {		
		return "member/memberregist";
	}
	
	// http://localhost:8080/member/regist/available?email=aaa@aaa.com
	@ResponseBody	// 응답하는 데이터를 JSON으로 변환해 클라이언트에게 반환한다.
	@GetMapping("/ajax/member/regist/available")
	public Map<String, Object> checkAvailableEmail(@RequestParam String email) {
		
		// 사용가능한 이메일이라면 true, 아니라면 false
		boolean isAvailableEmail = this.memberService.checkAvailableEmail(email);
		
		/*
		 * { "email": "aaa@aaa.com", "available": false }
		 */
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("email", email);
		responseMap.put("available", isAvailableEmail);
		return responseMap;
	}
	
	@PostMapping("/member/regist")
	public String doRegistMember(MemberVO memberVO, Model model) {
		
		boolean isNotEmptyEmail = ValidationUtils.notEmpty(memberVO.getEmail());
		boolean isNotEmptyName = ValidationUtils.notEmpty(memberVO.getName());
		boolean isNotEmptyPassword = ValidationUtils.notEmpty(memberVO.getPassword());
		boolean isEmailFormat = ValidationUtils.email(memberVO.getEmail());
		
		boolean isEnoughSize = ValidationUtils.size(memberVO.getPassword(), 10);
		boolean isNotEmptyConfirmPassword = ValidationUtils.notEmpty(memberVO.getConfirmPassword());
		boolean isEqualPassword = ValidationUtils.isEquals(memberVO.getPassword(), memberVO.getConfirmPassword());
		boolean isPasswordFormat = StringUtils.correctPasswordFormat(memberVO.getPassword());
		
		if (!isNotEmptyEmail) {
			model.addAttribute("errorMessage", "이메일은 필수 입력 값입니다.");
			return "member/memberregist";
		}
		if (!isNotEmptyName) {
			model.addAttribute("errorMessage", "이름은 필수 입력 값입니다.");
			return "member/memberregist";
		}
		if (!isNotEmptyPassword) {
			model.addAttribute("errorMessage", "비밀번호는 필수 입력 값입니다.");
			return "member/memberregist";
		}
		
		if (!isEmailFormat) {
			model.addAttribute("errorMessage", "이메일을 올바른 형태로 작성해주세요.");
			return "member/memberregist";
		}
		
		if (!isEnoughSize) {
			model.addAttribute("errorMessage", "비밀번호는 10자리 이상이어야 합니다.");
			return "member/memberregist";
		}
		
		if (!isNotEmptyConfirmPassword) {
			model.addAttribute("errorMessage", "비밀번호 확인은 필수 입력 값입니다.");
			return "member/memberregist";
		}
		
		if (!isEqualPassword) {
			model.addAttribute("errorMessage", "대충 비밀번호가 다르다는 뜻");
			return "member/memberregist";
		}
		
		if (!isPasswordFormat) {
			model.addAttribute("errorMessage", "대충 비밀번호 형식이 틀렸다는 뜻");
		}
		
		boolean isCreateSuccess = this.memberService.createNewMember(memberVO);
		if (isCreateSuccess) {
			logger.info("멤버 등록 성공");
		}
		else {
			logger.info("멤버 등록 실패");
		}
		return "redirect:/member/login";
	}
	
	@GetMapping("/member/login")
	public String viewLoginPage() {
		return "member/memberlogin";
	}
	
	@ResponseBody
	@PostMapping("/ajax/member/login")
	public AjaxResponse doLogin(MemberVO memberVO, HttpSession session, 
								@RequestParam(defaultValue = "/board/list") String nextUrl) {
		
		logger.info("NextUrl: " + nextUrl);
		
		// Validation Check (파라미터 유효성 검사)
		Validator<MemberVO> validator = new Validator<>(memberVO);
		validator.add("email", Type.NOT_EMPTY, "이메일을 입력해주세요.")
				 .add("email", Type.EMAIL, "이메일 형식이 아닙니다.")
				 .add("password", Type.NOT_EMPTY, "비밀번호를 입력해주세요.").start();
		
		if (validator.hasErrors()) {
			Map<String, List<String>> errors = validator.getErrors();
			return new AjaxResponse().append("errors", errors);
		}
		
		MemberVO member = this.memberService.getMember(memberVO);
		
		// 로그인이 정상적으로 이루어졌다면 세션을 생성한다.
		session.setAttribute("_LOGIN_USER_", member);
		session.setMaxInactiveInterval(20 * 60);
		
		return new AjaxResponse().append("next", nextUrl);
	}
	
	@GetMapping("/member/logout")
	public String doLogout(HttpSession session) {
		// Logout 처리
		// SessionID로 전달된 세션의 모든 정보를 삭제
		session.invalidate();
		return "redirect:/board/list";
	}
	
	@ResponseBody
	@GetMapping("/ajax/member/delete-me")
	public AjaxResponse doDeleteMe(HttpSession session, @SessionAttribute("_LOGIN_USER_") MemberVO memberVO) {
		// 현재 로그인 되어있는 사용자의 정보
//		MemberVO memberVO = (MemberVO) session.getAttribute("_LOGIN_USER_");
		boolean isSuccess = this.memberService.deleteMe(memberVO.getEmail());
		
		if (isSuccess) {
			session.invalidate();
		}
		
		return new AjaxResponse().append("next", isSuccess ? "/member/success-delete-me" : "/member/fail-delete-me");
	}
	
	@GetMapping("/member/{result}-delete-me")
	public String viewDeleteMePage(@PathVariable String result) {
		result = result.toLowerCase();
		if (!result.equals("fail") && !result.equals("success")) {
			// result의 값이 fail, success가 아니면 404페이지 보여주기
			return "error/404";
		}
		
		return "member/" + result + "deleteme";
	}

}
