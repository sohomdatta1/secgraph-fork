package panel;

import java.awt.Font;

import javax.swing.JTabbedPane;

public class TabbedPannel extends JTabbedPane{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabbedPannel(int i) {
		// TODO Auto-generated constructor stub
		//AnonymizePanel anonymizePanel_ = new AnonymizePanel();
	    setFont(new Font("Arial", Font.PLAIN, 20));
		//addTab ("Anonymization Module", new AnonymizePanel());
		addTab ("De-anonymization Module", new deAnonymizePanel());
		//addTab("Utility Module", new utilityPanel());
		addTab ("De-anonymization Quantification Module", new quantPanel());
	    setSelectedIndex(i);
	}
}
