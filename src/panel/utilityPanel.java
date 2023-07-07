package panel;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class utilityPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	String[] Algorithm = { "Deg", "JD", "ED", "PL", "LCC", "GCC","CC", "BC","EV","NC",
						   "NR","Infe","PR","HS","AS","RX","RE","MINS","CD"};
	private JComboBox<String> c;
	private JButton sIn = new JButton("Browse");
	private JTextField in = new JTextField(15); 
	private JTextField out = new JTextField(15);
	private JTextArea status = new JTextArea(10, 70);
	private JButton start = new JButton("start");
	private JButton sOut = new JButton("Browse");
	private JPanel subPan1 = new JPanel();
	private JPanel subPan2 = new JPanel();
	
	public utilityPanel() {
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		status.setText("Status");
		
		status.setBorder(border);
		c= new JComboBox<String>(Algorithm);
		setBackground(Color.white);
		in.setForeground(Color.black);
		out.setForeground(Color.black);
		add(c);
		subPan1.add(new JLabel("Select original graph file:"));
		subPan1.add(in);
		subPan1.add(sIn);
		subPan2.add(new JLabel("Select anonymized graph file:"));
		subPan2.add(out);
		subPan2.add(sOut);
		add(subPan1);
		add(subPan2);
		add(status);
		add(start);
	}
}
