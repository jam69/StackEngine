package com.jam69.stack.engine;




import java.util.ArrayList;
import java.util.List;


//class StackEngineException extends RuntimeException{
//	StackEngineException(String msg){
//		super(msg);
//	}
//}


public class StackEngine2 extends StackEngine {
	
		
	/**
	 * (n -- )  Y pinta por la pantalla n
	 */
	class Dot implements Operator {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			String x ="["+op1+"]";
			System.out.println(x);
		}
	}

	/**
	 * (n1 n2 -- n2 n1)
	 */
	class Swap implements Operator {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			Object op2 = stack.pop();
			stack.push(op1);
			stack.push(op2);
		}
	}
	
	/**
	 * (n -- n n)
	 */
	class Dup implements Operator {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			stack.push(op1);
			stack.push(op1);
		}
	}
	
	/**
	 * (n -- )
	 */
	class Drop implements Operator {
		public void process(Context ctx) {
			stack.pop();
		}
	}
	
	/**
	 *  (a b -- a b a)
	 */
	class Over implements Operator {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			Object op2 = stack.pop();
			stack.push(op1);
			stack.push(op2);
			stack.push(op1);
		}
	}
	
	/**
	 * (a b c -- b c a)
	 */
	class Rot implements Operator {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			Object op2 = stack.pop();
			Object op3 = stack.pop();
			stack.push(op1);
			stack.push(op3);
			stack.push(op2);
		}
	}
	
	class Equals implements Operator {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			Object op2 = stack.pop();
			stack.push(op1.equals(op2));
		}
	}
	class GreaterThan implements Operator {
		public void process(Context ctx) {
			Comparable op1 = (Comparable)stack.pop();
			Comparable op2 = (Comparable)stack.pop();
			stack.push(op1.compareTo(op2)>0);
		}
	}
	class GreaterOrEquals implements Operator {
		public void process(Context ctx) {
			Comparable op1 = (Comparable)stack.pop();
			Comparable op2 = (Comparable)stack.pop();
			stack.push(op1.compareTo(op2)>=0);
		}
	}
	class Sum implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()+op2.doubleValue();
			ctx.push(r);
		}
	}
	class Sub implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()-op2.doubleValue();
			ctx.push(r);
		}
	}
	class Mul implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()*op2.doubleValue();
			ctx.push(r);
		}
	}
	class Div implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()/op2.doubleValue();
			ctx.push(r);
		}
	}
	
	
	class OperatorIf implements Operator {
		public void process(Context ctx) {
			Boolean op1 = (Boolean)stack.pop();
			List<Token>[] elsePart=compileIf();
			if(op1) {
				StackEngine2.this.process(elsePart[0]);
			}else {
				if(elsePart[1]!=null) {
					StackEngine2.this.process(elsePart[1]);
				}
			}
		}
	}
	
	class Subroutine implements Operator{
		private final List<Token> program; // inmutable
		Subroutine(List<Token> program){
			this.program=program;
		}
		public void process(Context ctx) {
			StackEngine2.this.process(program);
		}
	}
	
	class OperatorDef implements Operator {
		public void process(Context ctx) {
			String name=nextName();
			List<Token> program=getSubroutine(";");
			dict.put(name,new Subroutine(program));
		}
	}
	
	public void addForhOperators() {
		dict.put(".",new Dot());
		dict.put("SWAP",new Swap());
		dict.put("DUP",new Dup());
		dict.put("DROP",new Drop());
		dict.put("OVER",new Over());
		dict.put("ROT",new Rot());
		dict.put("=",new Equals());
		dict.put(">",new GreaterThan());
		dict.put("+",new Sum());
		dict.put("-",new Sub());
		dict.put("*",new Mul());
		dict.put("/",new Div());
		dict.put(">=",new GreaterOrEquals());	
		dict.put("IF",new OperatorIf());
		dict.put(":",new OperatorDef());
		
	}
	
	public StackEngine2() {
		addForhOperators();
	}
	
	public String nextName()  {
		Token t=nextToken();
		if(t.type !=Token.Type.STR) {
			//throw new StackEngineException("Falta un id");
			System.err.println(" Falta un id");
		}
		return (String) t.value;
	}
	
	public List<Token> getSubroutine(String end)  { // compile
		List<Token> list=new ArrayList<Token>();
		Token token = nextToken();
		while(token.type!=Token.Type.EOF) {
			if(token.type == Token.Type.WORD) {
				if(token.value.equals(end)) {
					return list;
				}
			}
			list.add(token);
			token=nextToken();
		}
		System.err.println(" Falta un final ["+end+"]");
		return null;
	}
	public List<Token>[] compileIf()  {
		final String elseSym="ELSE";
		final String thenSym="THEN";
		List<Token> list=new ArrayList<Token>();
		List<Token> auxList=null;
//		boolean inThen=true;
		boolean skip=false;
		Token token = nextToken();
		while(token.type!=Token.Type.EOF) {
			if(token.type == Token.Type.WORD) {
				if(token.value.equals(thenSym)) {
					List<Token>[] ret=new List[2];
					if(auxList==null) {
						ret[0]=list;
						ret[1]=null;	
					}else {
						ret[0]=list;
						ret[1]=auxList;
					}
					
					return ret;
				}
				if(token.value.equals(elseSym)) {
//					inThen=false;
					skip=true;
					auxList=list;
					list=new ArrayList<Token>();
				}
			}
			if(!skip) {
				list.add(token);
			}
			skip=false;
			
			token=nextToken();
		}
		System.err.println(" Falta un final ["+thenSym+"]");
		return null;
	}
	

	

	
	public void process(List<Token> rutina)   {
		for(Token t:rutina) {
			process(t);
		}
	}
	

	
	public static void main(String[] args) throws StackEngineException {
		
		StackEngine2 scp =new StackEngine2();
		
//		scp.process(" 'dos' 'tres' = "  );
//		scp.process("'X' 2 3 = IF 'Distintos' THEN ");
//		scp.process(" 2 2 = IF 'Iguales' THEN ");
//		scp.process(" 2 3 = IF 'Distintos' ELSE 'Iguales' THEN ");
//		scp.process(" 2 2 = IF 'Distintos' ELSE 'Iguales' THEN ");
		scp.process(" : C 2 + ; ");
		scp.process(" 7 C ");
		scp.process(" 4 C ");
	}







}
