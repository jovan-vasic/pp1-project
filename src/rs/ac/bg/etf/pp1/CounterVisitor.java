package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;

public class CounterVisitor extends VisitorAdaptor {

	protected int count;

	public int getCount() {
		return this.count;
	}

	public static class FormParamCounter extends CounterVisitor {

		public void visit(FormParsListComma pars) {
			count++;
		}

		public void visit(FormParsListUnit pars) {
			count++;
		}
	}

	public static class VarCounter extends CounterVisitor {

		public void visit(LocalVarDecl var) {
			count++;
		}
	}
}
