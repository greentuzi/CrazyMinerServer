//用户信息类
public class User{
	private String ID;
	private String IP;
	private String Password;
	private int Port;
	private int ServerPort;
	public User(){
		ID=null;
		IP=null;
		Password=null;
	}
	public User(String id, String ip, String password,int port) {
		ID = id;
		IP = ip;
		Password=password;
		Port = port;
	}

	public String getID() {
		return ID;
	}

	public User setID(String id) {
		ID = id;
		return this;
	}

	public String getIP() {
		return IP;
	}

	public User setIP(String ip) {
		IP = ip;
		return this;
	}
	
	public String getPassword() {
		return Password;
	}

	public User setPassword(String password) {
		Password = password;
		return this;
	}
	
	public User setPort(int port) {
		Port = port;
		return this;
	}
	
	public int getPort() {
		return Port;
	}
	
	public User setServerPort(int ServerPort) {
		this.ServerPort = ServerPort;
		return this;
	}
	
	public int getServerPort() {
		return ServerPort;
	}
}