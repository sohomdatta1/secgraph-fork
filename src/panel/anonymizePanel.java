package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class anonymizePanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	String[] Algorithm = { "One", "Two", "Three", "Four", "Five" };
	private JComboBox<String> c = new JComboBox<String>(Algorithm);
	private JButton select = new JButton("select");
	private JTextField t = new JTextField(15); 
	public anonymizePanel() {
		setBackground(Color.white);
		setPreferredSize(new Dimension(600, 600));
		c.setSelectedIndex(4);
		t.setEditable(false);
		t.setForeground(Color.black);
		select.addActionListener(this);
		add(c, BorderLayout.PAGE_START);
		add(t);
		add(select);
	}

	public void actionPerformed(ActionEvent e) {
        String selectedText = (String)c.getSelectedItem();
        t.setText(selectedText);
	}
}
