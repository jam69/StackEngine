package com.jam69.stack.engineps;

@SuppressWarnings("serial")
public class StackEngineException extends RuntimeException{
	
	StackEngineException(String msg){
		super(msg);
	}
	StackEngineException(String msg,Exception e){
		super(msg,e);
	}
	
}
