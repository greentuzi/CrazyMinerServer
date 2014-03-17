import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;


abstract public class PublicUI {

	protected JFrame frame;
	protected JLabel lbl_Port;
	protected JTextField txt_Port;
	protected JTextPane txt_Send;
	protected JTextPane txt_Msg;
	protected JPanel pn_ConInfo;
	protected JPanel pn_Send;
	protected JScrollPane sp_Msg;
	protected JScrollPane sp_OnlineUser;
	protected JScrollPane sp_Send; 
	protected GroupLayout gl_pn_ConInfo;
	protected GroupLayout gl_pn_Send;
	protected GroupLayout groupLayout;
	protected JButton btn_Send;
	protected DefaultListModel<String> listModel;
	protected JList<String> list_OnlineUser;

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pn_ConInfo = new JPanel();
		pn_ConInfo.setBorder(new TitledBorder("连接信息"));
		
		sp_Msg = new JScrollPane();
		sp_Msg.setBorder(new TitledBorder("信息显示区"));
		
		pn_Send = new JPanel();
		pn_Send.setBorder(new TitledBorder("发消息"));
		
		listModel = new DefaultListModel<String>();
		list_OnlineUser = new JList<String>(listModel);
		sp_OnlineUser = new JScrollPane(list_OnlineUser);
		sp_OnlineUser.setBorder(new TitledBorder("上线用户列表"));
		
		sp_Send = new JScrollPane();
		
		btn_Send = new JButton("发送");
		btn_Send.setFont(new Font("宋体", Font.PLAIN, 18));
		gl_pn_Send = new GroupLayout(pn_Send);
		gl_pn_Send.setHorizontalGroup(
			gl_pn_Send.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pn_Send.createSequentialGroup()
					.addComponent(sp_Send, GroupLayout.PREFERRED_SIZE, 377, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btn_Send, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(4))
		);
		gl_pn_Send.setVerticalGroup(
			gl_pn_Send.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pn_Send.createSequentialGroup()
					.addGroup(gl_pn_Send.createParallelGroup(Alignment.LEADING)
						.addComponent(sp_Send, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_pn_Send.createSequentialGroup()
							.addContainerGap()
							.addComponent(btn_Send, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(213, Short.MAX_VALUE))
		);
		
		
		
		groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(pn_ConInfo, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(pn_Send, GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
								.addComponent(sp_Msg, GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(sp_OnlineUser, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)))
					.addGap(20))
		);		
		
		txt_Send = new JTextPane();
		sp_Send.setViewportView(txt_Send);
		pn_Send.setLayout(gl_pn_Send);
		
		txt_Msg = new JTextPane();
		txt_Msg.setEditable(false);
		sp_Msg.setViewportView(txt_Msg);
		
		lbl_Port = new JLabel("端口号");
		lbl_Port.setFont(new Font("宋体", Font.PLAIN, 16));
		
		txt_Port = new JTextField();
		txt_Port.setColumns(10);
		
	}

	 public boolean isIP(String IP){//判断是否是一个IP 
	        IP = IP.trim(); 
	        if(IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){ 
	            String s[] = IP.split("\\."); 
	            if(Integer.parseInt(s[0])<255) 
	                if(Integer.parseInt(s[1])<255) 
	                    if(Integer.parseInt(s[2])<255) 
	                        if(Integer.parseInt(s[3])<255) 
	                            return true;
	        }
			JOptionPane.showMessageDialog(frame, "非法ip地址", "错误",
					JOptionPane.ERROR_MESSAGE);
	        return false; 
	 } 
	 
	 public boolean isPort(String port)
	 {
	 	port=port.trim();
		if (!port.matches("[0-9]+"))
		{
			JOptionPane.showMessageDialog(frame, "端口号必须为整数", "错误",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		else
		{
			int p=Integer.parseInt(port);
			if (p<1024 || p>65535)
			{
				JOptionPane.showMessageDialog(frame, "端口号超出可用范围", "错误",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true; 
	 }
	 
	 void errorBox(String msg)
	 {
		 JOptionPane.showMessageDialog(frame, msg, "错误",
				 JOptionPane.ERROR_MESSAGE);
	 }
	
}
