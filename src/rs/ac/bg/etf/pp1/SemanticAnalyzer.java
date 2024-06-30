package rs.ac.bg.etf.pp1;

import java.util.HashMap;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {

	boolean errorDetected = false;

	public Struct boolType = Tab.find("bool").getType();

	Obj currentMethod = null;
	Obj currentType = null;

	int nVars = 0;
	int currentConst = 0;

	boolean returnFound = false;
	boolean mainFound = false;
	boolean isArray = false;

	int constCount = 0;
	int globalVarCount = 0;
	int localVarCount = 0;
	int printCount = 0;

	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	public boolean passed() {
		return !errorDetected;
	}

	// Program

	public void visit(Program program) {
		nVars = Tab.currentScope.getnVars();

		if (!mainFound) {
			report_error("Greska na " + program.getLine() + " liniji: Main metoda ne postoji", null);
		}
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	public void visit(ProgName progName) {
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
	}

	// GlobalVar

	public void visit(GlobalVarDeclUnit globalVar) {
		String name;
		name = globalVar.getName();

		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Greska na " + globalVar.getLine() + " liniji: Globalna promenljiva " + name
					+ " je vec deklarisana", null);
		} else {
			globalVarCount++;
			if (isArray) {
				Tab.insert(Obj.Var, name, new Struct(Struct.Array, currentType.getType()));
			} else {
				Tab.insert(Obj.Var, name, currentType.getType());
			}
			report_info("Deklarisana globalna promenljiva -> " + name, globalVar);
		}
	}

	// LocalVar

	public void visit(LocalVarDeclUnit var) {
		String name;
		name = var.getName();

		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Greska na " + var.getLine() + " liniji: Lokalna promenljiva " + name + " je vec deklarisana",
					null);
		} else {
			localVarCount++;
			if (isArray) {
				Tab.insert(Obj.Var, name, new Struct(Struct.Array, currentType.getType()));
			} else {
				Tab.insert(Obj.Var, name, currentType.getType());
			}
			report_info("Deklarisana lokalna promenljiva -> " + name, var);
		}
	}

	// Type

	public void visit(Type type) {
		String name = type.getTypeName();
		currentType = Tab.find(name);

		if (currentType == Tab.noObj) {
			report_error("Greska na " + type.getLine() + " liniji: Tip " + name + " nije pronadjen", null);
			type.struct = Tab.noType;
		} else {
			if (currentType.getKind() == Obj.Type) {
				type.struct = currentType.getType();
			} else {
				report_error("Greska na " + type.getLine() + " liniji: Tip " + name + " ne predstavlja tip", null);
				type.struct = Tab.noType;
			}
		}
	}

	// Const

	public void visit(ConstUnit con) {
		String name;
		name = con.getIdent();

		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Greska na " + con.getLine() + " liniji: Konstante " + name + " je vec deklarisana", null);
		} else {
			if (con.getConsts().struct != currentType.getType()) {
				report_error("Greska na " + con.getLine() + " liniji: Konstanta ima neodgovarajuci tip", null);
			} else {
				constCount++;
				con.obj = Tab.insert(Obj.Con, name, currentType.getType());
				con.obj.setAdr(currentConst);
			}
		}
	}

	public void visit(ConstsNum constant) {
		constant.struct = Tab.intType;
		currentConst = constant.getNumConst();
	}

	public void visit(ConstsChar constant) {
		constant.struct = Tab.charType;
		currentConst = constant.getCharConst();
	}

	public void visit(ConstsBool constant) {
		constant.struct = boolType;
		Boolean bool = constant.getBoolConst();

		currentConst = bool ? 1 : 0;
	}

	// AngleBrackets

	public void visit(AngleBracketsOptT ang) {
		isArray = true;
	}

	public void visit(AngleBracketsOptEps ang) {
		isArray = false;
	}

	// Method

	public void visit(MethodTypeName methodTypeName) {
		Obj object = Tab.find(methodTypeName.getMethName());

		if (object != Tab.noObj) {
			report_error("Greska na " + methodTypeName.getLine() + " liniji: Metoda " + methodTypeName.getMethName()
					+ " je vec deklarisana", null);
			currentMethod = Tab.noObj;
		} else {
			currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethName(), methodTypeName.getReturnType().struct);
			methodTypeName.obj = currentMethod;
			if (methodTypeName.getMethName().equals("main")) {
				mainFound = true;
				if (methodTypeName.getReturnType().struct.getKind() != Struct.None)
					report_error("Greska na " + methodTypeName.getLine() + " liniji: Main metoda mora biti void", null);
			}
		}

		Tab.openScope();
		report_info("Funkcija -> " + methodTypeName.getMethName(), methodTypeName);
	}

	public void visit(MethodDecl methodDecl) {
		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Greska na " + methodDecl.getLine() + " liniji: Funkcija " + currentMethod.getName()
					+ " nema return", null);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		currentMethod = null;
		returnFound = false;
	}

	public void visit(ReturnTypeVoid type) {
		type.struct = Tab.noType;
		currentType = new Obj(Obj.Type, "void", Tab.noType);
	}

	public void visit(ReturnTypeNoVoid type) {
		type.struct = type.getType().struct;
	}

	// Statement

	public void visit(StatementReturnExpr ret) {
		returnFound = true;

		if (currentMethod == null) {
			report_error("Greska na " + ret.getLine() + " liniji: Return se ne sme pozovati van metode", null);
		} else {
			if (currentMethod.getType() != ret.getExpr().struct) {
				report_error("Greska na " + ret.getLine()
						+ " liniji: Tip Return izraza se ne slaze sa tipom povratne vrednosti funkcije "
						+ currentMethod.getName(), null);
			}
		}
	}

	public void visit(StatementReturn ret) {
		returnFound = true;

		if (currentMethod == null) {
			report_error("Greska na " + ret.getLine() + " linija: Return se ne sme pozovati van metode", null);
		} else {
			if (currentMethod.getType() != Tab.noType) {
				report_error("Greska na " + ret.getLine() + " liniji: Metoda nije deklarisana kao void", null);
			}
		}
	}

	public void visit(StatementRead read) {
		Obj object = read.getDesignator().obj;
		int objKind = object.getKind();
		int typeKind = object.getType().getKind();

		if (objKind == Obj.Var || objKind == Obj.Fld || objKind == Obj.Elem) {
			if (!(typeKind == Struct.Int) || (typeKind == Struct.Char) || (typeKind == Struct.Bool)) {
				report_error(
						"Greska na " + read.getLine() + " liniji: Izraz mora da bude tipa int, char ili bool za Read",
						null);
			}
		} else {
			report_error("Greska na " + read.getLine() + " liniji: Pogresan izraz za Read", null);
		}
	}

	public void visit(StatementPrint print) {
		Struct struct = print.getExpr().struct;

		if (struct.getKind() == Struct.Array) {
			if (struct.getElemType().getKind() == Struct.Int || struct.getElemType().getKind() == Struct.Char
					|| struct.getElemType().getKind() == Struct.Bool) {
				printCount++;
			} else {
				report_error("Greska na " + print.getLine()
						+ " liniji: Elementi niza moraju biti tipa int, char ili bool za Print", null);
			}
		} else if (struct.getKind() == Struct.Int || struct.getKind() == Struct.Char
				|| struct.getKind() == Struct.Bool) {
			printCount++;
		} else {
			report_error(
					"Greska na " + print.getLine() + " liniji: Izraz mora biti tipa int, char, bool ili array za Print",
					null);
		}
	}

	public void visit(StatementPrintNum print) {
		Struct struct = print.getExpr().struct;

		if (struct.getKind() == Struct.Array) {
			if (struct.getElemType().getKind() == Struct.Int || struct.getElemType().getKind() == Struct.Char
					|| struct.getElemType().getKind() == Struct.Bool) {
				printCount++;
			} else {
				report_error("Greska na " + print.getLine()
						+ " liniji: Elementi niza moraju biti tipa int, char ili bool za Print", null);
			}
		} else if (struct.getKind() == Struct.Int || struct.getKind() == Struct.Char
				|| struct.getKind() == Struct.Bool) {
			printCount++;
		} else {
			report_error(
					"Greska na " + print.getLine() + " liniji: Izraz mora biti tipa int, char, bool ili array za Print",
					null);
		}
	}

	// Expr

	public void visit(ExprTerm expr) {
		Struct struct = expr.getTerm().struct;

		expr.struct = struct;
	}

	public void visit(ExprMinus expr) {
		Struct struct = expr.getTerm().struct;

		if (struct != Tab.intType) {
			report_error("Greska na " + expr.getLine() + " liniji: Izraz mora biti tipa int za negaciju", null);
			expr.struct = Tab.noType;
		} else {
			expr.struct = struct;
		}
	}

	public void visit(ExprAddop expr) {
		Struct struct1 = expr.getExpr().struct;
		Struct struct2 = expr.getTerm().struct;

		if (struct1 == Tab.intType && struct2 == Tab.intType) {
			expr.struct = struct2;
		} else {
			report_error("Greska na " + expr.getLine() + " liniji: Izrazi moraju biti tipa int za sabiranje", null);
			expr.struct = Tab.noType;
		}
	}

	// Term

	public void visit(TermFactor term) {
		term.struct = term.getFactor().struct;
	}

	public void visit(TermMulop term) {
		Struct struct1 = term.getFactor().struct;
		Struct struct2 = term.getTerm().struct;

		if (struct1 == Tab.intType && struct2 == Tab.intType) {
			term.struct = struct2;
		} else {
			report_error("Greska na " + term.getLine() + " liniji: Izrazi moraju biti tipa int za mnozenje/moduo",
					null);
			term.struct = Tab.noType;
		}
	}

	// Factor

	public void visit(FactorDesignator factor) {
		Struct struct = factor.getDesignator().obj.getType();

		factor.struct = struct;
	}

	public void visit(FactorExpr factor) {
		Struct struct = factor.getExpr().struct;

		factor.struct = struct;
	}

	public void visit(FactorDesignatorAct factor) {
		Obj object = factor.getDesignatorAct().getDesignator().obj;

		if (object.getKind() != Obj.Meth) {
			report_error("Greska na " + factor.getLine() + " liniji: " + object.getName() + " nije funkcija", null);
			factor.struct = Tab.noType;
		} else {
			factor.struct = object.getType();
		}
	}

	public void visit(FactorNewExpr factor) {
		Struct struct = factor.getExpr().struct;

		if (struct.getKind() != Struct.Int) {
			report_error("Greska na " + factor.getLine() + " liniji: Izraz mora biti tipa int", null);
			factor.struct = Tab.noType;
		} else {
			factor.struct = new Struct(Struct.Array, factor.getType().struct);
		}
	}

	public void visit(FactorNum factor) {
		factor.struct = Tab.intType;

		currentConst = factor.getNumConst();
	}

	public void visit(FactorChar factor) {
		factor.struct = Tab.charType;

		currentConst = (int) factor.getCharConst();
	}

	public void visit(FactorBool factor) {
		factor.struct = boolType;
		Boolean bool = factor.getBoolConst();

		currentConst = bool ? 1 : 0;
	}

	public void visit(FactorRange factor) {
		Struct struct = factor.getExpr().struct;

		// Check if the expression type is int
		if (struct.getKind() != Struct.Int) {
			report_error("Greska na " + factor.getLine() + " liniji: Izraz mora biti tipa int", null);
			factor.struct = Tab.noType;
		} else {
			// Set the type of factor to an array of integers
			factor.struct = new Struct(Struct.Array, factor.getExpr().struct);
		}
	}

	// public void visit(FactorRangeMod factor) { // TODO
	// Struct struct1 = factor.getExpr().struct;
	// Struct struct2 = factor.getExpr1().struct;
	// Struct struct3 = factor.getExpr2().struct;

	// public HashMap<String, Integer> map = new HashMap<String, Integer>();
	// map.put(null, null);

	// // Check if the expression type is int
	// if (struct1.getKind() != Struct.Int || struct2.getKind() != Struct.Int ||
	// struct3.getKind() != Struct.Int) {
	// report_error("Greska na " + factor.getLine() + " liniji: Izrazi moraju biti
	// tipa int", null);
	// factor.struct = Tab.noType;
	// } else {
	// // Set the type of factor to an array of integers
	// factor.struct = new Struct(Struct.Array, factor.getExpr().struct);
	// }
	// }

	// DesignatorStatement

	public void visit(DesignatorStatementAssign designator) {
		Obj object = designator.getDesignator().obj;

		if (object.getKind() == Obj.Var || object.getKind() == Obj.Fld || object.getKind() == Obj.Elem) {
			if (!designator.getExpr().struct.assignableTo(object.getType())) {
				report_error(
						"Greska na " + designator.getLine() + " liniji: Nekompatibilnost tipova za dodelu vrednosti",
						null);
			}
		} else {
			report_error("Greska na " + designator.getLine() + " liniji: Pogresan izraz za dodelu vrednosti", null);
		}
	}

	public void visit(DesignatorStatementInc designator) {
		Obj object = designator.getDesignator().obj;

		if (object.getKind() == Obj.Var || object.getKind() == Obj.Fld || object.getKind() == Obj.Elem) {
			if (!(object.getType().getKind() == Struct.Int)) {
				report_error(
						"Greska na " + designator.getLine() + " liniji: Izraz mora biti tipa int za inkrementiranje",
						null);
			}
		} else {
			report_error("Greska na " + designator.getLine() + " liniji: Pogresan izraz za inkrementiranje", null);
		}
	}

	public void visit(DesignatorStatementDec designator) {
		Obj object = designator.getDesignator().obj;

		if (object.getKind() == Obj.Var || object.getKind() == Obj.Fld || object.getKind() == Obj.Elem) {
			if (!(object.getType().getKind() == Struct.Int)) {
				report_error(
						"Greska na " + designator.getLine() + " liniji: Izraz mora biti tipa int za dekrementiranje",
						null);
			}
		} else {
			report_error("Greska na " + designator.getLine() + " liniji: Pogresan izraz za dekrementiranje", null);
		}
	}

	public void visit(DesignatorStatementAct designator) {
		Obj object = designator.getDesignatorAct().getDesignator().obj;

		if (object.getKind() != Obj.Meth) {
			report_error("Greska na " + designator.getLine() + " liniji: Izraz mora biti metoda", null);
		} else {
			report_info("Funkcija -> " + object.getName(), designator);
		}
	}

	// Designator

	public void visit(DesignatorIdent designator) {
		String name = designator.getIdent();
		Obj object = Tab.find(name);

		if (object == Tab.noObj) {
			report_error("Greska na " + designator.getLine() + " liniji: Promenljiva " + name + " nije pronadjena",
					null);
		} else {
			if (object.getKind() == Obj.Var) {
				if (object.getLevel() == 0)
					report_info("Globalna promenljiva -> " + name, designator);
				else
					report_info("Lokalna promenljiva -> " + name, designator);
			}
			if (object.getKind() == Obj.Con) {
				report_info("Konstanta -> " + name, designator);
			}
		}
		designator.obj = object;
	}

	public void visit(DesignatorExpr designator) {
		Obj object = designator.getDesignatorExprDesign().getDesignator().obj;
		String name = object.getName();
		designator.obj = object;

		if (object == Tab.noObj) {
			report_error("Greska na " + designator.getLine() + " liniji: Promenljiva " + name + " nije pronadjena",
					null);
		} else {
			if (object.getType().getKind() != Struct.Array) {
				report_error("Greska na " + designator.getLine() + " liniji: Promenljiva " + name + " mora biti niz",
						null);
			} else {
				if (designator.getExpr().struct != Tab.intType) {
					report_error("Greska na " + designator.getLine() + " liniji: Izraz unutar [] mora biti tipa int",
							null);
				} else {
					designator.obj = new Obj(Obj.Elem, name, object.getType().getElemType());
				}
			}
		}
	}
}
