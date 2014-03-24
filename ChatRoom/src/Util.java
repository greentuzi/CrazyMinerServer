import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class Util {
	
	static Util instance;

	private Util(){};
	public static Util getInstance(){
		if (instance == null)
			instance = new Util();
		return instance;
	}
	
	JSONObject shout242(double angle , Server.Client_Thread client){
		double originX,originY;
		String playerName = String.valueOf(client.id & 1); //角色名
		String reachTime = "1";  //到达时间
		String returnTime = "1"; //返回时间
		
		if((client.id & 1) == 0)
		{
			originX = 341.5;
		}
		else
		{
			originX = 1024.5;
		}
		originY = 144.0;
		
		double x = Math.cos(angle)*300+originX;
		double y = Math.sin(angle)*300+originY;
		/*
		System.out.println(client.id);
		
		String desPoint = String.valueOf(x)+"\n"+String.valueOf(y); //终点坐标

		String msg = "242##launchInfo##" + playerName + "\n" + desPoint + "\n" + reachTime + "\n" + returnTime + "##";
		*/
		JSONObject jobj = new JSONObject();
		jobj.put("flagID", "242");
		jobj.put("flagName", "launchInfo");
		jobj.put("playId", playerName);
		jobj.put("destX", String.valueOf(x));
		jobj.put("destY", String.valueOf(y));
		jobj.put("oreID", String.valueOf(x));
		jobj.put("reachTime", reachTime);
		jobj.put("returnTime", returnTime);
		
		return jobj;
	}
	
	JSONObject mapInfo201(){		//地图信息
		JSONObject jobj = new JSONObject();
		JSONArray jarr = new JSONArray();
		JSONObject ore;

		jobj.put("flagID", "201");
		jobj.put("flagName", "mapInfo");
		jobj.put("oreNum", "140");

		int remain = 30;
		int gridNum = 240;
		for (int i=0;i<140;++i)
		{
			double rand = Math.random();
			int chk = (int)(rand*(gridNum-i));

			if (chk < remain)
			{
				ore = new JSONObject();
				ore.put("orePos", i+1);
				ore.put("oreType", i & 1);
				jarr.add(ore);
				remain--;
			}
		}
		jobj.put("ores", jarr);

		return jobj;
	}
}
