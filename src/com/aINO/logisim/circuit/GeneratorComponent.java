/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aINO.logisim.circuit;

import java.util.ArrayList;
import com.cburch.logisim.comp.*;

/**
 *
 * @author pereira
 */
public class GeneratorComponent {

    private String MyLabel;
    private Component Comp;
    private ArrayList<String> Inputs;
    private ArrayList<String> Outputs;

    public GeneratorComponent(Component Comp, int In, int Out) {
        Inputs = new ArrayList<>();
        Outputs = new ArrayList<>();

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
        
        int j;
        for (j = 0; j < Outputs.size(); j++) {
            if(j == i)
                return (Outputs.get(j));
        }
        return ("");
    }

    public boolean isDefined() {
        // search each list for an empty string, or ""
        // if found, set check = false;
        boolean check = true;
        
        int j;
        for (j = 0; j < Inputs.size(); j++) {
            if(Inputs.get(j).equals(""))
                return(false);
        }
        
        for (j = 0; j < Outputs.size(); j++) {
            if(Outputs.get(j).equals(""))
                check = false;
        }
        
        return (true);
    }

    public String toCode() {
        String Code = "";

        // check if all fields are defined
        if (!this.isDefined()) {
            return ("");
        }

        for (int out = 0; out < Outputs.size(); out++) {
            // output name
            Code = Outputs.get(out) + " = ";

            // now adds operations
            for (int inp = 0; inp < Inputs.size(); inp++) {
                // ...
            }

            // finish code line with ; and a new line char
            Code += ";\n";
        }

        return (Code);
    }

    public ArrayList<String> getInputsPort() {
        return Inputs;
    }

    public ArrayList<String> getOutputsPort() {
        return Outputs;
    }

}
