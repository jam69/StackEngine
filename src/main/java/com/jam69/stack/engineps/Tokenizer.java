package com.jam69.stack.engineps;

public class Tokenizer{
	
	

	private static final String NumPattern="[0-9]+";
	private static final String StrPattern="[\"'].*[\"']";
	
	
	
	String txt;
	int p;
	String token;
	
	
	public Tokenizer(String s){
		txt=s;
		p=0;
	}
	
	protected Token nextToken() {
		String v= next();
//		System.out.println("NextToken:<"+v+">");
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
	
	
	private String next() {
		
		while(p<txt.length()) {
			char c = txt.charAt(p);
			// if is '%' ignorar hasta fin de linea
			if(Character.isWhitespace(c)) {
				p++;
			}else if(c=='\'') {
				return leeHasta('\'');
			}else if(c=='"') {
				return leeHasta('"');
			}else if(c=='/') {
				return leeHastaBlanco();
			}else {
				// if '(' lee hasta fin de linea e ignoralo
				return leeHastaBlanco();
			}
		}
		return null;
	}
	
	private String leeHasta(char f) {
		int a=p;
		p++;
		while(p<txt.length()) {
			char c = txt.charAt(p);
			if(c!=f) {
				p++;
			}else {
				p++;
				token=txt.substring(a, p);
				return token;
			}
		}
		token = txt.substring(a); 
		return token;
	}
	
	private String leeHastaBlanco() {
		
		int a=p;
		p++;
		while(p<txt.length()) {
			char c = txt.charAt(p);
			if(Character.isWhitespace(c)) {
				token=txt.substring(a, p);
				p++;
				return token;
			}else {
				p++;
			}
		}
		token = txt.substring(a); // p)?
		return token;
	}
}