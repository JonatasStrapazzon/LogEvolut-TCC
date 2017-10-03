/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aINO.logisim.inogui;

import com.aINO.logisim.circuit.*;
import com.bfh.logisim.designrulecheck.SimpleDRCContainer;
import com.bfh.logisim.fpgagui.FPGACommanderListModel;
import com.bfh.logisim.fpgagui.FPGACommanderListWindow;
import com.bfh.logisim.fpgagui.FPGACommanderTextWindow;
import com.bfh.logisim.settings.Settings;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Jhonny Str
 */
public class INOCommanderGui implements ActionListener, LibraryListener, ProjectListener, SimulatorListener, CircuitListener, WindowListener,
        MouseListener {

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("annotate")) {
            // runs annotate step
            Annotator.markUnannotated();
            this.doAnnotate(annotationList.getSelectedIndex() == 0);
        } else if (ae.getActionCommand().equals("generate")) {
            try {
                // runs generate step
                this.doGenerate();
            } catch (IOException ex) {
                Logger.getLogger(INOCommanderGui.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void libraryChanged(LibraryEvent event) {
        if (event.getAction() == LibraryEvent.ADD_TOOL
                || event.getAction() == LibraryEvent.REMOVE_TOOL) {
            RebuildCircuitSelection();
        }
    }

    @Override
    public void projectChanged(ProjectEvent event) {
        if (event.getAction() == ProjectEvent.ACTION_SET_CURRENT) {
            SetCurrentSheet(event.getCircuit().getName());
        } else if (event.getAction() == ProjectEvent.ACTION_SET_FILE) {
            RebuildCircuitSelection();
        }

        MyReporter.AddInfo("\n--> Project Changed <--\n");
    }

    @Override
    public void propagationCompleted(SimulatorEvent e) {
    }

    @Override
    public void simulatorStateChanged(SimulatorEvent e) {
    }

    @Override
    public void tickCompleted(SimulatorEvent e) {
    }

    @Override
    public void circuitChanged(CircuitEvent event) {
        int act = event.getAction();

        if (act == CircuitEvent.ACTION_SET_NAME) {
            RebuildCircuitSelection();
        }
        clearDRCTrace();

        MyReporter.AddInfo("\n--> Circuit Changed <--\n");
    }

    @Override
    public void windowOpened(WindowEvent we) {
        if (we.getSource().equals(panel)) {
            InfoWindow.setVisible(InfoWindow.IsActivated());
            ErrorWindow.setVisible(ErrorWindow.IsActivated());
            ConsoleWindow.setVisible(ConsoleWindow.IsActivated());
        }
    }

    @Override
    public void windowClosing(WindowEvent we) {
        if (we.getSource().equals(InfoWindow)) {
            tabbedPane.add(panelInfos, 0);
            tabbedPane.setTitleAt(0, "Infos (" + consoleInfos.size() + ")");
            tabbedPane.setSelectedIndex(0);
        }
        if (we.getSource().equals(ErrorWindow)) {
            int idx = tabbedPane.getComponentCount();
            Set<java.awt.Component> comps = new HashSet<java.awt.Component>(Arrays.asList(tabbedPane.getComponents()));
            if (comps.contains(panelConsole)) {
                idx = tabbedPane.indexOfComponent(panelConsole);
            }
            tabbedPane.add(panelErrors, idx);
            tabbedPane.setTitleAt(idx, "Errors (" + ErrorsList.getCountNr() + ")");
            clearDRCTrace();
        }
        if (we.getSource().equals(ConsoleWindow)) {
            tabbedPane.add(panelConsole, tabbedPane.getComponentCount());
        }
        if (we.getSource().equals(panel)) {
            InfoWindow.setVisible(false);
            ErrorWindow.setVisible(false);
            ConsoleWindow.setVisible(false);
            clearDRCTrace();
        }
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowActivated(WindowEvent we) {
        if (we.getSource().equals(panel)) {
            InfoWindow.setVisible(InfoWindow.IsActivated());
            ErrorWindow.setVisible(ErrorWindow.IsActivated());
            ConsoleWindow.setVisible(ConsoleWindow.IsActivated());
        }
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (me.getClickCount() > 1) {

            if (me.getSource().equals(tabbedPane)) {
                if (tabbedPane.getComponentCount() > 0) {
                    if (tabbedPane.getSelectedComponent().equals(panelInfos)) {
                        InfoWindow.setVisible(true);
                        tabbedPane.remove(tabbedPane.getSelectedIndex());
                    } else if (tabbedPane.getSelectedComponent().equals(panelErrors)) {
                        ErrorWindow.setVisible(true);
                        clearDRCTrace();
                        tabbedPane.remove(tabbedPane.getSelectedIndex());
                    } else if (tabbedPane.getSelectedComponent().equals(panelConsole)) {
                        ConsoleWindow.setVisible(true);
                        tabbedPane.remove(tabbedPane.getSelectedIndex());
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (me.getSource().equals(Errors)
                || me.getSource().equals(ErrorWindow.getListObject())) {
            clearDRCTrace();
            int idx = -1;
            if (me.getSource().equals(Errors)) {
                idx = Errors.getSelectedIndex();
            } else {
                idx = ErrorWindow.getListObject().getSelectedIndex();
            }
            if (idx >= 0) {
                if (ErrorsList.getElementAt(idx) instanceof SimpleDRCContainer) {
                    GenerateDRCTrace((SimpleDRCContainer) ErrorsList.getElementAt(idx));
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    public static final int FONT_SIZE = 12;
    private JFrame panel;
    private Project MyProject;
    private Circuit circuit;
    private Location loc;
    private JComboBox<String> circuitsList = new JComboBox<>();
    private JComboBox<String> annotationList = new JComboBox<>();
    private JLabel textAnnotation = new JLabel("Annotation method");
    private JButton annotateButton = new JButton();
    private JButton validateButton = new JButton();
    private JButton generateButton = new JButton();
    private JComponent panelConsole = new JPanel();
    private FPGACommanderTextWindow InfoWindow;
    private FPGACommanderTextWindow ConsoleWindow;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JComponent panelInfos = new JPanel();
    private JComponent panelErrors = new JPanel();
    private LinkedList<String> consoleInfos = new LinkedList<String>();
    private LinkedList<String> consoleConsole = new LinkedList<String>();
    private JTextArea textAreaInfo = new JTextArea(10, 50);
    private JTextArea textAreaConsole = new JTextArea(10, 50);
    private boolean DRCTraceActive = false;
    private SimpleDRCContainer ActiveDRCContainer;
    private INOReport MyReporter = new INOReport(this);
    private Settings MySettings = new Settings();
    private FPGACommanderListModel ErrorsList = new FPGACommanderListModel();
    private JList<Object> Errors = new JList<Object>();
    private FPGACommanderListWindow ErrorWindow = new FPGACommanderListWindow("INOCommander: Errors", Color.RED, true, ErrorsList);

    private Annotate Annotator = new Annotate();
    private Generate Generator = new Generate();
    private ArrayList<GeneratorComponent> OutputList = new ArrayList<>();
    private String Generated;

    public INOCommanderGui(Project Main) {
        MyProject = Main;
        Generated = "";

        InfoWindow = new FPGACommanderTextWindow("INOCommander: Infos", Color.GRAY, true);
        ConsoleWindow = new FPGACommanderTextWindow("INOCommander: Console", Color.LIGHT_GRAY, false);
        panel = new JFrame("Arduino Commander : " + MyProject.getLogisimFile().getName());
        panel.setResizable(false);
        panel.setAlwaysOnTop(false);
        panel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        panel.addWindowListener(this);
        InfoWindow.addWindowListener(this);
        ErrorWindow.addWindowListener(this);
        ErrorWindow.setSize(new Dimension(740, 400));
        ErrorWindow.getListObject().addMouseListener(this);
        ConsoleWindow.addWindowListener(this);

        GridBagLayout thisLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(thisLayout);

        // annotate button
        annotateButton.setActionCommand("annotate");
        annotateButton.setText("Annotate");
        annotateButton.addActionListener(this);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(annotateButton, c);

        generateButton.setActionCommand("generate");
        generateButton.setText("Generate");
        generateButton.addActionListener(this);
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 0;
        panel.add(generateButton, c);

        // output console
        Color fg = Color.GREEN;
        Color bg = Color.black;

        textAreaInfo.setForeground(fg);
        textAreaInfo.setBackground(bg);
        textAreaInfo.setFont(new Font("monospaced", Font.PLAIN, FONT_SIZE));

        Errors.setBackground(bg);
        Errors.setForeground(Color.RED);
        Errors.setSelectionBackground(Color.RED);
        Errors.setSelectionForeground(bg);
        Errors.setFont(new Font("monospaced", Font.PLAIN, FONT_SIZE));
        Errors.setModel(ErrorsList);
        Errors.setCellRenderer(ErrorsList.getMyRenderer(true));
        Errors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Errors.addMouseListener(this);

        JScrollPane textMessages = new JScrollPane(textAreaInfo);
        JScrollPane textErrors = new JScrollPane(Errors);

        textMessages
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textMessages
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        textErrors
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textErrors
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        GridLayout consolesLayout = new GridLayout(1, 1);

        panelInfos.setLayout(consolesLayout);
        panelInfos.add(textMessages);
        panelInfos.setName("Infos (0)");

        panelErrors.setLayout(consolesLayout);
        panelErrors.add(textErrors);
        panelErrors.setName("Errors (0)");

        tabbedPane.add(panelInfos); // index 0
        tabbedPane.add(panelErrors); // index 2
        tabbedPane.addMouseListener(this);

        textAreaInfo.setEditable(false);

        consoleInfos.clear();

        textAreaInfo.setText(null);

        //console
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        tabbedPane.setPreferredSize(new Dimension(500, 20 * FONT_SIZE));
        panel.add(tabbedPane, c);

        panel.pack();
        panel.setLocationRelativeTo(null);
        panel.setVisible(false);
    }

    private void doAnnotate(boolean ClearExistingLabels) {
        clearAllMessages();
        String CircuitName = MyProject.getCurrentCircuit().getName();
        Circuit Circ = MyProject.getLogisimFile().getCircuit(CircuitName);
        if (Circ != null) {
            if (ClearExistingLabels) {
                Circ.ClearAnnotationLevel();
            }

            OutputList = Annotator.AnnotateArduino(Circ, ClearExistingLabels, MyReporter);
            MyReporter.AddInfo("-- Annotation done --\n");
        }
    }

    private void doGenerate() throws IOException {
        clearAllMessages();
        Set<Component> components = null;
        components = MyProject.getCurrentCircuit().getNonWires();
        int z;
        int ret = Generator.Generate(MyProject, MyReporter, OutputList);

        switch (ret) {
            case 1:
                for (z = 0; z < Generator.getListError().size(); z++) {
                    MyReporter.AddError(Generator.getListError().get(z));
                }
                break;
            case 2:
                MyReporter.AddError(" ** No Component Added ** " + "\n");
                break;
            case 3:
                MyReporter.AddError(" ** LIST LOC WIRE UNMATCH: Different sizes ** " + "\n");
                break;
            case 4:
                MyReporter.AddError(" ** Circuit ERROR, check loose wires or ports without connections ** " + "\n");
                break;
            default:
                try {
                    if (Generated.equals("")) {
                        doSaveAs(components);
                    } else {
                        doSave(Generated, components);
                    }

                } catch (HeadlessException e) {
                    MyReporter.AddError(" ** Exception: " + e + "\n");
                    return;
                }
        }
    }

    private void clearAllMessages() {
        clearDRCTrace();
        textAreaInfo.setText(null);
        consoleInfos.clear();
        ErrorsList.clear();
        int idz = tabbedPane.indexOfComponent(panelErrors);
        if (idz >= 0) {
            tabbedPane.setTitleAt(idz, "Errors (" + ErrorsList.getSize() + ")");
            tabbedPane.setSelectedIndex(idz);
        }
        int idx = tabbedPane.indexOfComponent(panelInfos);
        if (idx >= 0) {
            tabbedPane.setTitleAt(idx, "Infos (" + consoleInfos.size() + ")");
            tabbedPane.setSelectedIndex(idx);
        }
        InfoWindow.clear();
        ConsoleWindow.clear();
        Rectangle rect = tabbedPane.getBounds();
        rect.x = 0;
        rect.y = 0;
        tabbedPane.paintImmediately(rect);
    }

    private void RebuildCircuitSelection() {
        circuitsList.removeAllItems();
        panel.setTitle("Arduino Commander : "
                + MyProject.getLogisimFile().getName());
        int i = 0;
        for (Circuit thisone : MyProject.getLogisimFile().getCircuits()) {
            circuitsList.addItem(thisone.getName());
            thisone.removeCircuitListener(this);
            thisone.addCircuitListener(this);
            if (thisone.getName().equals(
                    MyProject.getCurrentCircuit().getName())) {
                circuitsList.setSelectedIndex(i);
            }
            i++;
        }
    }

    private void SetCurrentSheet(String Name) {
        for (int i = 0; i < circuitsList.getItemCount(); i++) {
            if (circuitsList.getItemAt(i).equals(Name)) {
                circuitsList.setSelectedIndex(i);
                circuitsList.repaint();
                return;
            }
        }
    }

    private void clearDRCTrace() {
        if (DRCTraceActive) {
            ActiveDRCContainer.ClearMarks();
            DRCTraceActive = false;
            MyProject.repaintCanvas();
        }
    }

    public void ShowGui() {
        if (!panel.isVisible()) {
            panel.setVisible(true);
        } else {
            panel.setVisible(false);
            panel.setVisible(true);
        }
    }

    void ClearConsole() {
    }

    void AddConsole(String Message) {
    }

    public void AddInfo(Object Message) {
        StringBuffer Line = new StringBuffer();
        if (consoleInfos.size() < 9) {
            Line.append("    ");
        } else if (consoleInfos.size() < 99) {
            Line.append("   ");
        } else if (consoleInfos.size() < 999) {
            Line.append("  ");
        } else if (consoleInfos.size() < 9999) {
            Line.append(" ");
        }
        Line.append(Integer.toString(consoleInfos.size() + 1) + "> " + Message.toString()
                + "\n");
        consoleInfos.add(Line.toString());
        Line.setLength(0);
        for (String mes : consoleInfos) {
            Line.append(mes);
        }
        textAreaInfo.setText(Line.toString());
        InfoWindow.add(Line.toString());
        int idx = tabbedPane.indexOfComponent(panelInfos);
        if (idx >= 0) {
            tabbedPane.setSelectedIndex(idx);
            tabbedPane.setTitleAt(idx, "Infos (" + consoleInfos.size() + ")");
            Rectangle rect = tabbedPane.getBounds();
            rect.x = 0;
            rect.y = 0;
            tabbedPane.paintImmediately(rect);
        }
    }

    public void AddErrors(Object Message) {
        ErrorsList.add(Message);

        int idx = tabbedPane.indexOfComponent(panelErrors);
        if (idx >= 0) {
            tabbedPane.setSelectedIndex(idx);
            tabbedPane.setTitleAt(idx, "Errors (" + ErrorsList.getCountNr() + ")");
            Rectangle rect = tabbedPane.getBounds();
            rect.x = 0;
            rect.y = 0;
            tabbedPane.paintImmediately(rect);
        }
    }

    private void GenerateDRCTrace(SimpleDRCContainer dc) {
        DRCTraceActive = true;
        ActiveDRCContainer = dc;
        dc.MarkComponents();
        if (dc.HasCircuit()) {
            if (!MyProject.getCurrentCircuit().equals(dc.GetCircuit())) {
                MyProject.setCurrentCircuit(dc.GetCircuit());
            }
        }
        MyProject.repaintCanvas();
    }

    private void doSave(String arq, Set<Component> components) throws IOException {
        Object[] options = {"Save and Open",
            "Save", "New File", "Cancel"};
        int selectOption = JOptionPane.showOptionDialog(null,
                " File exists, would you like to overwrite it? ",
                null,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        switch (selectOption) {
            case 0: {
                String Output;
                FileWriter fw = new FileWriter(arq, false);
                Output = Generator.getOutPutCode(components);
                fw.write(Output);
                fw.close();
                java.awt.Desktop.getDesktop().open(new File(arq));
                break;
            }
            case 1: {
                String Output;
                FileWriter fw = new FileWriter(arq, false);
                Output = Generator.getOutPutCode(components);
                fw.write(Output);
                fw.close();
                break;
            }
            case 2:
                doSaveAs(components);
                break;
            default:
                return;
        }

        MyReporter.AddInfo("-- Generate done --\n");
    }

    private void doSaveAs(Set<Component> components) throws IOException {
        String Output;
        JFileChooser salvar = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ino Files", "ino");
        salvar.addChoosableFileFilter(filter);
        salvar.setAcceptAllFileFilterUsed(false);

        int ok = salvar.showSaveDialog(null);
        if (ok == JFileChooser.APPROVE_OPTION) {
            String diretorio = salvar.getSelectedFile().toString();
            String pasta = salvar.getSelectedFile().getName();

            String arquivo;
            String extensao = ".ino";

            File diretorio2 = new File(diretorio);
            diretorio2.mkdir();

            arquivo = diretorio2 + File.separator + pasta + extensao;

            //FileWriter fw = new FileWriter(arquivo);
            PrintWriter pw = new PrintWriter(new FileWriter(arquivo), false);

            Output = Generator.getOutPutCode(components);

            pw.write(Output);
            pw.close();

            java.awt.Desktop.getDesktop().open(new File(arquivo));

            Generated = arquivo;

            MyReporter.AddInfo("-- Generate done --\n");
        }
    }
}
