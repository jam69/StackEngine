package com.jam69.stack.inprogress;

public class Token {
	
	public enum Type {NUM,STR,WORD,EOF};
	
	Object value;
	Type type;
	
	public Token(Type type,Object value){
		this.type=type;
		this.value=value;
	}
	public String toString() {
		return String.format("%10s %10s",type,value);
	}
}
