import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends PublicUI{

	private ServerSocket server;
	private boolean isStart=false;
	private JButton btn_Startup;
	private JButton btn_Stop;
	private Service_Thread server_thread;
	private ArrayList<Client_Thread> clients;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Server() {
		initialize();
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		setLayout();

		btn_Stop.setEnabled(false);
		btn_Send.setEnabled(false);
		clients=new ArrayList<Client_Thread>();
		
		btn_Startup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String port=txt_Port.getText();
				if (!isPort(port))
					return;
				
				isStart = true;
				try{
					server=new ServerSocket(Integer.parseInt(port));
					server_thread=new Service_Thread(server);
					txt_Msg.setText(txt_Msg.getText()+"服务器已开启\n");
					server_thread.start();
					btn_Startup.setEnabled(false);
					btn_Stop.setEnabled(true);
					btn_Send.setEnabled(true);
				} catch (Exception e1)
				{
					errorBox("服务器启动失败");
					e1.printStackTrace();
				}
			}
		});
		
		btn_Stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeServer();
			}
		});
	
	
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();// 关闭服务器
				}
				System.exit(0);// 退出程序
			}
		});

		pn_ConInfo.setLayout(gl_pn_ConInfo);
		frame.getContentPane().setLayout(groupLayout);	
	}

	private void closeServer()
	{
		server_thread.stop();
		for (int i=clients.size()-1;i>=0;--i)
		{
			clients.get(i).getOutput().print(new Message("server_close","").toString());
			clients.get(i).getOutput().flush();
			clients.get(i).disconnect();
			listModel.removeAllElements();
		}
		isStart=false;
		try {
			server.close();
			txt_Msg.setText(txt_Msg.getText()+"服务器已关闭\n");
			btn_Startup.setEnabled(true);
			btn_Stop.setEnabled(false);
			btn_Send.setEnabled(false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	class Service_Thread extends Thread{
		ServerSocket server;
		public Service_Thread(ServerSocket server)
		{
			this.server=server;
		}
		
		public void run()
		{
			while (true){
				Socket socket;
				try {
					socket = server.accept();
					System.out.println(socket.getPort());
					Client_Thread client_thread=new Client_Thread(socket);
					client_thread.start();
					clients.add(client_thread);
					} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Client_Thread extends Thread{
		Socket socket;
		User user;
		BufferedReader input;
		PrintWriter output;
		public Client_Thread(Socket s)
		{
			socket=s;
			user=new User();
			try {
				input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output=new PrintWriter(socket.getOutputStream());
				} catch (IOException e) {
				e.printStackTrace();
				}
			}

		public void run()
		{
			try{
				char[] ch=new char[2000];
				input.read(ch);
				String msg=String.valueOf(ch).trim();
				while (msg!=null && !msg.equals("")){
					msgParse(msg);
					Arrays.fill(ch, '\0');
					input.read(ch);
					msg=String.valueOf(ch).trim();
				}
				for (int i=0;i<clients.size();i++) {
					if (clients.get(i).getId() == this.getId()) {
						clients.remove(i);
						return;
					}
				}
			
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		void sendlist(){
			String online="";
			for (int i=0;i<listModel.size();i++){
				if (listModel.get(i).equals(user.getID()))
					continue;
				online+=(listModel.get(i)+"\n");
			}
			output.print(new Message("user_list",online).toString());
			output.flush();
		}
		void sendmsg(String flag, String msg, boolean self){
			for (int i=0;i<clients.size();i++){
				if (self || clients.get(i).getId()!=this.getId())
				{
					clients.get(i).output.print(new Message(flag,msg).toString());
					clients.get(i).output.flush();
				}
			}
		}
		public void msgParse(String msg) {
			 StringTokenizer tk=new StringTokenizer(msg,"\n");
			 String code=tk.nextToken().trim();
			 //String extra=tk.nextToken().substring(1).trim();
			 String body=tk.nextToken("##").substring(1).trim();
			 switch(Integer.parseInt(code))
			 {
			 
			 case 0x01:
				 String[] spt=body.split("\n");
				 user.setID(spt[0]);
				 user.setIP(socket.getInetAddress().getHostAddress());
				 user.setPort(socket.getPort());
				 user.setServerPort(Integer.parseInt(spt[1]));
				 System.out.println(spt[1]);
				 txt_Msg.setText(txt_Msg.getText()+user.getID()+"上线了\n");
				 listModel.addElement(user.getID());
				 sendmsg("user_login",spt[0],false);
				 break;
			 case 0x02:
				 txt_Msg.setText(txt_Msg.getText()+user.getID()+"下线了\n");				 
				 sendmsg("user_logout",user.getID(),false);
				 disconnect();
				 break;
			 case 0x03:
				 sendlist();
				 break;
			 case 0x05:
				 body=user.getID()+"说: "+body;
				 txt_Msg.setText(txt_Msg.getText()+body+"\n");
				 sendmsg("room_text_transpond",body,true);
				 break;
			 case 0x07:
				 sendUser(body);
			 }
		 }
		
		private void sendUser(String id) {
			for (int i=0;i<clients.size();++i)
			{
				User tmpUsr=clients.get(i).getUser();
				if (tmpUsr.getID().equals(id))
				{
					output.print(new Message(
							"user_info",
							tmpUsr.getID()+"\n"+
							tmpUsr.getIP()+"\n"+
							tmpUsr.getServerPort()).toString());
					output.flush();
					break;
				}
			}
		}
		@SuppressWarnings("deprecation")
		public void disconnect()
		{
			 try {
				 input.close();
				 output.close();
				 socket.close();
				 listModel.removeElement(user.getID());
				 clients.remove(this);
				 stop();
			 	}
			 catch (IOException e) { 
				 e.printStackTrace();
			 }
		}
		
		public PrintWriter getOutput()
		{
			return output;
		}
		public User getUser()
		{
			return user;
		}
	}	
	
	private void setLayout()
	{
		frame.setTitle("服务器");
		frame.setBounds(100, 100, 613, 498);
		btn_Startup = new JButton("启动");
		btn_Stop = new JButton("停止");

		btn_Startup.setFont(new Font("宋体", Font.PLAIN, 18));
		btn_Stop.setFont(new Font("宋体", Font.PLAIN, 18));

		
		txt_Port.setText("8765");
		
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addComponent(pn_ConInfo, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addComponent(sp_Msg, GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(pn_Send, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE))
							.addComponent(sp_OnlineUser, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)))
			);
			
			gl_pn_ConInfo = new GroupLayout(pn_ConInfo);
			gl_pn_ConInfo.setHorizontalGroup(
				gl_pn_ConInfo.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_pn_ConInfo.createSequentialGroup()
						.addGap(16)
						.addComponent(lbl_Port)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(txt_Port, 99, 99, 99)
						.addGap(74)
						.addComponent(btn_Startup, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(btn_Stop, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(139, Short.MAX_VALUE))
			);
			gl_pn_ConInfo.setVerticalGroup(
				gl_pn_ConInfo.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_pn_ConInfo.createSequentialGroup()
						.addGroup(gl_pn_ConInfo.createParallelGroup(Alignment.BASELINE)
							.addComponent(txt_Port, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
							.addComponent(btn_Stop, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
							.addComponent(btn_Startup, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_Port, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
						.addGap(45))
			);
	}


}
