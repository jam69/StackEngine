


package com.jam69.stack.inprogress;



import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


interface Context {
	Object pop();
	void push(Object v);
	PrintStream getOutput();
	LinkedList<Object> getParamStack();
	
}

interface Word {

	public abstract void process(Context ctx);
}

interface Compilable extends Word, Cloneable {
	public void compile();
	public Compilable copy();
}

class Address {
	String name;
	int index;
	Address(String name,int index){
		this.name=name;
		this.index=index;
	}
	public String toString() {
		return name+"["+index+"]";
	}
}



public class ForthEngine implements Context {
	

	protected Tokenizer tokenizer;
	protected LinkedList<Object> stack=new LinkedList<Object>();
	protected LinkedList<Object> paramStack=new LinkedList<Object>();
	protected Map<String,Word> dict = new HashMap<String,Word>();
	protected boolean trace=false;
	protected Token lastToken;
	
	static class OperatorLeave implements Word {
	
		public void process(Context ctx) {
			// Do nothing
		}
		
	}

	static class Name implements Word {
		private final String value;
		public Name(String n) {
			this.value=n;
		}
		
		public void process(Context ctx) {
			ctx.push(value);
		}
	}
	
	static class NumberCte implements Word {
		private final Number value;
		public NumberCte(Number n) {
			this.value=n;
		}
		public void process(Context ctx) {
			ctx.push(value);
		}
	}
	
	static class StringCte implements Word {
		private final String value;
		public StringCte(String s) {
			this.value=s;
		}
		public void process(Context ctx) {
			ctx.push(value);
		}
	}
	
	/**
	 * (n -- )  Y pinta por la pantalla n
	 */
	static class Dot implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			ctx.getOutput().print(op1+" ");
		}
	}
	
	/**
	 *  Prints NewLine in the output
	 */
	static class NewLine implements Word {
		public void process(Context ctx) {
			ctx.getOutput().println();
		}
	}
	/**
	 * ( n -- )  y copia n en paramStack
	 */
	static class Save implements Word { 
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			ctx.getParamStack().push(op1);
		}
	}
	/**
	 *  ( -- n ) saca n del paramStack
	 */
	static class Restore implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.getParamStack().pop();
			ctx.push(op1);
		}
	}
	
	/**
	 * (n -- n n)
	 */
	static class Dup implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			ctx.push(op1);
			ctx.push(op1);
		}
	}
	
	/**
	 * (n -- n n) si n!= 0
	 * (0 -- 0) si == 0
	 */
	static class Dup2 implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			ctx.push(op1);
			Number n=(Number)op1;
			if(n.longValue()==0) {
				ctx.push(op1);
			}
		}
	}
	
	/**
	 * (n -- )
	 */
	static class Drop implements Word {
		public void process(Context ctx) {
			ctx.pop();
		}
	}
	
	/**
	 *  (a b -- a b a)
	 */
	static class Over implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			Object op2 = ctx.pop();
			ctx.push(op1);
			ctx.push(op2);
			ctx.push(op1);
		}
	}
	/**
	 * (n1 n2 -- n2 n1)
	 */
	static class Swap implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			Object op2 = ctx.pop();
			ctx.push(op1);
			ctx.push(op2);
		}
	}
	
	/**
	 * (a b c -- b c a)
	 */
	static class Rot implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			Object op2 = ctx.pop();
			Object op3 = ctx.pop();
			ctx.push(op1);
			ctx.push(op3);
			ctx.push(op2);
		}
	}
	
	/**
	 * ( -- I ) I It's the index of loop
	 */
	static class IndexOperator implements Word {
		public void process(Context ctx) {
			Object obj=ctx.getParamStack().get(0);
			ctx.push(obj);
		}
	}
	/**
	 *  (-- I') it's the limit of the loop
	 */
	static class IndexPrima implements Word {
		public void process(Context ctx) {
			Object obj=ctx.getParamStack().get(1);
			ctx.push(obj);
		}
	}
	
	/**
	 *  ( -- J) It's the index of the outer loop
	 *
	 */
	static class Index2 implements Word {
		public void process(Context ctx) {
			Object obj=ctx.getParamStack().get(2);
			ctx.push(obj);
		}
	}
	
	/*
	 * (a b -- 'a==b' ) 
	 */
	static class Equals implements Word {
		public void process(Context ctx) {
			Object op1 = ctx.pop();
			Object op2 = ctx.pop();
			ctx.push(op1.equals(op2));
		}
	}
	
	/*
	 * (a b -- 'a && b' ) ( a y b son boolean)
	 */
	static class OperatorAnd implements Word {
		public void process(Context ctx) {
			Boolean op1 = (Boolean)ctx.pop();
			Boolean op2 = (Boolean)ctx.pop();
			ctx.push(op1 && op2);
		}
	}
	
	/*
	 * (a b -- 'a||b' )   (a y b son boolean) 
	 */
	static class OperatorOr implements Word {
		public void process(Context ctx) {
			Boolean op1 = (Boolean)ctx.pop();
			Boolean op2 = (Boolean)ctx.pop();
			ctx.push(op1 || op2);
		}
	}
	
	/*
	 * (a -- ' !a' )  (a es boolean) 
	 */
	static class OperatorNot implements Word {
		public void process(Context ctx) {
			Boolean op1 = (Boolean)ctx.pop();
			ctx.push( ! op1 );
		}
	}
	
	/**
	 * (a b --  'a+b' )
	 */
	static class OperatorSum implements Word {
		public void process(Context ctx) {
			Object obj1 = ctx.pop();
			Object obj2 = ctx.pop();			
			Object r=sum(obj1,obj2);
			ctx.push(r);
		}
		/**
		 * Suma polimorifica
		 * 
		 * @param obj1
		 * @param obj2
		 * @return
		 */
		public static Object sum(Object obj1,Object obj2) {
			if(obj1 instanceof Address) {
				return sum((Address)obj1,obj2);
			}else if(obj2 instanceof Address) {
				return sum((Address)obj2,obj1);
			}
			if(obj1 instanceof Double) {
				return sum((Double)obj1,obj2);
			}else if(obj2 instanceof Double) {
				return sum((Double)obj2,obj1);
			}
			if(obj1 instanceof Long) {
				return sum((Long)obj1,obj2);
			}else if(obj2 instanceof Long) {
				return sum((Long)obj2,obj1);
			}
			if(obj1 instanceof Integer) {
				return sum((Integer)obj1,obj2);
			}if(obj2 instanceof Integer) {
				return sum((Integer)obj2,obj1);
			}
			System.err.println("No puedo sumar "+obj1.getClass()+" y "+obj2.getClass());
			return 0;
		}
		
		private static Object sum(Address addr,Object obj2) {
			Number n=(Number)obj2;
			return new Address(addr.name,addr.index+n.intValue());
		}
		private static Object sum(Double d1,Object obj2) {
			Number n=(Number)obj2;
			return d1+n.doubleValue();
		}
		private static Object sum(Long d1,Object obj2) {
			Number n=(Number)obj2;
			return d1+n.longValue();
		}
		private static Object sum(Integer d1,Object obj2) {
			Number n=(Number)obj2;
			return d1+n.intValue();
		}
	}
	

	
	

	/**
	 * (a b --  'a-b' )
	 */
	class OperatorSub implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			push(sub(op1,op2));
		}
		
		private Object sub(Number n1,Number n2) {
			if(n1 instanceof Double) {
				return sub(n1.doubleValue(),n2.doubleValue());
			}else if(n2 instanceof Double) {
				return sub(n1.doubleValue(),n2.doubleValue());
			}else
			if(n1 instanceof Long) {
				return sub(n1.longValue(),n2.longValue());
			}else if(n2 instanceof Double) {
				return sub(n1.longValue(),n2.longValue());
			}else
			if(n1 instanceof Integer) {
				return sub(n1.intValue(),n2.intValue());
			}else if(n2 instanceof Double) {
				return sub(n1.intValue(),n2.intValue());
			}
			System.err.println("No puedo restar "+n1.getClass()+" y "+n2.getClass());
			return 0;
		}
		private Object sub(double n1,double n2) {
			return n2-n1;
		}
		private Object sub(long n1,long n2) {
			return n2-n1;
		}
		private Object sub(int n1,int n2) {
			return n2-n1;
		}
		
	}
	
	/**
	 * (a b --  'a*b' )
	 */
	static class OperatorMul implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			ctx.push(mul(op1,op2));
		}
		
		private Object mul(Number n1,Number n2) {
			if(n1 instanceof Double) {
				return mul(n1.doubleValue(),n2.doubleValue());
			}else if(n2 instanceof Double) {
				return mul(n1.doubleValue(),n2.doubleValue());
			}else
			if(n1 instanceof Long) {
				return mul(n1.longValue(),n2.longValue());
			}else if(n2 instanceof Double) {
				return mul(n1.longValue(),n2.longValue());
			}else
			if(n1 instanceof Integer) {
				return mul(n1.intValue(),n2.intValue());
			}else if(n2 instanceof Double) {
				return mul(n1.intValue(),n2.intValue());
			}else
			System.err.println("No puedo multiplicar "+n1.getClass()+" y "+n2.getClass());
			return 0;
		}
		private Object mul(double n1,double n2) {
			return n2*n1;
		}
		private Object mul(long n1,long n2) {
			return n2*n1;
		}
		private Object mul(int n1,int n2) {
			return n2*n1;
		}
	}
	/**
	 * (a b --  'a/b' )
	 */
	static class OperatorDiv implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			ctx.push(div(op1,op2));
		}
		
		private Object div(Number n1,Number n2) {
			if(n1 instanceof Double) {
				return div(n1.doubleValue(),n2.doubleValue());
			}else if(n2 instanceof Double) {
				return div(n1.doubleValue(),n2.doubleValue());
			}
			if(n1 instanceof Long) {
				return div(n1.longValue(),n2.longValue());
			}else if(n2 instanceof Double) {
				return div(n1.longValue(),n2.longValue());
			}
			if(n1 instanceof Integer) {
				return div(n1.intValue(),n2.intValue());
			}else if(n2 instanceof Double) {
				return div(n1.intValue(),n2.intValue());
			}
			System.err.println("No puedo dividir "+n1.getClass()+" y "+n2.getClass());
			return 0;
		}
		private Object div(double n1,double n2) {
			return n1/n2;
		}
		private Object div(long n1,long n2) {
			return n1/n2;
		}
		private Object div(int n1,int n2) {
			return n1/n2;
		}
	}
	
	/**
	 * (a b c --  'a*b/c' )
	 */
	static class OperatorStarSlash implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Number op3 = (Number) ctx.pop();
			
			ctx.push(starSlash(op1,op2,op3));
		}
		private Object starSlash(Number n1,Number n2,Number n3) {
			if(n1 instanceof Double) {
				return starSlash(n1.doubleValue(),n2.doubleValue(),n3.doubleValue());
			}else if(n2 instanceof Double) {
				return starSlash(n1.doubleValue(),n2.doubleValue(),n3.doubleValue());
			}else if(n3 instanceof Double) {
				return starSlash(n1.doubleValue(),n2.doubleValue(),n3.doubleValue());
			}else
			if(n1 instanceof Long) {
				return starSlash(n1.longValue(),n2.longValue(),n3.longValue());
			}else if(n2 instanceof Long) {
				return starSlash(n1.longValue(),n2.longValue(),n3.longValue());
			}else if(n3 instanceof Long) {
				return starSlash(n1.longValue(),n2.longValue(),n3.longValue());
			}else
			if(n1 instanceof Integer) {
				return starSlash(n1.intValue(),n2.intValue(),n3.intValue());
			}else if(n2 instanceof Integer) {
				return starSlash(n1.intValue(),n2.intValue(),n3.intValue());
			}else if(n3 instanceof Integer) {
				return starSlash(n1.intValue(),n2.intValue(),n3.intValue());
			}
			
			System.err.println("No puedo StarSlash "+n1.getClass()+" , " +n2.getClass()+" y "+n3.getClass());
			return 0;
		}
		
		private Object starSlash(double n1,double n2,double n3) {
			return n2*n2/n3;	
		}
		private Object starSlash(long n1,long n2,long n3) {
			return n2*n2/n3;	
		}
		private Object starSlash(int n1,int n2,int n3) {
			return n2*n2/n3;	
		}
			
		
	}
	
	/**
	 * (a b c --  'a*b/3' )
	 */
	class OperatorStarSlashMod implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			Number op3 = (Number) pop();
			Long r=op1.longValue()*op2.longValue()/op3.longValue();
			Long m=op1.longValue()*op2.longValue()%op3.longValue();
			push(m);
			push(r);
		}
	}
	
	/**
	 *  (n -- 'n+1' )
	 */
	class OperatorPlusOne implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push( OperatorSum.sum(1,op1));
		}
	}
	/**
	 *  (n -- 'n-1' )
	 */
	class OperatorMinusOne implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push(op1.longValue() - 1);
		}
	}
	
	/**
	 *  (n -- 'n+2' )
	 */
	class OperatorPlusTwo implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push( OperatorSum.sum(2,op1));
		}
	}
	/**
	 *  (n -- 'n-2' )
	 */
	class OperatorMinusTwo implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push(op1.longValue() - 2);
		}
	}
	/**
	 *  (n -- 'n*2' )
	 */
	class OperatorTwoTimes implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push(op1.longValue() *2);
		}
	}
	
	/**
	 *  (n -- 'n/2' )
	 */
	class OperatorHalf implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push(op1.longValue() / 2);
		}
	}
	
	/**
	 *  (n -- 'abs(n)' )
	 */
	class OperatorAbs implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push(Math.abs(op1.longValue()));
		}
	}
	
	/**
	 *  (n -- ' -n ' )
	 */
	class OperatorNeg implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			push( - op1.longValue() );
		}
	}
	
	/**
	 *  (a b -- 'max(a,b)  ' )
	 */
	class OperatorMax implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			push( Math.max(op1.longValue(), op2.longValue()) ); 
 		}
	}
	/**
	 *  (a b -- 'min(a,b)  ' )
	 */
	class OperatorMin implements Word {
		public void process(Context ctx) {
			Number op1 = (Number) pop();
			Number op2 = (Number) pop();
			push( Math.min(op1.longValue(), op2.longValue()) ); 
 		}
	}
	
	/**
	 *  (a b -- 'a<b' )
	 */
	class OperatorLT implements Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) ctx.pop();
			Comparable op2 = (Comparable) ctx.pop();
			push(op1.compareTo(op2)==1);
		}
	}
	

	
	/**
	 *  (a b -- 'a>b' )
	 */
	class OperatorGT implements Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) ctx.pop();
			Comparable op2 = (Comparable) ctx.pop();
			push(op1.compareTo(op2)== -1);
		}
	}
	/**
	 *  (a -- 'a>0' )
	 */
	class OperatorG0 implements Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) ctx.pop();
			Comparable op2 = (Comparable) new Double(0);
			push(op1.compareTo(op2)== -1);
		}
	}
	/**
	 *  (a -- 'a==0' )
	 */
	class OperatorE0 implements Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) ctx.pop();
			Comparable op2 = (Comparable) new Double(0);
			push(op1.compareTo(op2)== 0);
		}
	}
	/**
	 *  (a -- 'a<0' )
	 */
	class OperatorL0 implements Word {
		public void process(Context ctx) {
			Comparable op1 = (Comparable) ctx.pop();
			Comparable op2 = (Comparable) new Double(0);
			push(op1.compareTo(op2)== 1);
		}
	}
	
	/**
	 *  (b -- ) 
	 *
	 *  IF [ xxx ELSE ]  yyy THEN
	 *  
	 *     If b is true executes yyy else executes xxx ( xxx ELSE part is optional) 
	 */
	class OperatorIf implements Compilable{
		private List<Word> thenPart;
		private List<Word> elsePart;
		
		
		@Override
		public Compilable copy() {
			try {
				return (Compilable) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public void compile() {
			List<Word>[] parts=compileIf();
			this.thenPart=parts[0];
			this.elsePart=parts[1];
		}
				
		public void process(Context ctx) {
			if(thenPart==null) {
				compile();
			}
			Boolean op1 = (Boolean)pop();
			if(op1) {
				ForthEngine.this.process(thenPart);
			}else {
				if(elsePart!=null) {
					ForthEngine.this.process(elsePart);
				}
			}
		}
	}
	
	/**
	 *  ( a b -- )
	 *  
	 *  a b DO xxxxx LOOP            
	 *  a b DO xxxxx s +LOOP
	 *  
	 *  a es el límite
	 *  b es el inicio
	 *  s es el incremento
	 *   
	 */
	class OperatorDoLoop implements Compilable{

		private List<Word> program;
		private boolean isPlus;
		
		@Override
		public Compilable copy() {
			try {
				return (Compilable) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public void compile() {
			program=compileUntil("LOOP","+LOOP");
			Token t=lastToken;
			if(t.value.equals("LOOP")) {
				isPlus=false;
			}else {
				isPlus=true;
			}
		}
				
		public void process(Context ctx) {
			if(program==null) {
				compile();
			}
			Number startValue = (Number) pop();
			Number endValue = (Number) pop();
			long index=startValue.longValue();
			ctx.getParamStack().push(endValue);
			ctx.getParamStack().push(index);
			long incr=1;
			do {
				ctx.getParamStack().set(0,index); // Don't pop
				ForthEngine.this.process(program);
				
				if(isPlus) {
					Number n=(Number)pop();
					incr=n.longValue();
				}
				index+=incr;
			}while( incr>0 ? 
					index < endValue.longValue() :
					index >= endValue.longValue()
					);
			ctx.getParamStack().pop();
			ctx.getParamStack().pop();
		}
	}
	
	class OperatorBegin implements Compilable{

		private List<Word> program;
		
		@Override
		public Compilable copy() {
			try {
				return (Compilable) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public void compile() {
			program=compileUntil("UNTIL","REPEAT");  //WHILE-REPEAT
				// TODO
		}
				
		public void process(Context ctx) {
			if(program==null) {
				compile();
			}
			
			boolean b=true;
			boolean leave=false;
			while(!b && !leave) {
				leave=ForthEngine.this.process(program);
				if(!leave) {
					b=(Boolean)pop();
				}
			}
		}
	}


	class Variable implements Word{
		
		private final String name;
		private Object[] values=new Object[1];
		
		public Variable(String name) {
			this.name=name;
		}
		
		public void process(Context ctx) {
			push(new Address(name,0));
		}
		
		public Object fetch(int index) {
			return values[index];
		}
		
		public void store(int index,Object val) {
			values[index]=val;
		}
		
		public void allot(Number size) {
			values=new Object[size.intValue()];
		}
		public String toString() {
			StringBuilder sb=new StringBuilder("VAR:"+name+"[");
			for(Object obj:values) {
				sb.append(obj).append(",");
			}
			sb.append("]");
			return sb.toString();
		}
		
	}
	
	class Constant implements Word{
		
		private final String name;
		private final List<Object>values;
		
		public Constant(String name,Object val) {
			this.name=name;
			List<Object>aux=new ArrayList<>(1);
			aux.add(val);
			this.values=Collections.unmodifiableList(aux);
		}
		
		public Constant(String name) {
			this.name=name;
			this.values=new ArrayList<>();
		}
		
		public void process(Context ctx) {
			if(values.size()==1) {
				push(values.get(0));
			}else {
				push(new Address(name,0));
			}
		}
		
		public Object fetch(int index) {
			return values.get(index);
		}
		
		public void addValue(Object obj) {
			values.add(obj);
		}
		
		public String toString() {
			StringBuilder sb=new StringBuilder("CTE:"+name+"[");
			for(Object obj:values) {
				sb.append(obj.toString()).append(",");
			}
			sb.append("]");
			return sb.toString();
		}
				
	}
	
	class Subroutine implements Word{
		
		private final List<Word> program; // inmutable
		
		Subroutine(List<Word> program){
			this.program=program;
		}
		public void process(Context ctx) {
			ForthEngine.this.process(program);
		}
	}
	
	/*
	 * ( var -- n) n es el valor de var
	 *  var ! 
	 */
	class OperatorFetch implements Word {
		
		public void process(Context ctx) {
			Address addr=(Address)pop();
			Word w=dict.get(addr.name);
			if (w instanceof Variable) {
				Variable v=(Variable)w;
				push(v.fetch(addr.index));
			}
		}
	}
	
	/*
	 * ( var -- ) Muestra el valor var
	 *  var ? 
	 */
	class OperatorDisplay implements Word {
		
		public void process(Context ctx) {
			Address addr=(Address)pop();
			Word w=dict.get(addr.name);
			if (w instanceof Variable) {
				Variable v=(Variable)w;
				System.out.print(v+" ");
			}
			if (w instanceof Constant) {
				Constant v=(Constant)w;
				System.out.print(v+" ");
			}
		}
	}
	
	/*
	 * ( n var -- )Incrementa en n el valor de var
	 *  n var +! 
	 */
	class OperatorIncVar implements Word {
		
		public void process(Context ctx) {
			Address addr=(Address)pop();
			Number n=(Number)pop();
			Word w=dict.get(addr.name);
			if (w instanceof Variable) {
				Variable v=(Variable)w;
				Double value=((Number)v.fetch(addr.index)).doubleValue();
				Double inc=n.doubleValue();
				v.store(addr.index,value + inc);
			}
		}
	}
	
	/*
	 * ( n var  -- )   v es el valor que se almacena en la variable n
	 */
	class OperatorStore implements Word {
		
		public void process(Context ctx) {
			Address addr=(Address)pop();
			Word w=dict.get(addr.name);
			if (w instanceof Variable) {
				Variable v=(Variable)w;
				Object obj=pop();
				v.store(addr.index,obj);
			}
		}
	}
	

	
	
	class OperatorVariable implements Word {
		
		public void process(Context ctx) {
			String name=(String) pop();
			Word w=new Variable(name);
			dict.put(name,w);
			push(new Address(name,0));
		}
	}
	
	class OperatorAllot implements Word {
		
		public void process(Context ctx) {
			Number size=(Number) pop();
			Address addr=(Address) pop();
			Word w = dict.get(addr.name);
			if(w instanceof Variable) {
				Variable var=(Variable)w;
				var.allot(size);	
			}
		}
	}
	
	class OperatorCreate implements Word {
		
		public void process(Context ctx) {
			String name=nextName();
			Word w=new Constant(name);
			dict.put(name,w);
			push(w);
		}

	}
	
	class OperatorComma implements Word {
		
		public void process(Context ctx) {
			Object obj=pop();
			Constant cte=(Constant)pop();
			cte.addValue(obj);
			push(cte);
		}
	}
	
	class OperatorConstant implements Word {
		
		public void process(Context ctx) {
			String name=nextName();
			Object v=pop();
			Word w=new Constant(name,v);
			dict.put(name,w);
		}
	}
	
	class OperatorDef implements Compilable {
		
		private List<Word> program;
		
		@Override
		public OperatorDef copy()  {
			try {
				return (OperatorDef) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
		
		@Override
		public void compile() {
			String name=nextName();
			program=compileUntil(";");
			Word w=new Subroutine(program);
			dict.put(name,w);
		}
		public void process(Context ctx) {
			if(program==null) {
				compile();
			}
		}
	}
	
	public ForthEngine() {
		
		// basics
		dict.put(".", new Dot());
		dict.put("CR", new NewLine());
		dict.put("DUP", new Dup());
		dict.put("DROP", new Drop());
		dict.put("OVER", new Over());
		dict.put("ROT", new Rot());
		dict.put("SWAP", new Swap());
		// ROT
		//
		// Comparation
		dict.put("=", new Equals());
		dict.put("<", new OperatorLT());
		dict.put(">", new OperatorGT());
		dict.put("0<", new OperatorL0());
		dict.put("0=", new OperatorE0());
		dict.put("0>", new OperatorG0());
		// logical
		dict.put("AND", new OperatorAnd());
		dict.put("OR", new OperatorOr());
		dict.put("NOT", new OperatorNot());
		// arithmetic
		dict.put("+", new OperatorSum());
		dict.put("-", new OperatorSub());
		dict.put("*", new OperatorMul());
		dict.put("/", new OperatorDiv());
		dict.put("*/", new OperatorStarSlash());
		dict.put("*/MOD", new OperatorStarSlashMod());
		dict.put("1+", new OperatorPlusOne());
		dict.put("1-", new OperatorMinusOne());
		dict.put("2+", new OperatorPlusTwo());
		dict.put("2-", new OperatorMinusTwo());
		dict.put("2*", new OperatorTwoTimes());
		dict.put("2/", new OperatorHalf());
		dict.put("ABS", new OperatorAbs());
		dict.put("NEGATE", new OperatorNeg());
		dict.put("MIN", new OperatorMin());
		dict.put("MAX", new OperatorMax());
		// Flow Control 
		dict.put("IF",new OperatorIf());
		dict.put("DO",new OperatorDoLoop());
		dict.put("LEAVE", new OperatorLeave());
		// Define procedures
		dict.put(":", new OperatorDef());
		dict.put("VARIABLE", new OperatorVariable());
		dict.put("CONSTANT", new OperatorConstant());
		dict.put("@", new OperatorFetch());
		dict.put("!", new OperatorStore());
		dict.put("?", new OperatorDisplay());
		dict.put("+!", new OperatorIncVar());
		dict.put("ALLOT", new OperatorAllot());
		dict.put("CREATE", new OperatorCreate());
		dict.put(",", new OperatorComma());
		// DUMP,
		// ERASE
		// param Stack
		dict.put("I", new IndexOperator());
		dict.put("I'", new IndexPrima());
		dict.put("J", new Index2());
		dict.put(">R", new Save());
		dict.put("R>", new Restore());
	}


	@Override
	public PrintStream getOutput() {
		return System.out;
	}
	
	@Override
	public LinkedList<Object> getParamStack(){
		return paramStack;
	}
	@Override
	public Object pop() {
		return stack.pop();
	}

	@Override
	public void push(Object v) {
		stack.push(v);
	}
	
	private Token nextToken() {
		lastToken=this.tokenizer.nextToken();
		return lastToken;
	}
	public void addOperator(String simb,Word ope) {
		dict.put(simb, ope);
	}
	
	public String nextName()  {
		Token t=nextToken();
		if(t.type !=Token.Type.WORD) {
			//throw new StackEngineException("Falta un id");
			System.err.println(" Falta un id");
		}
		return (String) t.value;
	}
	
	private boolean contains(String[] ends, String target) {
		for(String s:ends) {
			if(s.equals(target)) {
				return true;
			}
		}
		return false;
	}
	
	public List<Word> compileUntil(String ... ends )  { // compile
		List<Word> list=new ArrayList<Word>();
		Token token = nextToken();
		while(token.type!=Token.Type.EOF) {
			if(token.type == Token.Type.WORD) {
				String t=(String)token.value;
				if(contains(ends,t)) {
					return list;
				}
				
				Word ope = dict.get(token.value);
				if(ope!=null) {
					if(ope instanceof Compilable) {
						Compilable comp=(Compilable)ope;
						Compilable ope2= comp.copy();
						ope2.compile();
						list.add(ope2);
					}else {
						list.add(ope);
					}
//				}else {
//					// Unkwnow word
				}
			}
			if(token.type==Token.Type.NUM) {
				list.add(new NumberCte((Number)token.value));
			}
			if(token.type==Token.Type.STR) {
				list.add(new StringCte((String)token.value));
			}
			
			token=nextToken();
		}
		System.err.println(" Falta un final ["+ends+"]");
		return null;
	}
	
	public List<Word>[] compileIf()  {
		final String elseSym="ELSE";
		final String thenSym="THEN";
		List<Word> list=new ArrayList<Word>();
		List<Word> auxList=null;
		boolean skip=false;
		Token token = nextToken();
		while(token.type!=Token.Type.EOF) {
			if(token.type == Token.Type.WORD) {
				
				Word ope = dict.get(token.value);
				if(ope!=null) {
					if(ope instanceof Compilable) {
						Compilable comp=(Compilable)ope;
						Compilable aux=comp.copy();
						aux.compile();
						list.add(aux);
					}else {
						list.add(ope);
					}
				}
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
					skip=true;
					auxList=list;
					list=new ArrayList<Word>();
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
			
			token=nextToken();
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

	
	protected Word interpret(Token token) throws StackEngineException {
		if (trace) {
			System.out.print("Step:" + token.type + " " + token.value + "| ");
			dumpStack();
		}

		if (token.type == Token.Type.NUM) {
			return new NumberCte((Number) token.value);
		}
		if (token.type == Token.Type.STR) {
			return new StringCte((String) token.value);
		}

		if (token.type == Token.Type.WORD) {
			Word ope = dict.get(token.value);
			if (ope != null) {
				if (ope instanceof Compilable) {
					Compilable comp = (Compilable) ope;
					Compilable ope2 = comp.copy();
					ope2.compile();
					return ope2;
				} else {
					return ope;
				}
			} else {
				return new Name((String)token.value);
//				throw new StackEngineException("NO se que hacer con [" + token.value + "]");
			}
		}
		throw new StackEngineException("Error de lexer [" + token.value + "]");
	}
	
	public Object interpret(String s) throws StackEngineException  {
		System.out.println("\nProcessing:"+s);
		tokenizer =  new Tokenizer(s); 
		Token token = nextToken();
		while(token.type!=Token.Type.EOF) {
			Word w=interpret(token);
			w.process(this);
			token=nextToken();
		}
		if(stack.isEmpty()) {
			System.out.println(" End Processing:"+"EMPTY");
			return null;
		}else {
			Object r=stack.pop();
			System.out.println(" End Processing:"+r);
			if(!stack.isEmpty()) {
					System.out.println("Pila no esta vacia ");dumpStack();
			}
			return r;
		}
		
	}
	

	
	public boolean process(List<Word> rutina)   {
		for(Word w:rutina) {
			if(w instanceof OperatorLeave) {
				break;
			}
			w.process(this);
			
		}
		return false;
	}
	
	public static void main(String[] args) throws StackEngineException {
		
		ForthEngine scp =new ForthEngine();
		scp.interpret(" 'dos' " );
		scp.interpret(" 1 2 + " );
		scp.interpret(" 1 2 = " );
		scp.interpret(" 1 2 = IF 'iguales' THEN" );
		scp.interpret(" 1 1 = IF 'iguales' THEN" );
		scp.interpret(" 1 1 = IF 'distintos' ELSE 'iguales' THEN ." );
		scp.interpret(" 1 2 = IF 'distintos' ELSE 'iguales' THEN ." );
		scp.interpret(" 1 DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN  DROP" );
		scp.interpret(" 4  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN DROP" );
		scp.interpret(" 9  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN DROP" );
		scp.interpret(" : MAS2 2 + ;");
		scp.interpret(" : MAS3 3 + ;");
		scp.interpret(" : MAS5 MAS2 MAS3 ;");
		scp.interpret(" 3 MAS2 MAS2 MAS3 MAS5");
		scp.interpret(" 5 MAS5");
		scp.interpret(" 10 0 DO 'hola' . I . LOOP ");
		scp.interpret(" 10 0 DO  10 0 DO '[' . I . J . ']' . LOOP CR  LOOP ");
		scp.interpret(" 10 0 DO 'hola' . I .  CR 2 +LOOP ");
		scp.interpret(" -10 0 DO I . -1 +LOOP CR ");
		scp.interpret(": INC-COUNT DO I . DUP +LOOP DROP ;"); 
		scp.interpret(" 1 5 0 INC-COUNT");
		scp.interpret(" 2 5 0 INC-COUNT");
		scp.interpret(" -3 -10  10 INC-COUNT");
		scp.interpret("32767 1 DO I . I +LOOP");
		scp.interpret(" uno VARIABLE");
		scp.interpret(" dos VARIABLE");
		scp.interpret(" tres VARIABLE");
		scp.interpret(" cuatro VARIABLE");
		scp.interpret(" 1 uno !");
		scp.interpret(" 2 dos !");
		scp.interpret(" 3 tres !");
		scp.interpret(" 4 cuatro !");
		scp.interpret(" uno @ dos @ tres @ cuatro @ + + + .");
		scp.interpret(" uno @ dos @ + tres @ cuatro @  + + .");
		scp.interpret(" 1 tres +!");
		scp.interpret(" tres ?");
		scp.interpret(" 2 tres +!");
		scp.interpret(" tres @");
		scp.interpret(" unArray VARIABLE 10 ALLOT ");
		scp.interpret(" 100 unArray 1 + ! ");
		scp.interpret(" 200 unArray 2 + ! ");
		scp.interpret(" 300 unArray 3 + ! ");
		scp.interpret(" 400 unArray 4 + ! ");
		scp.interpret(" unArray ?");
		scp.interpret(" unArray 3 + @ ");
		scp.interpret(" 1000 CONSTANT mil ");
		scp.interpret(" mil 1 +");
		scp.interpret(" mil");
		scp.interpret(" CREATE limits 100 , 200 , 300 , 400 , ");
		scp.interpret(" limits 3 + ! ");
		scp.interpret(" limits 3 + . ");
		
	}







}
