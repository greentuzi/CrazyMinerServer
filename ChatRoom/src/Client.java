import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends PublicUI{

	private JTextField txt_IP;
	private JTextField txt_ID;
	private JPasswordField txt_Password;
	private JLabel lbl_IP;
	private JLabel lbl_ID;
	private JLabel lbl_Password;
	private JButton btn_Register;
	private JButton btn_Disconnect;
	private JButton btn_Connect;	
	private User user;
	private boolean isStart=false;
	private Client_Thread client_thread;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client client = new Client();
					client.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Client() {
		initialize();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		
		setLayout();

		btn_Connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (LoginCheck()==false)
					return ;
				user=new User(txt_ID.getText(),txt_IP.getText(),"",Integer.parseInt(txt_Port.getText()));
				try {
					client_thread=new Client_Thread(user);
					isStart = true;
					txt_Msg.setText(txt_Msg.getText()+"成功连接服务器\n");
					client_thread.start();
					btn_Connect.setEnabled(false);
					btn_Disconnect.setEnabled(true);
					btn_Send.setEnabled(true);
				} catch (Exception e1) {
					e1.printStackTrace();
						errorBox("连接服务器失败");
				}
			}
		});
		
		btn_Disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeClient();
			}
		});
		
		btn_Send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg=txt_Send.getText();
				if (msg!=null&&!msg.equals("")){
					client_thread.send(new Message("room_text",msg).toString());
				}
				txt_Send.setText("");
			}
		});
		
		list_OnlineUser.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2) {
					client_thread.send(new Message("user_info_request",list_OnlineUser.getSelectedValue()).toString());
				}
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			
		});
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeClient();
				}
				System.exit(0);
			}
		});
	}
		
	private void closeClient()
	{
		client_thread.send(new Message("logout","").toString());
		client_thread.disconnect();
	}
	
	class Client_Thread extends Thread{
		PrintWriter output;
		BufferedReader input;
		User info;
		int port;
		Socket socket;
		private P2Pinterface.Service_Thread service_thread;
		public Client_Thread(User info) throws UnknownHostException, IOException{
			this.info=info;
			socket=new Socket(info.getIP(),info.getPort());
			input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output=new PrintWriter(socket.getOutputStream());
			
			service_thread=new P2Pinterface().new Service_Thread(user);
			info.setServerPort(service_thread.getPort());
			service_thread.start();
			
			//System.out.println(info.getPort()+"    "+info.getServerPort());
			send(new Message("login",info.getID()+"\n"+info.getServerPort()).toString());			
		}

		public void run()
		{
			try{
				send(new Message("user_list_request","").toString());
				char[] ch=new char[2000];
				String msg;
				while (isStart){
					input.read(ch);
					msg=String.valueOf(ch).trim();
					if (!msg.equals("") && !msgParse(msg))
						return ;
					Arrays.fill(ch, '\0');
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		void send(String msg){
			output.print(msg);
			output.flush();
		}
		
		void setOnline(String online){
			StringTokenizer st = new StringTokenizer(online,"\n");
			while (st.hasMoreTokens()){
				String id=st.nextToken();
				if (!id.equals(info.getID())){
					listModel.addElement(id);
				}
			}
		}

		public synchronized void disconnect()
		{
			try {
				isStart=false;
				input.close();
				output.close();
				socket.close();
				txt_Msg.setText(txt_Msg.getText()+"连接已断开\n");
				listModel.removeAllElements();
				isStart=false;
				btn_Connect.setEnabled(true);
				btn_Disconnect.setEnabled(false);
				btn_Send.setEnabled(false);
				
			} catch (IOException e) {
				errorBox("断开连接失败");
				e.printStackTrace();
				isStart=true;
			}
		}
		
		public boolean msgParse(String msg) throws IOException {
			 StringTokenizer tk=new StringTokenizer(msg,"\n");
			 String code=tk.nextToken().trim();
			 String extra=tk.nextToken().substring(1).trim();
			 String body=tk.nextToken("##").substring(1).trim();
			 switch(Integer.parseInt(code))
			 {
			 case 0x23:
				 setOnline(body);
				 break;
			 case 0x25:
				txt_Msg.setText(txt_Msg.getText()+body+"\n");
				break;
							
			 case 0x27:
				 String spt[]=body.split("\n");
				 User private_user=new User();
				 private_user.setID(spt[0]);
				 private_user.setIP(spt[1]);
				 private_user.setServerPort(Integer.parseInt(spt[2]));	 
				 System.out.println("连接到:");
				 System.out.println(private_user.getIP()+private_user.getServerPort());
				 //判重to be done	
				 ArrayList<P2Pinterface> list=service_thread.getTalkingList();
				 int i;
				 for (i=0;i<list.size();i++){
					 if (private_user.getID().equals(list.get(i).getName())){
						 break;
					 }
				 }
				 if (i<list.size()){
					 break;
				 }
				 P2Pinterface newTalk=new P2Pinterface(private_user,user,list);
				 newTalk.setServicePort(service_thread.getPort());
				 System.out.println("服务器端口:");
				 System.out.println(service_thread.getPort());
				 service_thread.getTalkingList().add(newTalk);
				 break;
				
			 case 0x28:
				txt_Msg.setText(txt_Msg.getText()+body+"上线了\n");
				listModel.addElement(body);
				break;
			 case 0x29:
				txt_Msg.setText(txt_Msg.getText()+body+"下线了\n");
				listModel.removeElement(body);
				break;
			 case 0x2A:
				txt_Msg.setText(txt_Msg.getText()+"服务器已关闭\n");
				disconnect();
				return false;
			 }
			 return true;
		}
		
	}

	private void setLayout()
	{
		frame.setTitle("聊天室");
		frame.setBounds(100, 100, 630, 549);
		
		lbl_Port = new JLabel("服务器IP");
		lbl_Port.setFont(new Font("宋体", Font.PLAIN, 16));
		
		txt_Port = new JTextField();
		txt_Port.setColumns(10);
		
		lbl_IP = new JLabel("端口号");
		lbl_IP.setFont(new Font("宋体", Font.PLAIN, 16));
		
		txt_IP = new JTextField();
		txt_IP.setColumns(10);
		
		lbl_ID = new JLabel("  用户ID");
		lbl_ID.setFont(new Font("宋体", Font.PLAIN, 16));
		
		txt_ID = new JTextField();
		txt_ID.setColumns(10);
		
		btn_Disconnect = new JButton("断开");
		btn_Disconnect.setFont(new Font("宋体", Font.PLAIN, 18));
		btn_Disconnect.setEnabled(false);

		
		lbl_Password = new JLabel("用户密码");
		lbl_Password.setFont(new Font("宋体", Font.PLAIN, 16));
		
		txt_Password = new JPasswordField();
		txt_Password.setColumns(10);
		
		btn_Register = new JButton("注册");
		btn_Register.setFont(new Font("宋体", Font.PLAIN, 18));
		
		btn_Connect = new JButton("连接");
		btn_Connect.setFont(new Font("宋体", Font.PLAIN, 18));
		
		txt_IP.setText("127.0.0.1");
		txt_Port.setText("8765");
		
		btn_Send.setEnabled(false);
		
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addComponent(pn_ConInfo, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addComponent(sp_Msg, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(pn_Send, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE))
							.addComponent(sp_OnlineUser, GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)))
			);
			
			gl_pn_ConInfo = new GroupLayout(pn_ConInfo);
			gl_pn_ConInfo.setHorizontalGroup(
				gl_pn_ConInfo.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_pn_ConInfo.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_pn_ConInfo.createSequentialGroup()
								.addGap(6)
								.addComponent(lbl_ID))
							.addGroup(gl_pn_ConInfo.createSequentialGroup()
								.addGap(8)
								.addComponent(lbl_Port)))
						.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.LEADING, false)
							.addComponent(txt_IP)
							.addComponent(txt_ID, GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED, 13, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.TRAILING)
							.addComponent(lbl_Password)
							.addComponent(lbl_IP))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.TRAILING)
							.addComponent(txt_Password, 125, 125, 125)
							.addComponent(txt_Port, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.LEADING, false)
							.addComponent(btn_Disconnect, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btn_Connect, GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(btn_Register, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
						.addGap(6))
			);
			gl_pn_ConInfo.setVerticalGroup(
				gl_pn_ConInfo.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_pn_ConInfo.createSequentialGroup()
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_pn_ConInfo.createSequentialGroup()
								.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.BASELINE)
									.addComponent(btn_Connect, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
									.addComponent(txt_Port, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_IP, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_Port, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
									.addComponent(txt_IP, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.BASELINE)
										.addComponent(btn_Disconnect, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
										.addComponent(txt_Password, GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
										.addComponent(lbl_ID)
										.addComponent(lbl_Password))
									.addComponent(txt_ID, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)))
							.addGroup(gl_pn_ConInfo.createSequentialGroup()
								.addGap(10)
								.addComponent(btn_Register, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap())
			);
			pn_ConInfo.setLayout(gl_pn_ConInfo);
			frame.getContentPane().setLayout(groupLayout);
	}
	
	private boolean LoginCheck()
	{
		if (isStart){
			errorBox("请不要重复登录");
			return false;
		}

		if (txt_ID.getText().trim().equals(""))
		{
			errorBox("用户名不能为空");
			return false;
		}
		
		String port=txt_Port.getText().trim();

		return isIP(txt_IP.getText()) && isPort(port); 
	}
	

}