package com.jam69.stack.engineps;

import java.io.IOException;
import java.io.Reader;



public class TokenizerFile implements Tokenizer {
	
	

	private static final String NumPattern="-?[0-9]+";
	private static final String StrPattern="[\"'].*[\"']";
	
	
	
	private Reader is;
	String token;
	int lastChar;
	
	public TokenizerFile(Reader is){
		this.is=is;
	}
	
	public Token nextToken() throws IOException {
		String v= next();
//		System.out.println("TOKEN:<"+v+">");
		if(v==null) {
			return new Token(Token.Type.EOF,null);
		}
		
		if(v.matches(NumPattern)) {
			return new Token(Token.Type.NUM,Integer.parseInt(v));
		}
		if(v.matches(StrPattern)) {
			return new Token(Token.Type.STR,v.substring(1, v.length()-1));
		}
		if(v.startsWith("/")) {
			return new Token(Token.Type.STR,v.substring(1));
		}
		return new Token(Token.Type.WORD,v);
	}
	
	private int getChar() throws IOException {
		lastChar=is.read();
		return lastChar;
	}
	
	
	private String next() throws IOException {
		
		int c2;
		while(  (c2=getChar())!=-1) {
			char c = (char)c2;
			if(c=='%') {
				leeHasta('\n');
			}else if(Character.isWhitespace(c)) {
				
			}else if(c=='\'') {				
				return leeHasta('\'');
			}else if(c=='"') {
				return leeHasta('"');
			}else {
				// if '(' lee hasta fin de linea e ignoralo
				return leeHastaBlanco();
			}
		}
		return null;
	}
	
	private String leeHasta(char f) throws IOException {
		StringBuilder sb=new StringBuilder();
		sb.append((char)lastChar);
		int c2;
		while(  (c2=getChar())!=-1) {
			char c = (char)c2;
			if(c!=f) {
				sb.append(c);
			}else {
				sb.append(c);
				token=sb.toString();
				return token;
			}
		}
		token =sb.toString();
		return token;
	}
	
	private String leeHastaBlanco() throws IOException {
		
		StringBuilder sb=new StringBuilder();
		sb.append((char)lastChar);
		int c2;
		while(  (c2=getChar())!=-1) {
			char c = (char)c2;
			if(Character.isWhitespace(c)) {
				token=sb.toString();
				return token;
			}else {
				sb.append(c);
			}
		}
		token = sb.toString();
		return token;
	}
}