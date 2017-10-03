/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aINO.logisim.circuit;

import com.aINO.logisim.inogui.INOReport;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.Tunnel;
import com.cburch.logisim.tools.SetAttributeAction;
import com.cburch.logisim.tools.Strings;
import com.cburch.logisim.util.AutoLabel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Jhonny Str
 */
public class Annotate {

    private boolean Annotated;

    private static int PinCountD = 2; // digital pins start from 2
    private static int PinCountA = 0; // analog pins

    private class ArduinoComparator implements Comparator<Component> {

        @Override
        public int compare(Component o1, Component o2) {
            if (o1 == o2) {
                return 0;
            }
            Location l1 = o1.getLocation();
            Location l2 = o2.getLocation();
            if (l2.getY() != l1.getY()) {
                return l1.getY() - l2.getY();
            }
            if (l2.getX() != l1.getX()) {
                return l1.getX() - l2.getX();
            }
            return -1;
        }

    }

    public boolean isAnnotated() {
        return (Annotated);
    }

    public void markUnannotated() {
        Annotated = false;
    }

    private static String GetArduinoAnnotationName(Component comp) {
        String ComponentName;
        /* Pins are treated specially */
        if (comp.getFactory() instanceof Pin) {
            if (comp.getEnd(0).isOutput()) {
                // INPUT pin!
                switch (comp.getEnd(0).getWidth().getWidth()) {
                    case 10:
                        ComponentName = "A";// + PinCountA;    // analogRead as int
                        PinCountA++;
                        break;
                    case 1:
                        ComponentName = "D";// + PinCountD;    // digitalRead as boolean
                        PinCountD++;
                        break;
                    default:
                        ComponentName = "ERROR_I";
                        break;
                }
            } else {
                // OUTPUT pin!
                switch (comp.getEnd(0).getWidth().getWidth()) {
                    case 8:
                        ComponentName = "D";// + PinCountD;    // analogWrite as byte (8 bits PWM)
                        PinCountD++;
                        break;
                    case 1:
                        ComponentName = "D";// + PinCountD;    // digitalWrite
                        PinCountD++;
                        break;
                    default:
                        ComponentName = "ERROR_O";
                        break;
                }
            }
        } else {
            ComponentName = comp.getFactory().getHDLName(comp.getAttributeSet());
            ComponentName = ComponentName.substring(0, ComponentName.indexOf("_"));
        }
        return ComponentName;
    }

    public ArrayList<GeneratorComponent> AnnotateArduino(Circuit Circ, boolean ClearExistingLabels, INOReport MyReporter) {
        /* If I am already completely annotated, return */
        MyReporter.AddInfo("-- ANNOTATE --\n");

        if (Annotated) {
            MyReporter.AddInfo("Nothing to do !");
            return (null);
        }
        SortedSet<Component> comps = new TreeSet<Component>(new ArduinoComparator());
        HashMap<String, AutoLabel> lablers = new HashMap<String, AutoLabel>();
        Set<String> LabelNames = new HashSet<String>();

        for (Component comp : Circ.getNonWires()) {
            if (comp.getFactory() instanceof Tunnel) {
                continue;
            }
            /* we are directly going to remove duplicated labels */
            AttributeSet attrs = comp.getAttributeSet();
            if (attrs.containsAttribute(StdAttr.LABEL)) {
                String label = attrs.getValue(StdAttr.LABEL);
                if (!label.isEmpty()) {
                    if (LabelNames.contains(label.toUpperCase())) {
                        SetAttributeAction act = new SetAttributeAction(Circ, Strings.getter("changeComponentAttributesAction"));
                        act.set(comp, StdAttr.LABEL, "");
                        Circ.ProjAction(act);
                        //MyReporter.AddSevereWarning("Removed duplicated label "+Circ.getName()+"/"+label);
                    } else {
                        LabelNames.add(label.toUpperCase());
                    }
                }
            } else {
                MyReporter.AddInfo("** no label **");
            }
            /* now we only process those that require a label */
            if (comp.getFactory().RequiresNonZeroLabel()) {
                if (ClearExistingLabels) {
                    /* in case of label cleaning, we clear first the old label */
                    MyReporter.AddInfo("Cleared " + Circ.getName() + "/"
                            + comp.getAttributeSet().getValue(StdAttr.LABEL));
                    comp.getAttributeSet().setValue(StdAttr.LABEL, "");
                }
                if (comp.getAttributeSet().getValue(StdAttr.LABEL).isEmpty()) {
                    String ComponentName = GetArduinoAnnotationName(comp);
                    //comp.getAttributeSet().setValue(StdAttr.LABEL, ComponentName);
                    comps.add(comp);
                    if (!lablers.containsKey(ComponentName)) {
                        if (ComponentName.equals("D")) {
                            lablers.put(ComponentName, new AutoLabel(ComponentName + "2", Circ));
                        } else {
                            lablers.put(ComponentName, new AutoLabel(ComponentName + "0", Circ));
                        }
                    }
                }
            } else {
//                MyReporter.AddInfo("Skip --> " + Circ.getName() + "/"
//                        + comp.getAttributeSet().getValue(StdAttr.LABEL));
                if (comp.getAttributeSet().getValue(StdAttr.LABEL).isEmpty()) {
                    String ComponentName = GetArduinoAnnotationName(comp);
                    comps.add(comp);
                    if (!lablers.containsKey(ComponentName)) {
                        lablers.put(ComponentName, new AutoLabel(ComponentName + "_0", Circ));
                    }
                }
            }
            /* if the current component is a sub-circuit, recurse into it */
            if (comp.getFactory() instanceof SubcircuitFactory) {
                SubcircuitFactory sub = (SubcircuitFactory) comp.getFactory();
                this.AnnotateArduino(sub.getSubcircuit(), ClearExistingLabels, MyReporter);
            }
        }

        /* Now Annotate */
        for (Component comp : comps) {
            String ComponentName = GetArduinoAnnotationName(comp);
            if (!lablers.containsKey(ComponentName)
                    || !lablers.get(ComponentName).hasNext(Circ)) {
                /* This should never happen! */
                //MyReporter.AddFatalError("Annotate internal Error: Either there exists duplicate labels or the label syntax is incorrect!\nPlease try annotation on labeled components also\n");
                return (null);
            } else {
                AutoLabel NewAuto = lablers.get(ComponentName);
                String NewLabel = NewAuto.GetCurrent(Circ, comp.getFactory());
                SetAttributeAction act = new SetAttributeAction(Circ, Strings.getter("changeComponentAttributesAction"));
                act.set(comp, StdAttr.LABEL, NewLabel);
                Circ.ProjAction(act);
                MyReporter.AddInfo("Labeled " + Circ.getName() + "/" + NewLabel);

//                String[] parts = NewLabel.split("_");
//                String part1 = parts[0];
                //System.out.println("Componente " + ComponentName + " [" + part1 + "] nomeado com " + NewLabel);
            }
        }

        int contIn = 0, contOut = 0;
        ArrayList<GeneratorComponent> CreateComp = new ArrayList<>();
        ArrayList<String> labelInp = new ArrayList<>();
        ArrayList<String> labelOut = new ArrayList<>();
        for (Component com : Circ.getNonWires()) {
            List<EndData> ends = com.getEnds();
            if (com.getFactory().RequiresNonZeroLabel()) {
                continue;
            } else {
                MyReporter.AddInfo("Read Comp: " + com.getAttributeSet().getValue(StdAttr.LABEL));
                for (EndData end : ends) {
                    Location loc = end.getLocation();
                    if (end.isOutput()) {
                        contOut++;
                        labelOut.add(loc.toString());
                        //MyReporter.AddInfo("Output: " + loc);
                    } else {
                        contIn++;
                        labelInp.add(loc.toString());
                        //MyReporter.AddInfo("Input: " + loc);
                    }
                }
            }
            
//            for(int in =0; in < labelInp.size(); in++)
//                MyReporter.AddInfo("Com: " + com.getAttributeSet().getValue(StdAttr.LABEL) + " LabelInp: " + labelInp.get(in));
//        
//            for(int in =0; in < labelOut.size(); in++)
//                MyReporter.AddInfo("Com: " + com.getAttributeSet().getValue(StdAttr.LABEL) + " LabelOut: " + labelOut.get(in));
        
            GeneratorComponent comp = new GeneratorComponent(com, contIn, contOut, labelInp, labelOut);
            CreateComp.add(comp);
            
            contIn = contOut = 0;
            labelInp.clear();
            labelOut.clear();
        }
        
        Annotated = true;

        return (CreateComp);
    }

} // class
