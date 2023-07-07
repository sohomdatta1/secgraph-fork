package panel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

public class StartupPage extends JPanel{
	private static final long serialVersionUID = 1L;
	String[] moduleNames = {"De-anonymization Module","Utility Module","Anonymization Module","De-anonymization Quantification Module"};
	private JButton dm = new JButton(moduleNames[0]);
	private JButton um = new JButton(moduleNames[1]);
	private JButton am = new JButton(moduleNames[2]);
	private JButton qm = new JButton(moduleNames[3]);
	private JPanel subPan = new JPanel();
	//JFrame f;
	public StartupPage(final JFrame frame) {
		//f=frame;
		// TODO Auto-generated constructor stub
	   int b = 50*2;
	   am.setFont(new Font("Arial", Font.PLAIN, 20));
	   dm.setFont(new Font("Arial", Font.PLAIN, 20));
	   um.setFont(new Font("Arial", Font.PLAIN, 20));
	   qm.setFont(new Font("Arial", Font.PLAIN, 20));
	   setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
       subPan.setLayout(new GridLayout(2, 2));
       subPan.add(am);
	   subPan.add(dm);
	   subPan.add(um);
	   subPan.add(qm);
	   /*am.addActionListener(new ActionListener()
	   {
	     public void actionPerformed(ActionEvent e)
	     {
	    	 frame.setContentPane(new TabbedPannel(0));
	    	 frame.setVisible(true);
	     }
	   });*/
	   dm.addActionListener(new ActionListener()
	   {
	       public void actionPerformed(ActionEvent e)
	       {
	           frame.setContentPane(new TabbedPannel(1));
	           frame.setVisible(true);
	       }
	   });
	   /*um.addActionListener(new ActionListener()
	   {
		   public void actionPerformed(ActionEvent e)
		   {
			   frame.setContentPane(new TabbedPannel(2));
			   frame.setVisible(true);
		   }
	   });*/
	   /*qm.addActionListener(new ActionListener()
	   {
	       public void actionPerformed(ActionEvent e)
	       {
	           frame.setContentPane(new TabbedPannel(3));
	           frame.setVisible(true);
	       }
	   });*/
	   setLayout(new BorderLayout());
	   add(subPan, BorderLayout.CENTER);
	   
	   /*JPanel N = new JPanel();
	   JPanel S = new JPanel();
	   JPanel E = new JPanel();
	   JPanel W = new JPanel();
	   N.setSize(300, 400);
	   add(N,BorderLayout.NORTH);
	   add(S,BorderLayout.SOUTH);
	   add(E,BorderLayout.EAST);
	   add(W,BorderLayout.WEST);*/
	}
	
}
