package com.hello.forum.exceptions;

public class UserIdendifyNotMatchException extends RuntimeException {

	private static final long serialVersionUID = -1457359119683890515L;
	
	public UserIdendifyNotMatchException() {
		super("이메일 또는 비밀번호가 일치하지 않습니다.");
	}

}
