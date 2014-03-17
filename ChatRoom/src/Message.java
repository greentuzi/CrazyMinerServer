import java.util.HashMap;
import java.util.Map;


public class Message {
	private String body,extra;
	int flag;
	static Map<String,Integer> map = new HashMap<String,Integer>();
	{
		map.put("login",  0x01);
		map.put("logout",  0x02);
		map.put("user_list_request",  0x03);
		map.put("msg_list_request",  0x04);
		map.put("room_text", 0x05);
		map.put("room_picture", 0x06);
		map.put("user_info_request", 0x07);
		map.put("leave_text",  0x08);
		map.put("leave_voice",  0x09);
		
		map.put("private_chat_request",  0x11);
		map.put("voice_msg_request",  0x12);
		map.put("file_msg_request",  0x13);
		map.put("video_msg_request",  0x14);
		map.put("private_text",  0x15);
		map.put("private_picture",  0x16);
		map.put("private_voice",  0x17);
		map.put("private_file",  0x18);
		map.put("private_video",  0x19);
		
		map.put("OK",  0x21);
		map.put("reject",  0x22);
		map.put("user_list",  0x23);
		map.put("msg_list",  0x24);
		map.put("room_text_transpond",  0x25);
		map.put("room_picture_transpond",  0x26);
		map.put("user_info",  0x27);
		map.put("user_login",  0x28);
		map.put("user_logout",  0x29);
		map.put("server_close", 0x2A);
	}
	public Message(String extra,String body)
	{
		this.extra=extra;
		flag=map.get(extra);
		this.body=body;
	}
	public String toString()
	{
		return flag+"\n"+extra+"\n"+body+"\n##";
	}
}
