package com.hello.forum.member.service;

import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hello.forum.beans.SHA;
import com.hello.forum.exceptions.AlreadyUseException;
import com.hello.forum.exceptions.UserIdendifyNotMatchException;
import com.hello.forum.member.dao.MemberDao;
import com.hello.forum.member.dao.MemberDaoImpl;
import com.hello.forum.member.vo.MemberVO;

@SpringBootTest	// jUnit 테스트를 위해 필요한 Bean을 가져오기 위한 애노테이션
@ExtendWith(SpringExtension.class)	// Spring Test를 위해서 jUnit5를 사용하기 위한 설정
// MemberServiceImpl을 테스트 하기 위해 Import
// MemverSerivceImpl이 동작하기 위해 MemberDaoImpl을 Import
// MemverSerivceImpl이 동작하기 위해 SHA를 Import
@Import({MemberServiceImpl.class, MemberDaoImpl.class, SHA.class})
public class MemberServiceImplTest {

	/**
	 * Import 해온 MemberServiceImpl을 주입한다.
	 */
	@Autowired
	private MemberService memberService;
	
	/**
	 * MemberServiceImpl에 DI해주기 위한 MemberDao 선언
	 */
	@MockBean
	private MemberDao memberDao;
	
	/**
	 *  MemberServiceImpl에 DI해주기 위한 SHA 선언
	 */
//	@MockBean
//	private SHA sha;
	
	@Test
	@DisplayName("회원 ID 중복체크 테스트")
	public void checkAvailableEmailTest() {
		
		// MemberServiceImpl의 checkAvailableEmail(email) 함수를 테스트 하기 위해서
		// MemberDaoImpl의 getEmailCount(email)가 동작해야하는 방법을 작성한다.
		
		// 1. Given
		BDDMockito.given(this.memberDao.getEmailCount("user01@gmail.com")).willReturn(0);
		
		// 2. Given
		BDDMockito.given(this.memberDao.getEmailCount("user02@gmail.com")).willReturn(1);
		
		// 3. When
		boolean isAvailableEmail = this.memberService.checkAvailableEmail("user01@gmail.com");	// true
		
		// 4. Then
		// isAvailableEmail의 값이 true면 성공!
		// 아니라면 실패!
		Assertions.assertTrue(isAvailableEmail);
		
		// 5. When
		isAvailableEmail = this.memberService.checkAvailableEmail("user02@gmail.com");
		
		Assertions.assertFalse(isAvailableEmail);
		
		// 6. Verify
		Mockito.verify(this.memberDao).getEmailCount("user01@gmail.com");
		Mockito.verify(this.memberDao).getEmailCount("user02@gmail.com");
	}
	
	@Test
	@DisplayName("회원가입 실패 테스트")
	public void createNewMemberFailTest() {
		// 1. Given
		BDDMockito.given(this.memberDao.getEmailCount("user01@gmail.com")).willReturn(1);
		
		// 2. When
		MemberVO memberVO = new MemberVO();
		memberVO.setEmail("user01@gmail.com");
		
		// 작성된 테스트 코드는 AlreadyUseException이 발생해야한다!!
//		boolean isSuccess = this.memberService.createNewMember(memberVO);
		AlreadyUseException exception = Assertions.assertThrows(AlreadyUseException.class, 
				() -> this.memberService.createNewMember(memberVO));
		
		// 3. Then
		// 예상되는 예외 메시지와 실제 발생된 예외의메시지가 같은지 비교
		String message = "이미 사용중인 이메일입니다.";
		Assertions.assertEquals(message, exception.getMessage());
		
		// 4. Verify
		Mockito.verify(this.memberDao).getEmailCount("user01@gmail.com");
	}
	
	@Test
	@DisplayName("회원가입 성공 테스트")
	public void createNewMemberSuccessTest() {
		// 1. Given
		BDDMockito.given(this.memberDao.getEmailCount("user01@gmail.com")).willReturn(0);
		
		MemberVO memberVO = new MemberVO();
		memberVO.setEmail("user01@gmail.com");
		memberVO.setName("테스트 사용자");
		memberVO.setPassword("testpassword");
		memberVO.setConfirmPassword("testpassword");
		
		BDDMockito.given(this.memberDao.createNewMember(memberVO)).willReturn(1);
		
		// 2. When
		boolean isSuccess = this.memberService.createNewMember(memberVO);	// true;
		
		// 3. Then
		Assertions.assertTrue(isSuccess);	// 회원가입의 결과가 true인지 검증
		Assertions.assertNotNull(memberVO.getSalt());	// 비밀번호 암호화를 위한 salt가 생성되었는지 검증
		Assertions.assertNotEquals(memberVO.getPassword(), memberVO.getConfirmPassword());	// 비밀번호가 암호화되어 confirmPassword와 다른지 검증
		
		// 4. Verify
		Mockito.verify(this.memberDao).getEmailCount("user01@gmail.com");
		Mockito.verify(this.memberDao).createNewMember(memberVO);
	}
	
	@Test
	@DisplayName("회원가입 실패 테스트 1")
	public void getMemberFailTest1() {
		BDDMockito.given(this.memberDao.selectSalt("user01@gmail.com")).willReturn(null);
		
		MemberVO memberVO = new MemberVO();
		memberVO.setEmail("user01@gmail.com");
		memberVO.setPassword("testpassword");
		
		UserIdendifyNotMatchException exception = Assertions.assertThrows(UserIdendifyNotMatchException.class, () -> this.memberService.getMember(memberVO));
		
		Assertions.assertNotNull(exception);
		String message = "이메일 또는 비밀번호가 일치하지 않습니다.";
		Assertions.assertEquals(message, exception.getMessage());
		
		Mockito.verify(this.memberDao).selectSalt("user01@gmail.com");
	}
	
	@Test
	@DisplayName("회원가입 실패 테스트 2")
	public void getMemberFailTest2() {
		BDDMockito.given(this.memberDao.selectSalt("user01@gmail.com")).willReturn("qwerty");
		
		MemberVO memberVO = new MemberVO();
		memberVO.setEmail("user01@gmail.com");
		memberVO.setPassword("testpassword");
		
		BDDMockito.given(this.memberDao.selectMemberByEmailAndPassword(memberVO)).willReturn(null);
		
		UserIdendifyNotMatchException exception = Assertions.assertThrows(UserIdendifyNotMatchException.class, () -> this.memberService.getMember(memberVO));
		
		Assertions.assertNotNull(exception);
		String message = "이메일 또는 비밀번호가 일치하지 않습니다.";
		Assertions.assertEquals(message, exception.getMessage());
		
		Mockito.verify(this.memberDao).selectSalt("user01@gmail.com");
		Mockito.verify(this.memberDao).selectMemberByEmailAndPassword(memberVO);
	}
	
	@Test
	@DisplayName("회원가입 성공 테스트")
	public void getMemberSuccessTest() {
		BDDMockito.given(this.memberDao.selectSalt("user01@gmail.com")).willReturn("qwerty");
		MemberVO memberVO = new MemberVO();
		memberVO.setEmail("user01@gmail.com");
		memberVO.setPassword("testpassword");
		BDDMockito.given(this.memberDao.selectMemberByEmailAndPassword(memberVO)).willReturn(memberVO);
		
		MemberVO test = this.memberService.getMember(memberVO);
		
		Assertions.assertNotNull(test);
		Assertions.assertEquals(memberVO.getEmail(), test.getEmail());
		Assertions.assertNotEquals(test.getPassword(), "testpassword");
		
		Mockito.verify(this.memberDao).selectSalt("user01@gmail.com");
		Mockito.verify(this.memberDao).selectMemberByEmailAndPassword(memberVO);
	}
	
	@Test
	@DisplayName("회원 삭제 실패 테스트")
	public void deleteMeFailTest() {
		BDDMockito.given(this.memberDao.deleteMemberByEmail("user01@gmail.com")).willReturn(0);
		
		boolean isSuccess = this.memberService.deleteMe("user01@gmail.com");
		
		Assertions.assertFalse(isSuccess);
		
		Mockito.verify(this.memberDao).deleteMemberByEmail("user01@gmail.com");
	}
	
	@Test
	@DisplayName("회원 삭제 성공 테스트")
	public void deleteMeSuccessTest() {
		BDDMockito.given(this.memberDao.deleteMemberByEmail("user01@gmail.com")).willReturn(1);
		
		boolean isSuccess = this.memberService.deleteMe("user01@gmail.com");
		
		Assertions.assertTrue(isSuccess);
		
		Mockito.verify(this.memberDao).deleteMemberByEmail("user01@gmail.com");
	}
	
}
