package com.aINO.logisim.inomenu;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.aINO.logisim.inogui.INOCommanderGui;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;

@SuppressWarnings("serial")
public class MenuINO extends JMenu implements ActionListener {
	private Project ThisCircuit;
	private JMenuItem INOCommander = new JMenuItem();
	private INOCommanderGui Commander = null;

	public MenuINO(JFrame parent, LogisimMenuBar menubar, Project proj) {
		ThisCircuit = proj;

		INOCommander.addActionListener(this);

		add(INOCommander);
		setEnabled(parent instanceof Frame);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == INOCommander) {
			if (Commander == null)
				Commander = new INOCommanderGui(ThisCircuit);
			Commander.ShowGui();
		}
	}

	public void localeChanged() {
		this.setText(Strings.get("Arduino")); //@TCC
		INOCommander.setText(Strings.get("Convert Circuit")); //@TCC
	}
}
