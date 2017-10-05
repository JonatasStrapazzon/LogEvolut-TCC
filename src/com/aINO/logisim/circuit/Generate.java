/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aINO.logisim.circuit;

import com.aINO.logisim.inogui.INOReport;
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
    private ArrayList<String> listLocPort = new ArrayList<>();
    private ArrayList<GeneratorComponent> AnnotateList;

    public final int No_Errors = 0;
    public final int Check_Label_Error = 1;
    public final int No_Components_Added = 2;
    public final int Wire_List_Error = 3;
    public final int Other_Error = 4;

    public int Generate(Project Main, INOReport MyReporter, ArrayList<GeneratorComponent> OutputList) {
        MyProject = Main;
        AnnotateList = OutputList;

        Set<Wire> wires = null;
        Set<Component> components = null;
        wires = MyProject.getCurrentCircuit().getWires();
        components = MyProject.getCurrentCircuit().getNonWires();

        //Clear lists
        listError.clear();
        listLocPort.clear();

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

        //Static location to get conecctions
//        for (i = 0; i < locInComp.size(); i++) {
//            for (k = 0; k < locWireStart.size(); k++) {
//                if (locInComp.get(i).contains(locWireStart.get(k))) {
//                    connect = getConnections(locWireEnd.get(k), locWireStart, locWireEnd,
//                            locInComp, locOutComp, locGate);
//
//                    if (connect.size() > 0) {
//                        MyReporter.AddInfo("Connections: " + connect.toString());
//                    } else {
//                        MyReporter.AddInfo("There're no connections at this location!");
//                    }
//
//                    connect.clear();
//                }
//            }
//        }
        ArrayList<String> connect;
        GeneratorComponent compGen;
        List<EndData> ends;
        int i, cont = 1;
        String temp = "";

        //To set inputs and outputs pins to comps
        for (i = 0; i < AnnotateList.size(); i++) {
            compGen = AnnotateList.get(i);
            ends = compGen.getComp().getEnds();
            for (EndData end : ends) {
                Location loc = end.getLocation();
                int idx;
                if (end.isInput()) {
                    connect = getConnections(loc.toString(), locWireStart, locWireEnd,
                            locInComp, locOutComp);
                    if (connect.size() == 1) {
                        idx = compGen.getIndex(loc.toString());
                        compGen.setInput(idx, connect.get(0));
                    } else if (connect.size() > 1) {
                        return (Other_Error);
                    } else {
                    }
                } else {
                    connect = getConnections(loc.toString(), locWireStart, locWireEnd,
                            locInComp, locOutComp);
                    if (connect.size() > 1) {
                        return (Other_Error);
                    } else if (connect.size() == 1) {
                        compGen.setOutput(0, connect.get(0));
                    } else {
                    }
                }
                connect.clear();
            }

        }

        //To create the temporary variables and generate the code
        boolean Defined = false;
        ArrayList<GeneratorComponent> CreateComp = new ArrayList<>();
        while (!Defined) {
            Defined = true;
            for (i = 0; i < AnnotateList.size(); i++) {
                compGen = AnnotateList.get(i);
                ends = compGen.getComp().getEnds();
                if (compGen.isDefined()) {
                    if (compGen.getOutput(0).equals("")) {
                        compGen.setOutput(0, "T" + cont);
                        cont++;
                    }
                    MyReporter.AddInfo("GenerateCode: " + compGen.toCode());
                    for (EndData end : ends) {
                        Location loc = end.getLocation();
                        if (end.isOutput()) {
                            connect = getConnections_port(loc.toString(), locWireStart, locWireEnd, locGate);
                            ArrayList<String> listLP = getListLocPort();

                            for (int o = 0; o < AnnotateList.size(); o++) {
                                for (int x = 0; x < connect.size(); x++) {
                                    if (AnnotateList.get(o).getCompName().contains(connect.get(x))) {
                                        for (int l = 0; l < listLP.size(); l++) {
                                            int idx = AnnotateList.get(o).getIndex(listLP.get(l));
                                            if (idx > -1) {
                                                AnnotateList.get(o).setInput(idx, compGen.getOutput(0));
                                            }
                                        }
                                    }
                                }
                            }
                            listLP.clear();
                        }
                    }
                    
                    // insert in the end
                    CreateComp.add(compGen);
                }

                if (!compGen.isFinished()) {
                    Defined = false;
                }
            }
        }

        // clear the annotated list
        AnnotateList.clear();
        AnnotateList.addAll(CreateComp);
        CreateComp.clear();
        CreateComp = null;
        
        //SOME INFORMATIONS
        String inf;
        for (i = 0; i < AnnotateList.size(); i++) {
            MyReporter.AddInfo("CompName: " + AnnotateList.get(i).getCompName());
            for (int j = 0; j < AnnotateList.get(i).getInputsPort().size(); j++) {
                inf = AnnotateList.get(i).getInput(j);
                MyReporter.AddInfo("Inf Input: " + inf);
            }
            for (int jj = 0; jj < AnnotateList.get(i).getOutputsPort().size(); jj++) {
                inf = AnnotateList.get(i).getOutput(jj);
                MyReporter.AddInfo("Inf Output: " + inf);
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
                        compLabel = "** Without label **";
                    }

                    listError.add("Invalid label : [" + compLabel + "] -> Ex: (D2, A0)" + "\n");
                }
            }
        }
    }

    private ArrayList<String> getConnections(String locAt, ArrayList<String> locWireStart,
            ArrayList<String> locWireEnd, ArrayList<String> locInComp, ArrayList<String> locOutComp) {
        int j;
        ArrayList<String> listLoc = new ArrayList<>();
        ArrayList<String> listWS = new ArrayList<>();
        listWS.addAll(locWireStart);
        ArrayList<String> listWE = new ArrayList<>();
        listWE.addAll(locWireEnd);

        //Get Wire locations
        for (j = 0; j < listWS.size(); j++) {
            boolean found = false;
            String recursive = "(0,0)";

            if (listWS.get(j).contains(locAt)) {
                recursive = listWE.get(j);
                found = true;
            } else if (listWE.get(j).contains(locAt)) {
                recursive = listWS.get(j);
                found = true;
            }

            // call recursive for that wire
            if (found) {
                // remove from list, as wire meets negative position
                listWS.set(j, "(-1,-1)");
                listWE.set(j, "(-1,-1)");

                // call recursive  
                listLoc.addAll(getConnections(recursive, listWS, listWE,
                        locInComp, locOutComp));
            }

        }

        //Get connections for the InComp
        for (j = 0; j < locInComp.size(); j++) {
            if (locInComp.get(j).contains(locAt)) {
                String name = locInComp.get(j);
                String name2 = name.substring(name.indexOf(")") + 3);
                listLoc.add(name2);
            }
        }

        //Get connections for the OutComp
        for (j = 0; j < locOutComp.size(); j++) {
            if (locOutComp.get(j).contains(locAt)) {
                String name = locOutComp.get(j);
                String name2 = name.substring(name.indexOf(")") + 3);
                listLoc.add(name2);
            }
        }

        return listLoc;
    }

    private ArrayList<String> getConnections_port(String locAt, ArrayList<String> locWireStart,
            ArrayList<String> locWireEnd, ArrayList<String> locGate) {
        int j;
        ArrayList<String> listLoc = new ArrayList<>();
        ArrayList<String> listWS = new ArrayList<>();
        listWS.addAll(locWireStart);
        ArrayList<String> listWE = new ArrayList<>();
        listWE.addAll(locWireEnd);

        //Get Wire locations
        for (j = 0; j < listWS.size(); j++) {
            boolean found = false;
            String recursive = "(0,0)";

            if (listWS.get(j).contains(locAt)) {
                recursive = listWE.get(j);
                found = true;
            } else if (listWE.get(j).contains(locAt)) {
                recursive = listWS.get(j);
                found = true;
            }

            // call recursive for that wire
            if (found) {
                // remove from list, as wire meets negative position
                listWS.set(j, "(-1,-1)");
                listWE.set(j, "(-1,-1)");

                // call recursive  
                listLoc.addAll(getConnections_port(recursive, listWS, listWE, locGate));
            }
        }

        //Get connections for the Gate
        for (j = 0; j < locGate.size(); j++) {
            if (locGate.get(j).contains(locAt)) {
                String name = locGate.get(j);
                String name2 = name.substring(name.indexOf(")") + 3);
                String name3 = name.substring(0, name.indexOf(")") + 1);
                listLoc.add(name2);
                listLocPort.add(name3);
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
        String code = "";

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
                + code
                + "\n"
                + listaWrite
                + "}";

        return s;
    }

    public ArrayList<String> getListError() {
        return listError;
    }

    public ArrayList<String> getListLocPort() {
        return listLocPort;
    }

    public Set<Component> getNonWires() {
        return comps;
    }
}
