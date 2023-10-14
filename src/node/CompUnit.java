package node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tool.filewritter;

public class CompUnit {
    public List<Decl> decls;
    public List<FuncDef> funcDefs;
    public MainFuncDef mainFuncDef;

    public CompUnit(List<Decl> decls, List<FuncDef> funcDefs,
                    MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
    }

    public void printToFile() {
        for (Decl decl : decls) {
            decl.printToFile();
        }
        for (FuncDef funcDef : funcDefs) {
            funcDef.printToFile();
        }
        boolean isnull = mainFuncDef == null;
        System.out.println(isnull);
        mainFuncDef.printToFile();
        filewritter.printGrammer("CompUnit");

    }

}
