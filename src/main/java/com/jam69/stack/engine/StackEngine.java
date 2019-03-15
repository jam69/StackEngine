


package com.jam69.stack.engine;



import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

interface Context {
	Object pop();
	void push(Object v);
}

interface Operator {
	public void process(Context ctx);
}

class StackEngineException extends RuntimeException{
	StackEngineException(String msg){
		super(msg);
	}
}

class Token {
	enum Type {NUM,STR,WORD,EOF};
	Object value;
	Type type;
	
	Token(Type type,Object value){
		this.type=type;
		this.value=value;
	}
	public String toString() {
		return String.format("%10s %10s",type,value);
	}
}

public class StackEngine implements Context {
	
	
	private static final String NumPattern="[0-9]+";
	private static final String StrPattern="[\"'].*[\"']";
	
	class Tokenizer{
		String txt;
		int p;
		String token;
		
		Tokenizer(String s){
			txt=s;
			p=0;
		}
		
		String next() {
			
			while(p<txt.length()) {
				char c = txt.charAt(p);
				if(Character.isWhitespace(c)) {
					p++;
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
	

		
	
	protected Tokenizer tokenizer;
	protected LinkedList<Object> stack=new LinkedList<Object>();
	protected Map<String,Operator> dict = new HashMap<String,Operator>();
	protected boolean trace=false;
	
	
	public StackEngine() {
		
	}
	
	
	
	protected Token nextToken() {
		String v= tokenizer.next();
		if(v==null) {
			return new Token(Token.Type.EOF,null);
		}
		if(v.matches(NumPattern)) {
			return new Token(Token.Type.NUM,Integer.parseInt(v));
		}
		if(v.matches(StrPattern)) {
			return new Token(Token.Type.STR,v.substring(1, v.length()-1));
		}
		return new Token(Token.Type.WORD,v);
	}
	

	@Override
	public Object pop() {
		return stack.pop();
	}

	@Override
	public void push(Object v) {
		stack.push(v);
	}
	
	public void addOperator(String simb,Operator ope) {
		dict.put(simb, ope);
	}
	
	
	private void dumpStack() {
		System.out.print("STACK:[");
		for(Object obj:stack) {
			System.out.print(obj);
			System.out.print(",");
		}
		System.out.println("]");
	}
	
	protected void process(Token token) throws StackEngineException {
		if(trace) {
			System.out.print("Step:"+token.type+" "+token.value+"| ");dumpStack();
		}
		if(token.type==Token.Type.NUM) {
			push(token.value);
		}
		if(token.type==Token.Type.STR) {
			push(token.value);
		}
		if(token.type==Token.Type.WORD) {
			Operator ope = dict.get(token.value);
			if(ope!=null) {
				ope.process(this);
			}else {
				throw new StackEngineException("NO se que hacer con ["+token.value+"]");
			}
		}
	}
	
	public Object process(String s) throws StackEngineException  {
		System.out.println("Processing:"+s);
		tokenizer =  new Tokenizer(s); 
		Token token = nextToken();
		while(token.type!=Token.Type.EOF) {
			process(token);
			token=nextToken();
		}
		if(stack.isEmpty()) {
			System.out.println("End Processing:"+"EMPTY");
			return null;
		}else {
			Object r=stack.pop();
			System.out.println("End Processing:"+r);
			return r;
		}
		
	}
	
	public static void main(String[] args) throws StackEngineException {
		
		StackEngine scp =new StackEngine();
//		scp.process(" 'dos' 'tres' = "  );
	}







}
