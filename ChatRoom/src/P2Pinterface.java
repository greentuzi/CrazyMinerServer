import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;


public class P2Pinterface {
	
	private JFrame frame;
	private User user;
	private User me;
	private ChatRecord record;
	JPanel pn_Icon;
	JButton btn_SendPic;
	JPanel pn_Send;
	JScrollPane sp_Send;
	JButton btn_Send;
	JTextPane txt_Send;
	JTextPane txt_Msg;
	JLabel lblIcon;
	JButton btn_SendDoc;
	JLabel lbl_Name;
	JButton btn_TalkRec;
	JScrollPane sp_Msg;
	JPanel pn_Theme;
	JComboBox<String> cb_Theme;
	Socket socket = null;
	Client_Thread client_thread;
	BufferedReader input;
	PrintWriter output;
	String name;
	int service_port;
	boolean Is_service;
	ArrayList<P2Pinterface> list;
	File file_rec;
	FileOutputStream print_rec;
	final int DISCONNECT=0;
	final int MSG=1;
	final int ID=2;
	final int OK=3;
	final int PORT=4;
	public P2Pinterface(){}
	public P2Pinterface(final User user,final User m,ArrayList<P2Pinterface> list) throws IOException {
		this.user=user;
		this.me=new User();
		this.me.setID(m.getID());
		this.list=list;
		initialize();
		Is_service=false;
		frame.setVisible(true);
		set(user.getID());
		record=new ChatRecord(user.getID());
		record.getFrame().setVisible(false);
		file_rec=new File(user.getID()+".txt");
		boolean flag=true;
		if (!file_rec.exists()){
			file_rec.createNewFile();
			flag=false;
		}
		if (flag){
			/*FileReader fr=new FileReader(file_rec);
	         BufferedReader br=new BufferedReader(fr);
	         String temp=null;
	         temp=br.readLine();
	         while(temp!=null){
	        	 System.out.println(new String(temp.getBytes("utf-8")));
	        	 record.getPane().setText(record.getPane().getText()+temp+"\n");
	             temp=br.readLine();
	         }
	         fr.close();
	         br.close();*/
			FileInputStream fi=new FileInputStream(file_rec);
			byte[] b = new byte[1024];
			int ret;
			while((ret=fi.read(b,0,1024))!=-1)
			{
				record.getPane().setText(record.getPane().getText()+new String(b,0,ret)+"\n");
				
			}
		}
		print_rec = new FileOutputStream(file_rec,true);
		record.getClear().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileWriter fw = new FileWriter(file_rec); 
					fw.write(""); 
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				record.getPane().setText("");
			}
		});
	}
	
	public P2Pinterface(final Socket socket, final User user,User m,ArrayList<P2Pinterface> list) throws IOException {
		this.socket=socket;
		this.user=user;
		this.list=list;
		this.me=new User();
		this.me.setID(m.getID());
		initialize();
		System.out.println("new:");
		System.out.println(me.getID());
		Is_service=true;
		frame.setVisible(true);
		try {
			input=new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			output=new PrintWriter(this.socket.getOutputStream());
			System.out.println(user.getServerPort());
			client_thread=new Client_Thread(this.socket);
			client_thread.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void setSocket(Socket s) throws IOException{
		this.socket=s;
		input=new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output=new PrintWriter(this.socket.getOutputStream());
		client_thread=new Client_Thread(this.socket);
		client_thread.start();
	}
	public String getName(){
		return name;
	}
	public void setServicePort(int p){
		service_port=p;
	}
	public void disconnect_client() throws IOException{
		if (!Is_service){
			for (int i=0;i<list.size();i++){
				if (list.get(i).getName().equals(getName())){
					list.remove(i);
					break;
				}
			}
			sendData(DISCONNECT,"");
		}
		else {
			Is_service=false;
		}
		socket.close();
		socket=null;
		input.close();
		input=null;
		output.close();
		output=null;
		client_thread.stop();
	}
	
	public void disconnect_service() throws IOException{
		if (Is_service){
			for (int i=0;i<list.size();i++){
				if (list.get(i).getName().equals(getName())){
					list.remove(i);
					break;
				}
			}
			sendData(DISCONNECT,"");
			String s=input.readLine();
			if (s!=null&&s.equals("3##ok")){
				socket.close();
				socket=null;
				input.close();
				input=null;
				output.close();
				output=null;
				client_thread.stop();
			}
		}
		else {
			sendData(OK,"");
			socket.close();
			socket=null;
			input.close();
			input=null;
			output.close();
			output=null;
			client_thread.stop();
		}
	}
	
	class Service_Thread extends Thread{
		ServerSocket server;
		User me;
		private ArrayList<P2Pinterface> talking = new ArrayList<P2Pinterface>();
		public Service_Thread(User me)
		{
			this.me=me;
			try {
				server=new ServerSocket(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public int getPort()
		{
			return server.getLocalPort();
		}
		
		public ArrayList<P2Pinterface> getTalkingList()
		{
			return talking;
		}
		public void run()
		{
			while (true){
				Socket socket;
				User usr=new User();
				try {
					socket = server.accept();
					usr.setIP(socket.getInetAddress().getHostAddress());
					System.out.println("来自:");
					System.out.println(usr.getIP()+usr.getServerPort());
					BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String temp=in.readLine();
					char id[]=temp.toCharArray();
					temp=in.readLine();
					char port[]=temp.toCharArray();
					System.out.println(port);
					int i;
					System.out.println("size:");
					System.out.println(talking.size());
					for (i=0;i<talking.size();i++){
						String a,b;
						a=talking.get(i).user.getID();
						b=new String(id,3,id.length-3);
						if (a==null){
							System.out.println("a is null");
						}
						if (a.equals(b)){
							break;
						}
					}
					if (i<talking.size()){
						talking.get(i).setSocket(socket);
						temp=in.readLine();
						talking.get(i).getData(temp);
						continue;
					}
					P2Pinterface p2p=new P2Pinterface(socket,usr,this.me,talking);
					temp=in.readLine();
					p2p.set(new String(id,3,id.length-3));
					p2p.user.setServerPort(Integer.parseInt(new String(port,3,port.length-3)));
					p2p.setServicePort(getPort());
					p2p.getData(temp);
					System.out.println("服务器端口:");
					System.out.println(getPort());
					talking.add(p2p);
					} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Client_Thread extends Thread {
		Socket socket;
		public Client_Thread(Socket s) {
			socket=s;
		}
		public void run() {
			try{
				String msg;
				msg=input.readLine();
				while (true){
					getData(msg);
					if (socket==null){
						break;
					}
					msg=input.readLine();
				}
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
	}
	
	private void initialize() throws IOException {
		frame = new JFrame();
		frame.setTitle("与"+user.getID()+"聊天中");
		frame.setBounds(100, 100, 600, 501);
		
		pn_Icon = new JPanel();
		pn_Icon.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		btn_SendPic = new JButton("发送图片");
		btn_SendPic.setFont(new Font("宋体", Font.PLAIN, 14));
		btn_SendPic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		btn_SendDoc = new JButton("发送文档");
		btn_SendDoc.setFont(new Font("宋体", Font.PLAIN, 14));
		
		btn_TalkRec = new JButton("聊天记录");
		btn_TalkRec.setFont(new Font("宋体", Font.PLAIN, 14));
		btn_TalkRec.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				record.getFrame().setVisible(true);
			}
		});
		
		lbl_Name = new JLabel("对方ID");
		lbl_Name.setFont(new Font("Serif", Font.PLAIN, 18));
		
		sp_Msg = new JScrollPane();
		sp_Msg.setBorder(new TitledBorder("消息显示区"));
		
		pn_Theme = new JPanel();
		pn_Theme.setLayout(new GridLayout(1,1));
		
		pn_Theme.setBorder(new TitledBorder("选择主题"));
		
		pn_Send = new JPanel();
		pn_Send.setBorder(new TitledBorder("写消息"));
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(pn_Send, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(pn_Icon, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(lbl_Name, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
								.addComponent(sp_Msg, GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(btn_TalkRec, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
								.addComponent(pn_Theme, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
								.addComponent(btn_SendPic, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
								.addComponent(btn_SendDoc, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(lbl_Name, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(pn_Icon, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(sp_Msg, GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pn_Theme, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btn_SendPic, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btn_SendDoc, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 132, Short.MAX_VALUE)
							.addComponent(btn_TalkRec, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(pn_Send, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		
		sp_Send = new JScrollPane();
		
		btn_Send = new JButton("\u53D1\u9001");
		btn_Send.setFont(new Font("宋体", Font.PLAIN, 16));
		GroupLayout gl_pn_Send = new GroupLayout(pn_Send);
		gl_pn_Send.setHorizontalGroup(
			gl_pn_Send.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pn_Send.createSequentialGroup()
					.addComponent(sp_Send, GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btn_Send, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
					.addGap(2))
		);
		gl_pn_Send.setVerticalGroup(
			gl_pn_Send.createParallelGroup(Alignment.LEADING)
				.addComponent(sp_Send, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
				.addGroup(gl_pn_Send.createSequentialGroup()
					.addGap(18)
					.addComponent(btn_Send, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(22, Short.MAX_VALUE))
		);
		
		txt_Send = new JTextPane();
		sp_Send.setViewportView(txt_Send);
		pn_Send.setLayout(gl_pn_Send);
		
		txt_Msg = new JTextPane();
		txt_Msg.setEditable(false);
		sp_Msg.setViewportView(txt_Msg);
		
		String[] themes={"默认","绿","蓝"};
		cb_Theme = new JComboBox<String>(themes);
		cb_Theme.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent e) {
				int color_txt=0xFFFFFF;
				int color_bg=0xEEEEEE;
				switch(e.getItem().toString())
				{
				case "绿":
					color_txt=0xD1F3D0;
					color_bg=0x91D74F;
					break;
				case "蓝":
					color_txt=0xE1E6F6;
					color_bg=0xAFE6FF;
					
				default:
					break;
				}
				txt_Msg.setBackground(new Color(color_txt));
				txt_Send.setBackground(new Color(color_txt));
				frame.getContentPane().setBackground(new Color(color_bg));
				pn_Send.setBackground(new Color(color_bg));
				sp_Msg.setBackground(new Color(color_bg));
				pn_Theme.setBackground(new Color(color_bg));
			}
			
		});
		cb_Theme.setFont(new Font("宋体", Font.PLAIN, 14));
		pn_Theme.add(cb_Theme);
		
		lblIcon = new JLabel("<html>对方<br>头像</html>");
		lblIcon.setFont(new Font("宋体", Font.PLAIN, 14));
		pn_Icon.add(lblIcon);
		frame.getContentPane().setLayout(groupLayout);
		btn_Send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (socket==null)
				{
					try {
						if (txt_Send.getText()==null||txt_Send.getText().equals("")){
							return;
						}
						socket=new Socket(user.getIP(),user.getServerPort());
						System.out.println("link to:");
						System.out.println(user.getID());
						//System.out.println(user.getServerPort());
						input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
						output=new PrintWriter(socket.getOutputStream());
						sendData(ID,"");
						sendData(PORT,"");
						sendData(MSG,txt_Send.getText());
						txt_Msg.setText(txt_Msg.getText()+me.getID()+"说:"+txt_Send.getText()+"\n");
						record.getPane().setText(record.getPane().getText()+me.getID()+"说:"+txt_Send.getText()+"\n");
						/*PrintStream ps=new PrintStream(print_rec);
						ps.println(new String((me.getID()+"说:"+txt_Send.getText()).getBytes("utf-8")));
						ps.close();*/
						print_rec.write((me.getID()+"说:"+txt_Send.getText()+"\n").getBytes());
						
						clear();
						client_thread=new Client_Thread(socket);
						client_thread.start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				else {
					sendData(MSG,txt_Send.getText());
					if (txt_Send.getText()!=null&&!txt_Send.getText().equals("")){
						txt_Msg.setText(txt_Msg.getText()+me.getID()+"说:"+txt_Send.getText()+"\n");
						record.getPane().setText(record.getPane().getText()+me.getID()+"说:"+txt_Send.getText()+"\n");
					}
					/*PrintStream ps=new PrintStream(print_rec);
					try {
						ps.println(new String((me.getID()+"说:"+txt_Send.getText()).getBytes("utf-8")));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					ps.close();*/
					try {
						print_rec.write((me.getID()+"说:"+txt_Send.getText()+"\n").getBytes());
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					clear();
				}
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					print_rec.close();
					record.getFrame().setEnabled(false);
					record=null;
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				if (socket==null){
					for (int i=0;i<list.size();i++){
						if (list.get(i).getName().equals(getName())){
							list.remove(i);
							return;
						}
					}
					return;
				}
				try {
					if (Is_service){
						disconnect_service();
					}
					else {
						disconnect_client();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}// 关闭服务器
			}
		});
	}
	public void clear(){
		txt_Send.setText("");
	}
	void errorBox(String msg)
	{
		JOptionPane.showMessageDialog(frame, msg, "错误",
			 JOptionPane.ERROR_MESSAGE);
	}
	
	public void setID(String s){
		lbl_Name.setText(s);
	}
	
	public void setTitle(String s){
		frame.setTitle("与"+s+"聊天中");
	} 
	
	public void set(String s) throws IOException{
		setID(s);
		setTitle(s);
		name=s;
		user.setID(s);
		record=new ChatRecord(user.getID());
		record.getFrame().setVisible(false);
		file_rec=new File(user.getID()+".txt");
		boolean flag=true;
		if (!file_rec.exists()){
			file_rec.createNewFile();
			flag=false;
		}
		if (flag){
			 FileReader fr=new FileReader(file_rec);
	         BufferedReader br=new BufferedReader(fr);
	         String temp=null;
	         temp=br.readLine();
	         while(temp!=null){
	        	 System.out.println(temp);
	        	 record.getPane().setText(record.getPane().getText()+temp+"\n");
	             temp=br.readLine();
	         }
	         fr.close();
	         br.close();
		}
		print_rec = new FileOutputStream(file_rec,true);
		record.getClear().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					 FileWriter fw = new FileWriter(file_rec); 
					 fw.write(""); 
					 fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				record.getPane().setText("");
			}
		});
	}
	public void sendData(int flag,String s){
		if (flag==DISCONNECT){
			output.println("0"+"##"+"disconnect");
			output.flush();
		}
		else if (flag==MSG) {
			output.println("1"+"##"+s);
			output.flush();
		}
		else if (flag==ID){
			output.println("2"+"##"+me.getID());
			output.flush();
		}
		else if (flag==OK){
			output.println("3"+"##"+"ok");
			output.flush();
		}
		else {
			output.println("4"+"##"+String.valueOf(service_port));
			output.flush();
		}
	}
	public void getData(String s) throws IOException{
		if (s==null||s.equals("")){
			return;
		}
		char msg[]=s.toCharArray();
		if (msg[0]=='0'){
			if (Is_service){
				disconnect_client();
			}
			else {
				disconnect_service();
			}
		}
		else if (msg[0]=='1'){
			txt_Msg.setText(txt_Msg.getText()+name+"说:"+new String(msg,3,msg.length-3)+"\n");
			record.getPane().setText(record.getPane().getText()+name+"说:"+new String(msg,3,msg.length-3)+"\n");
			/*PrintStream ps=new PrintStream(print_rec);
			ps.println(new String((name+"说:"+new String(msg,3,msg.length-3)+"说:"+txt_Send.getText()).getBytes("utf-8")));
			ps.close();*/
			print_rec.write((name+"说:"+new String(msg,3,msg.length-3)+"\n").getBytes());
		}
	}
}
