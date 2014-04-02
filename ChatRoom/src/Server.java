import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
public class Server extends PublicUI{

	private ServerSocket server;					//服务器
	private boolean isStart=false;					//启动flag
	private JButton btn_Startup;
	private JButton btn_Stop;
	private Service_Thread server_thread;			//服务器线程
	private ArrayList<Client_Thread> clients;		//客户端们
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
			}
		});
	
	
		frame.addWindowListener(new WindowAdapter() {			//在关闭服务器窗口时的回调函数
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();			// 关闭服务器					
				}
				System.exit(0);				// 退出程序
			}
		});

		pn_ConInfo.setLayout(gl_pn_ConInfo);
		frame.getContentPane().setLayout(groupLayout);	
	}

	private void closeServer()
	{
//		server_thread.stop();
	}
	
	class Service_Thread extends Thread{
		ServerSocket server;				//服务器socket
		int c = 0;
		public Service_Thread(ServerSocket server)
		{
			this.server=server;
		}
		
		public void run()
		{
			while (true){
				Socket socket;
				try {
					socket = server.accept();			//一旦有人接入，则socket成为通信用的ClientSocket
				
					System.out.println(socket.getPort());
					Client_Thread client_thread=new Client_Thread(socket,c++);		//客户线程，传入socket和ID（ID只作临时调试用）
					client_thread.start();			//线程开始，run()
					clients.add(client_thread);
					} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Client_Thread extends Thread{
		int id;
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		public Client_Thread(Socket s,int _id)
		{
			id = _id;
			socket=s;
			try {
				input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output=new PrintWriter(socket.getOutputStream());
				} catch (IOException e) {
				e.printStackTrace();
				}
			}

		public void run() {
			try{
				output.print(Util.getInstance().posInfo208(this)+"\0");
				output.flush();					
				output.print(Util.getInstance().mapInfo201()+"\0");
				output.flush();
			
				char[] ch=new char[2000];
				input.read(ch);					//不断从输入流中获取消息
				String msg=String.valueOf(ch).trim();
				while (msg!=null && !msg.equals("")){
					JSONObject jobj = new JSONObject();					
					//msgParse(msg);				//处理消息
					System.out.println(msg);
					jobj = Util.getInstance().msgParse(msg,this);
					if(jobj != null){
						String sentMsg = jobj + "\0";
						System.out.println(sentMsg);
						sendmsg(sentMsg);
						//output.print(sentMsg);
						//output.flush();						
					}
					Arrays.fill(ch, '\0');
					input.read(ch);
					msg=String.valueOf(ch).trim();
				}

			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		void sendmsg(String msg){			//调试用，发送消息

			for(int i=0;i<clients.size();i++)	{
				clients.get(i).output.print(msg);
				clients.get(i).output.flush();
			}
		}


		public void msgParse(String msg) {
			
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
