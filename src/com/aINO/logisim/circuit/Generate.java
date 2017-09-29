/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aINO.logisim.circuit;

import com.aINO.logisim.inogui.INOReport;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.Tunnel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jhonny Str
 */
public class Generate {

    private Project MyProject;
    private HashSet<Component> comps = new HashSet<Component>();
    private ArrayList<String> listError = new ArrayList<>();

    public final int No_Errors = 0;
    public final int Check_Label_Error = 1;
    public final int No_Components_Added = 2;
    public final int Wire_List_Error = 3;

    public int Generate(Project Main, INOReport MyReporter) {
        MyProject = Main;

        Set<Wire> wires = null;
        Set<Component> components = null;
        wires = MyProject.getCurrentCircuit().getWires();
        components = MyProject.getCurrentCircuit().getNonWires();

        //Clear lists
        listError.clear();

        MyReporter.AddInfo("-- GENERATE --");
        MyReporter.AddInfo("Project name: " + MyProject.getLogisimFile().getName());
        MyReporter.AddInfo("Circuit name: " + MyProject.getCurrentCircuit().getName() + "\n");

        //Check Annotation Labels
        if (components.size() > 0) {
            CheckLabels(components);
        } else {
            return (No_Components_Added);
        }

        //Check if there's some error and send code error
        if (getListError().size() > 0) {
            return (Check_Label_Error);
        } else {
            MyReporter.AddInfo("Check Labels Done!!" + "\n");
        }

        //Get wire locations
        ArrayList<String> locWireStart = new ArrayList<>();
        ArrayList<String> locWireEnd = new ArrayList<>();
        Iterator<Wire> wir = wires.iterator();
        while (wir.hasNext()) {
            Wire it = wir.next();

            String cord = it.toString();

            String start = cord.substring(cord.indexOf("("), cord.indexOf(")") + 1);
            String end = cord.substring(cord.indexOf("-") + 1, cord.indexOf("]"));

            locWireStart.add(start);
            locWireEnd.add(end);
        }

        if (locWireStart.size() != locWireEnd.size()) {
            return (Wire_List_Error);
        }

        //SOME INFORMATIONS
        MyReporter.AddInfo("-- LIST LOC WIRE START --");
        int k;
        for (k = 0; k < locWireStart.size(); k++) {
            MyReporter.AddInfo(locWireStart.get(k));
        }

        MyReporter.AddInfo("-- LIST LOC WIRE END --");
        for (k = 0; k < locWireEnd.size(); k++) {
            MyReporter.AddInfo(locWireEnd.get(k));
        }

        //Get component locations
        ArrayList<String> locInComp = new ArrayList<>();
        ArrayList<String> locOutComp = new ArrayList<>();
        ArrayList<String> locGate = new ArrayList<>();
        String ComponentName;
        String gate;
        for (Component com : components) {
            List<EndData> ends = com.getEnds();
            ComponentName = com.getAttributeSet().getValue(StdAttr.LABEL);
            if (com.getFactory().RequiresNonZeroLabel()) {
                for (EndData end : ends) {
                    Location loc = end.getLocation();
                    if (com.getEnd(0).isOutput()) {
                        locInComp.add(loc + ", " + ComponentName);
                    } else {
                        locOutComp.add(loc + ", " + ComponentName);
                    }
                }
            } else {
                for (EndData end : ends) {
                    Location loc = end.getLocation();
                    gate = com.getAttributeSet().getValue(StdAttr.LABEL);
                    locGate.add(loc + ", " + gate);
                }
            }
        }

        //SOME INFORMATIONS
        MyReporter.AddInfo("-- LIST LOC INPUTS --");
        int i;
        for (i = 0; i < locInComp.size(); i++) {
            MyReporter.AddInfo(locInComp.get(i));
        }

        MyReporter.AddInfo("-- LIST LOC OUTPUTS --");
        for (i = 0; i < locOutComp.size(); i++) {
            MyReporter.AddInfo(locOutComp.get(i));
        }

        MyReporter.AddInfo("-- LIST LOC GATES --");
        for (i = 0; i < locGate.size(); i++) {
            MyReporter.AddInfo(locGate.get(i));
        }

        //Static location to get conecctions
        ArrayList<String> connect;
        for (i = 0; i < locInComp.size(); i++) {
            for (k = 0; k < locWireStart.size(); k++) {
                if (locInComp.get(i).contains(locWireStart.get(k))) {
                    connect = getConnections(locWireEnd.get(k), locWireStart, locWireEnd,
                            locInComp, locOutComp, locGate);

                    if (connect.size() > 0) {
                        MyReporter.AddInfo("Connections: " + connect.toString());
                    } else {
                        MyReporter.AddInfo("There're no connections at this location!");
                    }

                    connect.clear();
                }
            }
        }

        // no errors!
        return (No_Errors);
    }

    private void CheckLabels(Set<Component> components) {
        String compLabel;
        List<String> labels = Arrays.asList("D2", "D3", "D4", "D5", "D6", "D7",
                "D8", "D9", "D10", "D11", "D12", "D13",
                "A0", "A1", "A2", "A3", "A4", "A5");

        for (Component com : components) {
            if (com.getFactory().RequiresNonZeroLabel()) {
                compLabel = com.getAttributeSet().getValue(StdAttr.LABEL);

                if (!labels.contains(compLabel)) {
                    if (compLabel.isEmpty()) {
                        compLabel = "**Without label**";
                    }

                    listError.add("Invalid label : [" + compLabel + "]" + "\n");
                }
            }
        }
    }

    private ArrayList<String> getConnections(String locAt, ArrayList<String> locWireStart,
            ArrayList<String> locWireEnd, ArrayList<String> locInComp,
            ArrayList<String> locOutComp, ArrayList<String> locGate) {
        int j;
        ArrayList<String> listLoc = new ArrayList<>();

        //Get Wire locations
        for (j = 0; j < locWireStart.size(); j++) {

            boolean found = false;
            String recursive = "(0,0)";

            if (locWireStart.get(j).contains(locAt)) {
                recursive = locWireEnd.get(j);
                found = true;
            } else if (locWireEnd.get(j).contains(locAt)) {
                recursive = locWireStart.get(j);
                found = true;
            }

            // call recursive for that wire
            if (found) {
                // remove from list, as wire meets negative position
                locWireStart.set(j, "(-1,-1)");
                locWireEnd.set(j, "(-1,-1)");

                // call recursive  
                listLoc.addAll(getConnections(recursive, locWireStart, locWireEnd,
                        locInComp, locOutComp, locGate));
            }

        }

        //Get connections for the InComp
        for (j = 0; j < locInComp.size(); j++) {
            if (locInComp.get(j).contains(locAt)) {
                listLoc.add("{" + locInComp.get(j) + "}");
            }
        }

        //Get connections for the Gate
        for (j = 0; j < locGate.size(); j++) {
            if (locGate.get(j).contains(locAt)) {
                listLoc.add("{" + locGate.get(j) + "}");
            }
        }

        //Get connections for the OutComp
        for (j = 0; j < locOutComp.size(); j++) {
            if (locOutComp.get(j).contains(locAt)) {
                listLoc.add("{" + locOutComp.get(j) + "}");
            }
        }

        return listLoc;
    }

    public String getOutPutCode(Set<Component> components) throws IOException {
        ArrayList<String> guardaPinos = new ArrayList<>();
        ArrayList<String> pinMode = new ArrayList<>();
        ArrayList<String> guardaVars = new ArrayList<>();
        ArrayList<String> digitalRead = new ArrayList<>();
        ArrayList<String> digitalWrite = new ArrayList<>();

        String componentName;
        String componentLabel;

        for (Component comp : components) {
            if (comp.getFactory() instanceof Tunnel) {
                continue;
            } else if (comp.getFactory() instanceof Pin) {

                // pin is INPUT (has output value)
                if (comp.getEnd(0).isOutput()) {
                    componentLabel = comp.getAttributeSet().getValue(
                            StdAttr.LABEL);
                    componentName = "PIN_" + componentLabel;

                    guardaPinos.add(componentName);
                    guardaVars.add(componentLabel);
                    pinMode.add(",INPUT_PULLUP);");
                    digitalRead.add("   " + componentLabel + " = digitalRead(" + componentName + ");");
                } // pin is OUTPUT (has input value)
                else if (comp.getEnd(0).isInput()) {
                    componentLabel = comp.getAttributeSet().getValue(
                            StdAttr.LABEL);
                    componentName = "PIN_" + componentLabel;

                    guardaPinos.add(componentName);
                    guardaVars.add(componentLabel);
                    pinMode.add(",OUTPUT);");
                    digitalWrite.add("   digitalWrite(" + componentName + ", " + componentLabel + ");");
                }
            }
        }

        int i;
        String listaPinos = "";
        String listaVars = "";
        String listaPinMode = "";
        String pinoLab;
        String listaRead = "";
        String listaWrite = "";

        for (i = 2; i < guardaPinos.size() + 2; i++) {
            pinoLab = guardaPinos.get(i - 2).substring(5);
            listaPinos += "const int " + guardaPinos.get(i - 2) + " = " + pinoLab + ";" + "\n";
            listaVars += "boolean " + guardaVars.get(i - 2) + ";" + "\n";
            listaPinMode += "   pinMode(" + guardaPinos.get(i - 2) + pinMode.get(i - 2) + "\n";
        }

        for (i = 2; i < digitalRead.size() + 2; i++) {
            listaRead += digitalRead.get(i - 2) + "\n";
        }

        for (i = 2; i < digitalWrite.size() + 2; i++) {
            listaWrite += digitalWrite.get(i - 2) + "\n";
        }

        String s = listaPinos
                + "\n"
                + listaVars
                + "\n"
                + "void setup()\n"
                + "{\n"
                + listaPinMode
                + "}\n"
                + "\n"
                + "void loop()\n"
                + "{\n"
                + listaRead
                + "\n"
                + listaWrite
                + "}";

        return s;
    }

    public ArrayList<String> getListError() {
        return listError;
    }

//    public ArrayList<String> getListLoc() {
//        return listLoc;
//    }
    
    public Set<Component> getNonWires() {
        return comps;
    }
}
