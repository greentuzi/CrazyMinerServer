import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;


public class ChatRecord {

	private JFrame frame;
	private JTextField txt_search;
	private String id;
	private JTextPane txt_Record;
	private String last_find;
	private int last_start;
	private JButton btn_Clear;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatRecord window = new ChatRecord("");
					window.frame.setVisible(true);
					window.txt_Record.setText("adsfaffaf\nasdfadfd\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public JButton getClear(){
		return btn_Clear;
	}
	public JFrame getFrame(){
		return frame;
	}
	public ChatRecord(String id) {
		this.id=id;
		last_find="";
		last_start=-1;
		initialize();
	}
	public JTextPane getPane(){
		return txt_Record;
	}
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 321);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
			}
		});
		JPanel panel = new JPanel();
		JScrollPane sp_Record = new JScrollPane();
		sp_Record.setBorder(new TitledBorder("与"+id+"的聊天记录"));
		
		txt_Record = new JTextPane();
		txt_Record.setEditable(false);
		txt_Record.setFocusable(true);
		sp_Record.setViewportView(txt_Record);
		
		JTextArea textArea = new JTextArea();
		sp_Record.setRowHeaderView(textArea);
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.SOUTH, panel, 283, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, sp_Record, 10, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, sp_Record, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, sp_Record, 207, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, sp_Record, 424, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, panel, 213, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().setLayout(springLayout);
		
		txt_search = new JTextField();
		txt_search.setColumns(10);
		
		JButton btn_Search = new JButton("\u67E5\u627E");
		btn_Search.setFont(new Font("宋体", Font.PLAIN, 12));
		btn_Search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s=txt_search.getText();
				String content=txt_Record.getText();
				if (!s.equals(last_find)){
					int start=content.indexOf(s);
					if (start!=-1){
						txt_Record.requestFocus();
						txt_Record.select(start,start+s.length());
						last_start=start;
						last_find=s;
					}
				}
				else {
					int start=content.indexOf(s,last_start+s.length());
					if (start!=-1){
						txt_Record.requestFocus();
						txt_Record.select(start,start+s.length());
						last_start=start;
						last_find=s;
					}
					else {
						txt_Record.requestFocus();
						txt_Record.select(last_start,last_start+s.length());
					}
				}
			}
		});
		
		btn_Clear = new JButton("\u6E05\u7A7A\u804A\u5929\u8BB0\u5F55");
		btn_Clear.setFont(new Font("宋体", Font.PLAIN, 12));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(btn_Clear, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(btn_Search, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
							.addGap(10)
							.addComponent(txt_search, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txt_search, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_Search))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(btn_Clear, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		frame.getContentPane().add(panel);
		frame.getContentPane().add(sp_Record);
	}
}
