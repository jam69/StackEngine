package com.jam69.stack.engine;


public class StackCalculator extends StackEngine{
	
	
	class SumToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()+op2.doubleValue();
			ctx.push(r);
		}
	}
	class SubToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()-op2.doubleValue();
			ctx.push(r);
		}
	}
	class MulToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()*op2.doubleValue();
			ctx.push(r);
		}
	}
	class DivToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Number op2 = (Number) ctx.pop();
			Double r=op1.doubleValue()/op2.doubleValue();
			ctx.push(r);
		}
	}
	class SqrtToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Double r=Math.sqrt(op1.doubleValue());
			ctx.push(r);
		}
	}
	class SinToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Double r=Math.sin(op1.doubleValue());
			ctx.push(r);
		}
	}
	class CosToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Double r=Math.cos(op1.doubleValue());
			ctx.push(r);
		}
	}
	class TanToken implements Operator {
		public void process(Context ctx) {
			Number op1 = (Number) ctx.pop();
			Double r=Math.tan(op1.doubleValue());
			ctx.push(r);
		}
	}
	class PiToken implements Operator {
		public void process(Context ctx) {
			ctx.push(Math.PI);
		}
	}
	
	public StackCalculator () {
		addOperator("+",new SumToken());
		addOperator("-",new SubToken());
		addOperator("*",new MulToken());
		addOperator("/",new DivToken());
		addOperator("sin",new SinToken());
		addOperator("cos",new CosToken());
		addOperator("tan",new TanToken());
		addOperator("PI",new PiToken());
	}
	
	
	public static void main(String[] args) throws StackEngineException {
		
		StackCalculator scp =new StackCalculator();
		System.out.println("Result:"+ scp.process(" 3 9 + 4 6 + * "  ));
	}

}
