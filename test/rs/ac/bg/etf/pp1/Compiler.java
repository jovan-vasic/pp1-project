package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Compiler {
	
	static Logger log = Logger.getLogger(MJParserTest.class);

	static {
		DOMConfigurator.configure("config/log4j.xml"); // DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void tsdump(SemanticAnalyzer v) {
		log.info("===================================");
		log.info("constCount = " + v.constCount);
		log.info("globalVarCount = " + v.globalVarCount);
		log.info("localVarCount = " + v.localVarCount);
		log.info("printCount = " + v.printCount);
		log.info("===================================");
		
		Tab.dump();
	}

	public static void main(String[] args) throws Exception {
		Reader br = null;
		try {
			File sourceCode = new File(args[0]);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());

			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);

			MJParser p = new MJParser(lexer);
			Symbol s = p.parse(); // pocetak parsiranja

			Program prog = (Program) (s.value);
			Tab.init();
			
			// BoolType creation
			Obj boolType = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool));
			boolType.setAdr(-1);
			boolType.setLevel(-1);

			// ispis sintaksnog stabla
			log.info(prog.toString(""));
			log.info("===================================");

			// ispis prepoznatih programskih konstrukcija
			SemanticAnalyzer v = new SemanticAnalyzer();
			prog.traverseBottomUp(v);
			
			tsdump(v);

			if (!p.errorDetected && v.passed()) {
				File objFile = new File(args[1]);
				if (objFile.exists())
					objFile.delete();

				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);

				Code.dataSize = v.nVars;
				Code.mainPc = codeGenerator.getMainPc();
				Code.write(new FileOutputStream(objFile));

				log.info("Parsiranje uspesno zavrseno!");
			} else {
				log.error("Parsiranje NIJE uspesno zavrseno!");
			}
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e1) {
					log.error(e1.getMessage(), e1);
				}
		}

	}

}
