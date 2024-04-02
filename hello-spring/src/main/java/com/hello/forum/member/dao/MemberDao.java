package com.hello.forum.member.dao;

import com.hello.forum.member.vo.MemberVO;

public interface MemberDao {
	
	public String NAME_SPACE = "com.hello.forum.member.dao.MemberDao";
	
	public int getEmailCount(String email);
	
	public int createNewMember(MemberVO memberVO);

	public String selectSalt(String email);

	public MemberVO selectMemberByEmailAndPassword(MemberVO memberVO);

	public int deleteMemberByEmail(String email);

}
