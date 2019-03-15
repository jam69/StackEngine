


package com.jam69.stack.inprogress;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


interface Context {
	Object pop();
	void push(Object v);
}

interface IWord {
	public void compile(Context ctx);
	public void process(Context ctx);
}
abstract class Word {
	public Word compile(Context ctx) {
		return this;
	}
	abstract public void process(Context ctx);
}





public class StackEngine3 implements Context {
	

	
	protected Tokenizer tokenizer;
	protected LinkedList<Object> stack=new LinkedList<Object>();
	protected Map<String,Word> dict = new HashMap<String,Word>();
	protected boolean trace=false;
	
	
	class NumberCte extends Word {
		private final Number value;
		public NumberCte(Number n) {
			this.value=n;
		}
		public void process(Context ctx) {
			stack.push(value);
		}
	}
	class StringCte extends Word {
		private final String value;
		public StringCte(String s) {
			this.value=s;
		}
		public void process(Context ctx) {
			stack.push(value);
		}
	}
	
	class Dot extends Word {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			System.out.println("-->>"+op1);
		}
	}
	/**
	 * (n -- n n)
	 * 
	 *
	 */
	class Dup extends Word {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			stack.push(op1);
			stack.push(op1);
		}
	}
	
	/**
	 * (n -- )
	 */
	class Drop extends Word {
		public void process(Context ctx) {
			stack.pop();
		}
	}
	class Equals extends Word {
		public void process(Context ctx) {
			Object op1 = stack.pop();
			Object op2 = stack.pop();
			stack.push(op1.equals(op2));
		}
	}
	class OperatorSum extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()+op2.doubleValue();
			ctx.push(r);
		}
	}
	class OperatorLT extends Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) ctx.pop();
			Comparable op2 = (Comparable) ctx.pop();
			stack.push(op1.compareTo(op2)==1);
		}
	}
	
	
	class OperatorIf extends Word{
		
		public Word compile(Context ctx) {
			List<Word>[] parts=getSubroutine("ELSE","THEN");
			return new ExecutableIf(parts[0],parts[1]);
		}
		
		public void process(Context ctx) {
			System.err.println("NO excutable");
		}
	}
	
	class ExecutableIf extends Word{
		private List<Word> thenPart;
		private List<Word> elsePart;
		
		public ExecutableIf(List<Word> thenPart,List<Word> elsePart) {
			this.thenPart=thenPart;
			this.elsePart=elsePart;
		}
		
		public Word compile(Context ctx) {
			System.err.println("NO compilable");
			return null;
		}
		
		public void process(Context ctx) {
			Boolean op1 = (Boolean)stack.pop();
			if(op1) {
				StackEngine3.this.process(thenPart);
			}else {
				if(elsePart!=null) {
					StackEngine3.this.process(elsePart);
				}
			}
		}
	}
	
	class Subroutine2 extends Word{
		
		private final List<Word> program; // inmutable
		
		Subroutine2(List<Word> program){
			this.program=program;
		}
		public void process(Context ctx) {
			StackEngine3.this.process(program);
		}
	}
	
	class OperatorDef extends Word {
		
		private List<Word> program;
		
		public Word compile(Context ctx) {
			String name=nextName();
			program=getSubroutine(";");
			Word w=new Subroutine2(program);
			dict.put(name,w);
			return w; // 
		}
		public void process(Context ctx) {
			StackEngine3.this.process(program);
		}
	}
	
	public StackEngine3() {
		dict.put(".", new Dot());
		dict.put("DUP", new Dup());
		dict.put("DROP", new Drop());
		dict.put("=", new Equals());
		dict.put("<", new OperatorLT());
		dict.put("+", new OperatorSum());
		dict.put("IF",new OperatorIf());
		dict.put("DEF", new OperatorDef());
	}


	@Override
	public Object pop() {
		return stack.pop();
	}

	@Override
	public void push(Object v) {
		stack.push(v);
	}
	
	public void addOperator(String simb,Word ope) {
		dict.put(simb, ope);
	}
	
	public String nextName()  {
		Token t=tokenizer.nextToken();
		if(t.type !=Token.Type.STR) {
			//throw new StackEngineException("Falta un id");
			System.err.println(" Falta un id");
		}
		return (String) t.value;
	}
	
	public List<Word> getSubroutine(String end)  { // compile
		List<Word> list=new ArrayList<Word>();
		Token token = tokenizer.nextToken();
		while(token.type!=Token.Type.EOF) {
			if(token.type == Token.Type.WORD) {
				if(token.value.equals(end)) {
					return list;
				}
				if(token.type==Token.Type.NUM) {
					list.add(new NumberCte((Number)token.value));
				}
				if(token.type==Token.Type.STR) {
					list.add(new StringCte((String)token.value));
				}
				Word ope = dict.get(token.value);
				if(ope!=null) {
					list.add(ope.compile(this));
				}else {
					// Unkwnow word
				}
			}
			
			token=tokenizer.nextToken();
		}
		System.err.println(" Falta un final ["+end+"]");
		return null;
	}
	public List<Word>[] getSubroutine(String elseSym,String thenSym)  {
		List<Word> list=new ArrayList<Word>();
		List<Word> auxList=null;
//		boolean inThen=true;
		boolean skip=false;
		Token token = tokenizer.nextToken();
		while(token.type!=Token.Type.EOF) {
			if(token.type == Token.Type.WORD) {
				if(token.value.equals(thenSym)) {
					List<Word>[] ret=new List[2];
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
					list=new ArrayList<Word>();
				}
				Word ope = dict.get(token.value);
				if(ope!=null) {
					list.add(ope.compile(this));
				}
				
			}
			if(!skip) {
				if(token.type==Token.Type.NUM) {
					list.add(new NumberCte((Number)token.value));
				}
				if(token.type==Token.Type.STR) {
					list.add(new StringCte((String)token.value));
				}
			}
			skip=false;
			
			token=tokenizer.nextToken();
		}
		System.err.println(" Falta un final ["+thenSym+"]");
		return null;
	}
	
	private void dumpStack() {
		System.out.print("STACK:[");
		for(Object obj:stack) {
			System.out.print(obj);
			System.out.print(",");
		}
		System.out.println("]");
	}
	
	protected Word compile(Token token) throws StackEngineException {
		if(trace) {
			System.out.print("Compile:"+token);
		}
		if(token.type==Token.Type.NUM) {
			return new NumberCte((Number)token.value);
		}
		if(token.type==Token.Type.STR) {
			return new StringCte((String)token.value);
		}
		if(token.type==Token.Type.WORD) {
			Word ope = dict.get(token.value);
			if(ope!=null) {
				return ope.compile(this);
			}else {
				throw new StackEngineException("NO se que hacer con ["+token.value+"]");
			}
		}
		throw new StackEngineException("NO se que hacer con XX ["+token.value+"]");
	}
	
	protected void interpret(Token token) throws StackEngineException {
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
			Word ope = dict.get(token.value);
			if(ope!=null) {
				ope.process(this);
			}else {
				throw new StackEngineException("NO se que hacer con ["+token.value+"]");
			}
		}
	}
	
	public Object interpret(String s) throws StackEngineException  {
		System.out.println("Processing:"+s);
		tokenizer =  new Tokenizer(s); 
		Token token = tokenizer.nextToken();
		while(token.type!=Token.Type.EOF) {
			interpret(token);
			token=tokenizer.nextToken();
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
	
	public List<Word> compile(String s) throws StackEngineException  {
		System.out.println("Compiling:"+s);
		List<Word> program=new ArrayList<Word>();
		tokenizer =  new Tokenizer(s); 
		Token token = tokenizer.nextToken();
		while(token.type!=Token.Type.EOF) {
			program.add(compile(token));
			token=tokenizer.nextToken();
		}
		return program;
	}
	
	public void process(List<Word> rutina)   {
		for(Word w:rutina) {
			w.process(this);
		}
	}
	
	public static void main(String[] args) throws StackEngineException {
		
		StackEngine3 scp =new StackEngine3();
		scp.interpret(" 'dos' " );
		scp.interpret(" 1 2 + " );
		scp.interpret(" 1 2 = " );
		scp.process(scp.compile(" 1 2 = IF 'iguales' THEN" ));
		scp.process(scp.compile(" 1 1 = IF 'iguales' THEN" ));
		scp.process(scp.compile(" 1 1 = IF 'distintos' ELSE 'iguales' THEN ." ));
		scp.process(scp.compile(" 1 2 = IF 'distintos' ELSE 'iguales' THEN ." ));
		scp.process(scp.compile(" 1  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN . " ));
		scp.process(scp.compile(" 4  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN ." ));
		scp.process(scp.compile(" 9  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN ." ));
//		scp.process(scp.compile(" DEF MAS2 2 + ; 3 MAS2 . 5 MAS2 ." ));
		
	}







}
