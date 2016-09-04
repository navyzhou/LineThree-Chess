package com.yc.games;

/**
 * 用户对战信息
 * @author navy
 *
 */
public class Record {
	private String name;  //挑战方名字
	private String otherName; //迎战方名字
	
	//private int num1=9; //挑战方剩余落子数
	//private int num2=9; //迎战方剩余落子数
	
//	private int count1=9;  //挑战方剩余棋子数
//	private int count2=9;  //迎战方剩余棋子数
	
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

	public Record() {
		super();
	}
	
	public Record(Person p) {
		super();
		this.name = p.getName();
		this.otherName = p.getOtherName();
	}

	public Record(String name, String otherName) {
		super();
		this.name = name;
		this.otherName = otherName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((otherName == null) ? 0 : otherName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Record other = (Record) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (otherName == null) {
			if (other.otherName != null)
				return false;
		} else if (!otherName.equals(other.otherName))
			return false;
		return true;
	}
}
