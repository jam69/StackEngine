


package com.jam69.stack.engineps;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	public void process(Context ctx);
}
abstract class Word {
	public Word compile(Context ctx) {
		return this;
	}
	abstract public void process(Context ctx);
}



public class StackEnginePS implements Context {
	

	
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
			push(value);
		}
		public String toString() {
			return ""+value;
		}
	}
	
	class StringCte extends Word {
		private final String value;
		public StringCte(String s) {
			this.value=s;
		}
		public void process(Context ctx) {
			push(value);
		}
		public String toString() {
			return "'"+value+"'";
		}
	}
	
	class True extends Word {
		public void process(Context ctx) {
			push(Boolean.TRUE);
		}
		public String toString() {
			return "/true";
		}
	}
	class False extends Word {
		public void process(Context ctx) {
			push(Boolean.FALSE);
		}
		public String toString() {
			return "/false";
		}
	}
	class Dot extends Word {
		public void process(Context ctx) {
			Object op1 = pop();
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
			Object op1 = pop();
			push(op1);
			push(op1);
		}
	}
	class Roll extends Word {
		public void process(Context ctx) {
			Object op1 = pop();
			Object op2 = pop();
			// op1 : direction and distance
			// op2 : num objects
		}
			
	}
	
	class Swap extends Word {
		public void process(Context ctx) {
			Object op1 = pop();
			Object op2 = pop();
			push(op1);
			push(op2);
		}
	}
	/**
	 * (n -- )
	 */
	class Drop extends Word {
		public void process(Context ctx) {
			pop();
		}
	}
	class Equals extends Word {
		public void process(Context ctx) {
			Object op1 = pop();
			Object op2 = pop();
			push(op1.equals(op2));
		}
		public String toString() {
			return "/eq " ;
		}
	}
	class OperatorNeg extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push(-op1.doubleValue());
		}
		public String toString() {
			return "/neg";
		}
	}
	class OperatorAbs extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Double r=Math.abs(op1.doubleValue());
			push(r);
		}
		public String toString() {
			return "/abs";
		}
	}
	class OperatorSum extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			Double r=op1.doubleValue()+op2.doubleValue();
			push(r);
		}
		public String toString() {
			return "/sum";
		}
	}
	class OperatorSub extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			Double r=op1.doubleValue()-op2.doubleValue();
			push(r);
		}
		public String toString() {
			return "/sub";
		}
	}
	class OperatorMul extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			Double r=op1.doubleValue()*op2.doubleValue();
			push(r);
		}
		public String toString() {
			return "/mul";
		}
	}
	class OperatorDiv extends Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			Double r=op1.doubleValue()/op2.doubleValue();
			push(r);
		}
		public String toString() {
			return "/div";
		}
	}
	class OperatorIDiv extends Word {
		public void process(Context ctx) {
			Long op1 = (Long) pop();
			Long op2 = (Long) pop();
			Long r=op1.longValue() / op2.longValue();
			push(r);
		}
		public String toString() {
			return "/idiv";
		}
	}
	class OperatorMod extends Word {
		public void process(Context ctx) {
			Long op1 = (Long) pop();
			Long op2 = (Long) pop();
			Long mod=op1.longValue() % op2.longValue();
			push(mod);
		}
		public String toString() {
			return "/mod";
		}
	}
	class OperatorLT extends Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) pop();
			Comparable op2 = (Comparable) pop();
			push(op1.compareTo(op2)>0);
		}
		public String toString() {
			return "/LT";
		}
	}
	class OperatorGT extends Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) pop();
			Comparable op2 = (Comparable) pop();
			push(op1.compareTo(op2)<0);
		}
		public String toString() {
			return "/GT";
		}
	}
	class OperatorGE extends Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) pop();
			Comparable op2 = (Comparable) pop();
			push(op1.compareTo(op2)<=0);
		}
		public String toString() {
			return "/GE";
		}
	}
	class OperatorLE extends Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) pop();
			Comparable op2 = (Comparable) pop();
			push(op1.compareTo(op2)>=0);
		}
		public String toString() {
			return "/LE";
		}
	}
	class OperatorNE extends Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) pop();
			Comparable op2 = (Comparable) pop();
			push(op1.compareTo(op2)!=0);
		}
		public String toString() {
			return "/NE";
		}
	}
	
	

	
	class OperatorIf extends Word{
		
		public void process(Context ctx) {
			List<Word> thenPart =  (List<Word>)pop();
			Boolean op1 = (Boolean)pop();
			
			if(op1) {
				StackEnginePS.this.process(thenPart);
			}
		}
		public String toString() {
			return "@IF";
		}
	}
	class OperatorIfElse extends Word{
		
		public void process(Context ctx) {
			
			List<Word> elsePart =  (List<Word>)pop();
			List<Word> thenPart =  (List<Word>)pop();
			Boolean op1 = (Boolean)pop();
			
			if(op1) {
				StackEnginePS.this.process(thenPart);
			}else {
				StackEnginePS.this.process(elsePart);
			}
		}
		public String toString() {
			return "@ifelse " ;
		}
	}
	
	class OperatorLoop extends Word{
		
		public void process(Context ctx) {
			List<Word> block =  (List<Word>)pop();
			boolean st=true;
			while(st) {
				st=StackEnginePS.this.process(block);
			}
		}
		public String toString() {
			return "@loop " ;
		}
	}
	
	/*
	 * a b c {block} for --
	 * for(i=a;i<b;i+=c){
	 *     block
	 *     }
	 */
	class OperatorFor extends Word{
		
		public void process(Context ctx) {
			List<Word> block =  (List<Word>)pop();
			Integer start = (Integer) pop();  // TODO valido para numbers
			Integer end = (Integer) pop();
			Integer step = (Integer) pop();
			boolean st=true;
			if(step>0) {
				for(Integer index=start;st && index <end ; index += step) {
					push(index);
					st=StackEnginePS.this.process(block);
				}
			}else {
				for(Integer index=start;st && index >= end ; index += step) {
					push(index);
					st=StackEnginePS.this.process(block);
				}
			}
		}
		public String toString() {
			return "@for " ;
		}
	}
	class OperatorRepeat extends Word{
		
		public void process(Context ctx) {
			List<Word> block =  (List<Word>)pop();
			Integer times = (Integer) pop();  // TODO valido para numbers
			boolean st=true;
			for(int index=0;st && index < times ; index++) {
				st=StackEnginePS.this.process(block);
			}
	
		}
		public String toString() {
			return "@repeat " ;
		}
	}
	
	class OperatorExit extends Word{
		
		public void process(Context ctx) {
			
			// nothing
		}
		public String toString() {
			return "@exit " ;
		}
	}
	
	class OperatorWord extends Word{
 
		private final Word program; // inmutable
		
		OperatorWord(Word program){
			this.program=program;
		}
		public void process(Context ctx) {
//			System.out.println("--Ex:"+program);
			program.process(ctx);
		}
		public String toString() {
			return "@W:"+program;
		}
	}
	
	class Block extends Word{
		private final List<Word> program; // inmutable
		
		Block(List<Word> program){
			this.program=program;
		}
		public void process(Context ctx) {
			push(program);
		}
		public String toString() {
			return "@B{:"+dumpWords(program)+"}";
		}
	}
	class Subroutine extends Word{
		private final List<Word> program; // inmutable
		
		Subroutine(List<Word> program){
			this.program=program;
		}
		public void process(Context ctx) {
			StackEnginePS.this.process(program);
		}
		public String toString() {
			return "@S{:"+dumpWords(program)+"}";
		}
	}
	
	class OperatorDef extends Word {
		
		public void process(Context ctx) {
			Object obj= pop();
			String name = (String) pop();
			if(obj instanceof List ) {
				List<Word> program = (List<Word>) obj;
				Word w=new Subroutine(program);
				dict.put(name,w);
			}else if(obj instanceof Number) {
				Number n=(Number)obj;
				Word w=new NumberCte(n);
				dict.put(name, w);
			}else if(obj instanceof String) {
				String n=(String)obj;
				Word w=new StringCte(n);
				dict.put(name, w);
			}		
		}
	}
	
	class OperatorCompile extends Word {
		
		public void process(Context ctx) {
			List<Word> program =  getSubroutine();
			push(program);
		}
	}
	
	public StackEnginePS() {
		dict.put("true", new True());
		dict.put("false", new False());
		
		dict.put("==", new Dot());
		dict.put("dup", new Dup());
		dict.put("drop", new Drop());
		dict.put("pop", new Drop());
		dict.put("exch", new Swap());
		dict.put("roll", new Roll());
		// pstack
		// clear
		
		
		dict.put("add", new OperatorSum());
		dict.put("sub", new OperatorSub());
		dict.put("mul", new OperatorMul());
		dict.put("div", new OperatorDiv());
		dict.put("idiv", new OperatorIDiv());
		dict.put("mod", new OperatorMod());
		dict.put("eq", new Equals());
		dict.put("ne", new OperatorNE());
		dict.put("gt", new OperatorGT());
		dict.put("lt", new OperatorLT());
		dict.put("ge", new OperatorGE());
		dict.put("le", new OperatorLE());
		
		dict.put("if",new OperatorIf());
		dict.put("ifelse",new OperatorIfElse());
		dict.put("loop",new OperatorLoop());
		dict.put("exit",new OperatorExit());
		// repeat
		
		dict.put("def", new OperatorDef());
		dict.put("{", new OperatorCompile());
		
		// arrays
		// mark,[
		// array
		// aload
		// astore
		// get
		// put
		// length
		// forall
	}


	@Override
	public Object pop() {
		if(stack.isEmpty()) {
			return null;
		}
		Object obj=stack.pop();
//		System.out.println("POP:"+obj);
		return obj ;
	}

	@Override
	public void push(Object v) {
//		System.out.println("PUSH:"+v);
		stack.push(v);
	}
	
	public void addOperator(String simb,Word ope) {
		
		dict.put(simb, ope);
	}
	

	
	public List<Word> getSubroutine()  { // compile
		List<Word> list=new ArrayList<Word>();
		try {
			Token token= tokenizer.nextToken();

			while(token.type!=Token.Type.EOF) {
				if(token.type==Token.Type.NUM) {
					list.add(new NumberCte((Number)token.value));
				}else if(token.type==Token.Type.STR) {
					list.add(new StringCte((String)token.value));
				}else if(token.type == Token.Type.WORD) {
					if(token.value.equals("}")) {
						//					System.err.println("LIST:"+list);
						return list;
					}	
					if(token.value.equals("{")) {
						List<Word>program=getSubroutine();
						Word w=new Block(program);
						list.add(w);
					}else {
						Word ope = dict.get(token.value);
						if(ope!=null) {
							list.add(new OperatorWord(ope));
						}else {
							throw new StackEngineException("NO se que hacer con ["+token.value+"]");
						}
					}
				}

				token=tokenizer.nextToken();
			}
		} catch (IOException e) {
			throw new StackEngineException("Reading file ",e);
		}
		System.err.println(" Falta un final '}' ");
		return null;
	}

	private String dumpWords(List<Word>p) {
		StringBuilder sb=new StringBuilder(" { ");
		for(Word obj:p) {
			sb.append(obj);
			sb.append(" ");
		}
		sb.append(" } ");
		return sb.toString();
	}
	private void dumpStack() {
		System.out.print("STACK:[");
		for(Object obj:stack) {
			System.out.print(obj);
			System.out.print(" ");
		}
		System.out.println("]");
	}
	

	
	protected void interpret(Token token) throws StackEngineException {
		if(trace) {
			System.err.print("Step:"+token.type+" "+token.value+"| ");dumpStack();
		}
		if(token.type==Token.Type.NUM) {
			push(token.value);
		}
		if(token.type==Token.Type.STR) {
			push(token.value);
		}
		if(token.type==Token.Type.WORD) {
			if("{".equals(token.value)){
				List<Word> list=getSubroutine();
				push(list);
			}else {
				Word ope = dict.get(token.value);
				if(ope!=null) {
					ope.process(this);
				}else {
					throw new StackEngineException("NO se que hacer con ["+token.value+"]");
				}
			}
		}
	}
	
	public Object interpret(String s) throws StackEngineException  {
		System.out.println("Processing:"+s);
		try {
			tokenizer =  new TokenizerString(s); 
			Token token = tokenizer.nextToken();
			while(token.type!=Token.Type.EOF) {
				interpret(token);
				token=tokenizer.nextToken();
			}
		} catch (IOException e) {
			throw new StackEngineException("Reading tokens",e);
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
	
	public Object interpret(File f) throws StackEngineException  {
		System.out.println("Processing file:"+f);
		try {
			tokenizer =  new TokenizerFile(new FileReader(f)); 
			Token token = tokenizer.nextToken();
			while(token.type!=Token.Type.EOF) {
				interpret(token);
				token=tokenizer.nextToken();
			}
		} catch (IOException e) {
			throw new StackEngineException("Reading tokens",e);
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
	
	public boolean process(List<Word> rutina)   {
//		System.err.println("PROCESS:"+dumpWords(rutina));
		for(Word w:rutina) {
			if(w instanceof OperatorExit ) {
				return false;
			}
			w.process(this);
		}
		return true;
	}
	
	public static void main(String[] args) throws StackEngineException {
		
		StackEnginePS scp =new StackEnginePS();
		
		if(args.length==1) {
			scp.interpret(new File(args[0]));
			return;
		}
		
//		scp.interpret(" 'dos' " );
//		scp.interpret(" 1 2 add " );
//		scp.interpret(" 1 2 eq " );
//		scp.interpret(" true { 'iguales' } if" );
//		scp.interpret(" false { 'iguales' } if" );
//		scp.interpret(" true { true { 'otro' } if } if" );
		scp.interpret(" 1 1 eq { 'iguales' } { 'distintos' } ifelse" );
		scp.interpret(" 1 2 eq { 'iguales' } { 'distintos' } ifelse" );
		scp.interpret(" 1 2 lt { 'menor' } { 'mayor o igual' } ifelse" );
		scp.interpret(" 7 dup 5 lt  { 3 lt  "
				+ "                       { 'menor3' } "
				+ "                       { 'entre 3 y 5 '  } "
				+ "                       ifelse "
				+ "                  }  { 'mayor 5' }  ifelse "   );
//		scp.interpret("  /mas2 { 2 add } def");
//		scp.interpret("  3 mas2 ");
//		scp.interpret("	 5 mas2 " );
		
	}







}
