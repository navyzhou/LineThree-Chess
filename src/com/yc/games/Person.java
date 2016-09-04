package com.yc.games;

/**
 * 对战用户信息
 * @author navy
 *
 */
public class Person {
	private String name;  //我的名字
	private String otherName; //对战好友的名字
	
	@Override
	public String toString() {
		return name+"\t"+otherName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOtherName() {
		return otherName;
	}

	public void setOtherName(String otherName) {
		this.otherName = otherName;
	}

	public Person(String name, String otherName) {
		super();
		this.name = name;
		this.otherName = otherName;
	}

	public Person() {
		super();
	}
}
