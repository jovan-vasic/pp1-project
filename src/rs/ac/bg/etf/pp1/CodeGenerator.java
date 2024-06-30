package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;

	public Struct boolType = Tab.find("bool").getType();

	public int getMainPc() {
		return mainPc;
	}

	// Method

	public void visit(MethodTypeName methodTypeName) {
		if ("main".equalsIgnoreCase(methodTypeName.getMethName())) {
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);

		// Collect arguments and local variables
		SyntaxNode methodNode = methodTypeName.getParent();

		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);

		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);

		// Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(varCnt.getCount() + fpCnt.getCount());
	}

	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	// Statement

	public void visit(StatementRead read) {
		Obj object = read.getDesignator().obj;
		int readCode = (object.getType() == Tab.charType) ? Code.bread : Code.read;

		Code.put(readCode);
		Code.store(object);
	}

	public void visit(StatementPrint print) {
		Struct struct = print.getExpr().struct;

		if (struct.getKind() == Struct.Array) {
			// Array printing
			Struct elemType = struct.getElemType();

			Obj arrLen = Tab.insert(Obj.Var, "arrLen", Tab.intType);
			Code.put(Code.dup); // placeholder
			Code.put(Code.arraylength); // Get the array length
			Code.store(arrLen); // Store the array length

			Obj arrRef = Tab.insert(Obj.Var, "arrRef", Tab.intType);
			Code.store(arrRef);

			Obj index = Tab.insert(Obj.Var, "i", Tab.intType);
			Code.loadConst(0); // Initialize the index to 0
			Code.store(index); // Store the index

			int startLoop = Code.pc; // Start of the loop

			Code.load(arrLen); // Load the array length
			Code.load(index); // Load the index
			Code.putFalseJump(Code.gt, 0); // Jump out of the loop if index > array length

			int adr = Code.pc - 2; // Remember the address of the jump instruction

			Code.load(arrRef); // Duplicate the array reference
			Code.load(index); // Load the index
			Code.put(Code.aload); // Load the array element

			// Print the array element
			if (elemType == Tab.charType) {
				Code.loadConst(1); // width (1B for char)
				Code.put(Code.bprint);
			} else {
				Code.loadConst(4); // width
				Code.put(Code.print);
			}

			Code.load(index); // Load the index
			Code.loadConst(1); // Load the constant 1
			Code.put(Code.add); // Increment the index
			Code.store(index); // Store the incremented index

			Code.putJump(startLoop); // Jump back to the start of the loop

			Code.fixup(adr); // Fix the jump instruction
		} else {
			// Non-array printing
			if (struct == Tab.charType) {
				Code.loadConst(1); // width (1B for char)
				Code.put(Code.bprint);
			} else {
				Code.loadConst(4); // width
				Code.put(Code.print);
			}
		}
	}

	public void visit(StatementPrintNum print) {
		Struct struct = print.getExpr().struct;

		if (struct.getKind() == Struct.Array) {
			// Array printing
			Struct elemType = struct.getElemType();

			Obj arrLen = Tab.insert(Obj.Var, "arrLen", Tab.intType);
			Code.put(Code.dup); // placeholder
			Code.put(Code.arraylength); // Get the array length
			Code.store(arrLen); // Store the array length

			Obj arrRef = Tab.insert(Obj.Var, "arrRef", Tab.intType);
			Code.store(arrRef);

			Obj index = Tab.insert(Obj.Var, "i", Tab.intType);
			Code.loadConst(0); // Initialize the index to 0
			Code.store(index); // Store the index

			int startLoop = Code.pc; // Start of the loop

			Code.load(arrLen); // Load the array length
			Code.load(index); // Load the index
			Code.putFalseJump(Code.gt, 0); // Jump out of the loop if index > array length

			int adr = Code.pc - 2; // Remember the address of the jump instruction

			Code.load(arrRef); // Duplicate the array reference
			Code.load(index); // Load the index
			Code.put(Code.aload); // Load the array element

			// Print the array element
			if (elemType == Tab.charType) {
				Code.loadConst(1); // width (1B for char)
				Code.put(Code.bprint);
			} else {
				Code.loadConst(4); // width
				Code.put(Code.print);
			}

			Code.load(index); // Load the index
			Code.loadConst(1); // Load the constant 1
			Code.put(Code.add); // Increment the index
			Code.store(index); // Store the incremented index

			Code.putJump(startLoop); // Jump back to the start of the loop

			Code.fixup(adr); // Fix the jump instruction
		} else {
			// Non-array printing
			Code.loadConst(print.getN1());
			if (struct == Tab.charType) {
				Code.put(Code.bprint);
			} else {
				Code.put(Code.print);
			}
		}
	}

	// Term

	public void visit(TermMulop term) {
		Mulop mulop = term.getMulop();

		if (mulop instanceof MulopMul) {
			Code.put(Code.mul);
		}
		if (mulop instanceof MulopDiv) {
			Code.put(Code.div);
		}
		if (mulop instanceof MulopMod) {
			Code.put(Code.rem);
		}
	}

	// Expr

	public void visit(ExprAddop expr) {
		Addop addop = expr.getAddop();
		int opCode = (addop instanceof AddopPlus) ? Code.add : Code.sub;

		Code.put(opCode);
	}

	public void visit(ExprMinus expr) {
		Code.put(Code.neg);
	}

	// Factor

	public void visit(FactorDesignator factor) {
		Obj object = factor.getDesignator().obj;

		Code.load(object);
	}

	public void visit(FactorNum factor) {
		Obj object = Tab.insert(Obj.Con, "$", Tab.intType);
		object.setAdr(factor.getNumConst());
		object.setLevel(0);

		Code.load(object);
	}

	public void visit(FactorChar factor) {
		Obj object = Tab.insert(Obj.Con, "$", Tab.charType);
		object.setAdr(factor.getCharConst());
		object.setLevel(0);

		Code.load(object);
	}

	public void visit(FactorBool factor) {
		Obj object = Tab.insert(Obj.Con, "$", boolType);
		object.setAdr(factor.getBoolConst() ? 1 : 0);
		object.setLevel(0);

		Code.load(object);
	}

	public void visit(FactorNewExpr factor) {
		Struct struct = factor.getType().struct;
		int newarrayArg = (struct == Tab.charType) ? 0 : 1; // 0 - Byte, 1 - Word

		Code.put(Code.newarray);
		Code.put(newarrayArg);
	}

	public void visit(FactorRange factor) {
		Struct struct = factor.getExpr().struct;

		Obj arrLen = Tab.insert(Obj.Var, "arrLen", Tab.intType);
		Code.store(arrLen); // Store the array length
		Code.load(arrLen); // Load the array length

		// Create a new integer array
		Code.put(Code.newarray);
		Code.put(1); // Type for integer arrays

		Obj arrRef = Tab.insert(Obj.Var, "arrRef", struct);
		Code.store(arrRef); // Store the array reference

		Obj index = Tab.insert(Obj.Var, "i", Tab.intType);
		Code.loadConst(0); // Initialize the index to 0
		Code.store(index); // Store the index in 'i'

		int startLoop = Code.pc; // Start of the loop

		Code.load(arrLen); // Load the array length
		Code.load(index); // Load the index
		Code.putFalseJump(Code.gt, 0); // Jump out of the loop if index > array length

		int adr = Code.pc - 2; // Remember the address of the jump instruction

		Code.load(arrRef); // Load the array reference
		Code.load(index); // Load the index
		Code.put(Code.dup); // Duplicate the index
		Code.put(Code.astore); // Store the array element

		Code.load(index); // Load the index
		Code.loadConst(1); // Load the constant 1
		Code.put(Code.add); // Increment the index
		Code.store(index); // Store the incremented index

		Code.putJump(startLoop); // Jump back to the start of the loop
		Code.fixup(adr); // Fix the jump instruction

		Code.load(arrRef); // Load the array reference
	}

	// public void visit(FactorRangeMod factor) { // TODO
	// Struct struct = factor.getExpr().struct;

	// Obj step = Tab.insert(Obj.Var, "step", Tab.intType);
	// Code.store(step);
	// Obj stop = Tab.insert(Obj.Var, "stop", Tab.intType);
	// Code.store(stop);
	// Obj start = Tab.insert(Obj.Var, "start", Tab.intType);
	// Code.store(start);
	// Obj arrLen = Tab.insert(Obj.Var, "arrLen", Tab.intType);

	// Code.load(stop);
	// Code.load(start);
	// Code.put(Code.sub);
	// Code.load(step);
	// Code.put(Code.div);
	// Code.loadConst(1);
	// Code.put(Code.add);
	// Code.store(arrLen); // Store the array length

	// Code.load(arrLen); // Load the array length

	// // Create a new integer array
	// Code.put(Code.newarray);
	// Code.put(1); // Type for integer arrays

	// Obj arrRef = Tab.insert(Obj.Var, "arrRef", struct);
	// Code.store(arrRef); // Store the array reference

	// Obj index = Tab.insert(Obj.Var, "i", Tab.intType);
	// Code.loadConst(0); // Initialize the index to 0
	// Code.store(index); // Store the index in 'i'

	// int startLoop = Code.pc; // Start of the loop

	// Code.load(stop);
	// Code.load(start);
	// Code.putFalseJump(Code.gt, 0); // Jump out of the loop if start > stop

	// int adr = Code.pc - 2; // Remember the address of the jump instruction

	// Code.load(arrRef); // Load the array reference
	// Code.load(index); // Load the index
	// Code.load(start);
	// Code.put(Code.astore); // Store the array element

	// Code.load(index); // Load the index
	// Code.loadConst(1); // Load the constant 1
	// Code.put(Code.add); // Increment the index
	// Code.store(index); // Store the incremented index

	// Code.load(start);
	// Code.load(step);
	// Code.put(Code.add);
	// Code.store(start);

	// Code.putJump(startLoop); // Jump back to the start of the loop
	// Code.fixup(adr); // Fix the jump instruction

	// Code.load(arrRef); // Load the array reference
	// }

	// Designator

	public void visit(DesignatorStatementAssign designator) {
		Obj object = designator.getDesignator().obj;

		Code.store(object);
	}

	public void visit(DesignatorStatementInc designator) { // array[i] = array[i] + 1
		Obj object = designator.getDesignator().obj;
		int objKind = object.getKind();

		if (objKind == Obj.Var) {
			Code.load(object);
		}
		if (objKind == Obj.Elem) { // array, i
			Code.put(Code.dup); // array, i, array
			Code.put(Code.dup); // array, i, array, i
			Code.load(object);
		}

		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(object);
	}

	public void visit(DesignatorStatementDec designator) { // array[i] = array[i] - 1
		Obj object = designator.getDesignator().obj;
		int objKind = object.getKind();

		if (objKind == Obj.Var) {
			Code.load(object);
		}
		if (objKind == Obj.Elem) {
			Code.put(Code.dup); // duplicates the array reference on the stack
			Code.put(Code.dup); // duplicates the index on the stack
			Code.load(object); // loads the value at the array index
		}

		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(object);
	}

	public void visit(DesignatorExprDesign designator) {
		Obj object = designator.getDesignator().obj;

		Code.load(object);
	}
}