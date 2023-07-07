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

public class deAnonymizePanel extends JPanel{

	private static final long serialVersionUID = 1L;
	//String[] Algorithm = { "NS", "DV", "PFG", "YG", "ADA", "KL","JLSB" };
	String[] Algorithm = { "ADA", "JLSB" };
	private JComboBox<String> c;
	private JButton sIn = new JButton("Browse");
	private JTextField in = new JTextField(15); 
	private JTextField out = new JTextField(15);
	private JTextField aux = new JTextField(15);
	private JTextField seed = new JTextField(15);
	private JButton sSeed = new JButton("Browse");
	private JTextArea status = new JTextArea(10, 70);
	private JButton start = new JButton("start");
	private JButton sOut = new JButton("Browse");
	private JPanel subPan1 = new JPanel();
	private JPanel subPan2 = new JPanel();
	private JPanel subPan3 = new JPanel();
	public deAnonymizePanel() {
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		status.setText("Status");
		
		status.setBorder(border);
		c= new JComboBox<String>(Algorithm);
		setBackground(Color.white);
		in.setForeground(Color.black);
		aux.setForeground(Color.black);
		out.setForeground(Color.black);
		seed.setForeground(Color.black);
		add(c);
		subPan1.add(new JLabel("Select anonymized graph file:"));
		subPan1.add(in);
		subPan1.add(sIn);
		subPan2.add(new JLabel("Select auxiliary graph file:"));
		subPan2.add(aux);
		subPan2.add(sOut);
		subPan3.add(new JLabel("Select seed file:"));
		subPan3.add(out);
		subPan3.add(sSeed);
		add(subPan1);
		add(subPan2);
		add(subPan3);
		add(status);
		add(start);
	}
}
