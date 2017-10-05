/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aINO.logisim.circuit;

import java.util.ArrayList;
import com.cburch.logisim.comp.*;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;

/**
 *
 * @author pereira
 */
public class GeneratorComponent {

    private Component Comp;
    private ArrayList<String> Inputs;
    private ArrayList<String> Outputs;
    private ArrayList<String> labelInp;
    private ArrayList<String> labelOut;

    public GeneratorComponent(Component Comp, int In, int Out, 
            ArrayList<String> labelInputs, ArrayList<String> labelOutputs) {
        Inputs = new ArrayList<>();
        Outputs = new ArrayList<>();
        labelInp = new ArrayList<>();
        labelOut = new ArrayList<>();
        
        // this component (to get type and label)
        this.Comp = Comp;

        // input list
        while (In > 0) {
            Inputs.add("");
            In--;
        }
        //  output list
        while (Out > 0) {
            Outputs.add("");
            Out--;
        }
        
        labelInp.addAll(labelInputs);
        labelOut.addAll(labelOutputs);
    }

    public void setInput(int i, String Var) {
        // set i input to Var
        Inputs.set(i, Var);
    }

    public void setOutput(int i, String Var) {
        // set i output to Var 
        Outputs.set(i, Var);
    }

    public String getInput(int i) {
        // get i input to Var name
        // check if index is within list range
        // return string at index
        // return empty if not found

        if(i < Inputs.size())
            return (Inputs.get(i));
        
        // 404 not found
        return ("");
    }

    public String getOutput(int i) {
        // get i output to Var
        // return empty if not found
        
       if(i < Outputs.size())
            return (Outputs.get(i));
        
        // 404 not found
        return ("");
    }

    public boolean isDefined() {
        // search each list for an empty string, or ""
        // if found, set check = false;
        int j;
        for (j = 0; j < Inputs.size(); j++) {
            if(Inputs.get(j).equals(""))
            {
                return (false);
            }
        }
        
        return (true);
    }
    
    public boolean isFinished(){
        // search each list for an empty string, or ""
        // if found, set check = false;
        int j;
        for (j = 0; j < Outputs.size(); j++) {
            if(Outputs.get(j).equals(""))
            {
                return (false);
            }
        }
        
        return (true);
    }

    public String toCode() {
        String Code = "";
        String Op = getOperationGate();
        String Suf = getSuffixGate();
        String Pref = getPrefixGate();
        
        // check if all fields are defined
        if (!this.isDefined()) {
            return ("");
        }

        for (int out = 0; out < Outputs.size(); out++) {
            // output name
            Code = Outputs.get(out) + " = ";
            
            // now adds operations
            for (int inp = 0; inp < Inputs.size(); inp++) {
                if(inp == Inputs.size()-1) Op = "";
                Code += Pref + Inputs.get(inp) + Suf + Op;
            }

            // finish code line with ; and a new line char
            Code += ";\n";
        }

        return (Code);
    }
    
    private String getOperationGate(){
        String Operation = "";
        String Gate = getCompName();
        
        if(Gate.contains("AND") || Gate.contains("NAND"))
            Operation = " && ";
        else if(Gate.contains("OR") || Gate.contains("NOR"))
            Operation = " || ";
        else if(Gate.contains("XOR") || Gate.contains("XNOR"))
            Operation = " ^ ";
     
        return (Operation);
    }
    
    private String getPrefixGate(){
        String Operation = "";
        String Gate = getCompName();
        
        if(Gate.contains("NAND"))
            Operation = "!(";
        else if(Gate.contains("NOT"))
            Operation = "!(";
        else if(Gate.contains("NOR"))
            Operation = "!(";
        else if(Gate.contains("XNOR"))
            Operation = "!(";
     
        return (Operation);
    }
    
      private String getSuffixGate(){
        String Operation = "";
        String Gate = getCompName();
        
        if(Gate.contains("NAND"))
            Operation = ")";
        else if(Gate.contains("NOT"))
            Operation = ")";
        else if(Gate.contains("NOR"))
            Operation = ")";
        else if(Gate.contains("XNOR"))
            Operation = ")";
     
        return (Operation);
    }

    public ArrayList<String> getInputsPort() {
        return (Inputs);
    }

    public ArrayList<String> getOutputsPort() {
        return (Outputs);
    }
    
    public Component getComp(){
        return (Comp);
    }
    
    public String getCompName(){
        return (Comp.getAttributeSet().getValue(StdAttr.LABEL));
    }
    
    public ArrayList<String> getLabelInp(){
        return (labelInp);
    }
    
    public ArrayList<String> getLabelOut(){
        return (labelOut);
    }
    
    public int getIndex(String loc){
        int index;
        
        for(int l=0; l < labelInp.size(); l++){
            if(labelInp.get(l).contains(loc)){
                index = l;
                return (index);
            }
        }
//        
//        for(int l=0; l < labelOut.size(); l++){
//            if(labelOut.get(l).contains(loc.toString())){
//                index = l;
//                return (index);
//            }
//        }
        
        return (-1);
    }
    
}
