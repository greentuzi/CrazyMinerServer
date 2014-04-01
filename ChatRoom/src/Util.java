import java.util.ArrayList;
import java.util.Random;

//import test.HelloWorld.Ores;
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
	
	public class Ores{
		double P1maxAngle,P1minAngle,P2maxAngle,P2minAngle;
		int oreType,orePos;
	}
	
	private Ores ores[];
	int maxRow = 8;
	int maxCol = 22;
	int oreNum = 30;
	
	JSONObject msgParse(String msg,Server.Client_Thread client){
		//JSONObject jobj = new JSONObject();
		JSONObject rec = JSONObject.fromObject(msg);
		int flagID =  Integer.parseInt(rec.getString("flagID"));
		switch (flagID){
		case 241:
			double angle = Double.parseDouble(rec.getString("angle"));
			JSONObject jobj = shout242(angle,client);
			return jobj;
		}
		return null;
	}
	
	//发射矿钩信息
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
		

		/*
		System.out.println(client.id);
		
		String desPoint = String.valueOf(x)+"\n"+String.valueOf(y); //终点坐标

		String msg = "242##launchInfo##" + playerName + "\n" + desPoint + "\n" + reachTime + "\n" + returnTime + "##";
		*/
		boolean willCatch = false;
		double length = 0;
		int oreID = 0;
		System.out.println(angle);
		//for(int i = 0;i<oreNum;i++)
			//System.out.println(ores[i].P1minAngle + " " + ores[i].P1maxAngle + " " );
		//判断是否抓到
		for(int i = 0;i<oreNum;i++){
			if(angle >= ores[i].P1minAngle && angle <= ores[i].P1maxAngle && (client.id & 1) == 0){
				willCatch = true;
				//System.out.println(ores[i].P1minAngle + " " + ores[i].P1maxAngle + " " );
				int col = ores[i].orePos % maxCol;
				int row = ores[i].orePos / maxCol + 1;
				float r_x = col * 60 - 30 + 23; //23为两则留空
				float r_y = row * 60 - 30 + 144 + 100 + 44;//144:玩家高度,100:绳子初长,44:留空
				double af;
				System.out.println("r_x:" + r_x + " r_y:" + r_y);
				
				if(angle >= 3.14/2)
					af = Math.atan((r_y - 144)/(341.5 - r_x)) + angle - 3.14;
				else af =angle - Math.atan((r_y - 144)/(r_x - 341.5)) ;
				
				System.out.println("af:" + af);
				int r = ( ores[i].oreType % 3 + 3 ) * 5; 
				double s = Math.sqrt((r_x - 341.5) * (r_x - 341.5) + (r_y - 144) * (r_y -144));
				System.out.println("s:"+ s + "sin(af):" + Math.sin(af) );
				System.out.println("test:" + (s*s*Math.sin(af)*Math.sin(af)));
				System.out.println("id:" +  ores[i].orePos);
				length = s*Math.cos(af) - Math.sqrt(r*r - s*s*Math.sin(af)*Math.sin(af));
				oreID = ores[i].orePos;
				break;
			}
			else if(angle >= ores[i].P2minAngle && angle <= ores[i].P2maxAngle && (client.id & 1) == 1)
			{
				willCatch = true;
				int col = (i+1) % maxCol;
				int row = (i+1) / maxCol + 1;
				float r_x = col * 60 - 30 + 23; //23为两则留空
				float r_y = row * 60 - 30 + 144 + 100 + 44;//144:玩家高度,100:绳子初长,44:留空
				double af;
				if(angle >= 3.14/2)
					af = Math.atan((r_y - 144)/(1024.5 - r_x)) + angle - 3.14;
				else af =angle - Math.atan((r_y - 144)/(r_x - 1024.5)) ;
				int r = ( ores[i].oreType % 3 + 3 ) * 5; 
				double s = Math.sqrt((r_x - 1024.5) * (r_x - 1024.5) + (r_y - 144) * (r_y -144));
				length = s*Math.cos(af) - Math.sqrt(r*r - s*s*Math.sin(af)*Math.sin(af));
				oreID =  ores[i].orePos;
				break;
			}
		}
		
		double x;
		double y;

		if(willCatch){
			x = Math.cos(angle)*length+originX;
			y = Math.sin(angle)*length+originY;
		}
		else{
			x = Math.cos(angle)*300+originX;
			y = Math.sin(angle)*300+originY;
		}

		System.out.println("l:" + length );	
		
		JSONObject jobj = new JSONObject();
		jobj.put("flagID", "242");
		jobj.put("flagName", "launchInfo");
		jobj.put("playID", playerName);
		jobj.put("destX", String.valueOf(x));
		jobj.put("destY", String.valueOf(y));
		jobj.put("oreID", String.valueOf(oreID));
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
		jobj.put("oreNum", "30");

		int remain;

		int gridNum = 176;
		//Ores[] ores;
		ores = new Ores[30];
		remain = oreNum;
		
		for (int i=0;i<gridNum;++i)
		{
			double rand = Math.random();
			int chk = (int)(rand*(gridNum-i));

			if (chk < remain)
			{
				Random ran2 = new Random();
				int orePos = i+1;
				int oreType = Math.abs(ran2.nextInt() % 6);
				//System.out.println(oreType);
				ore = new JSONObject();
				ore.put("orePos", orePos);
				ore.put("oreType", oreType);
				jarr.add(ore);
				
				//半径:15,20或25
				int r = ( oreType % 3 + 3 ) * 5;
				
 				//计算矿石圆心坐标
				int col = orePos % maxCol;
				int row = orePos / maxCol + 1;
				//圆心坐标
				float x = col * 60 - 30 + 23; //23为两则留空
				float y = row * 60 - 30 + 144 + 100 + 44;//144:玩家高度,100:绳子初长,44:留空
				//玩家焦点到圆心距离
				double d1 = Math.sqrt((x - 341.5) * (x - 341.5) + (y - 144) * (y -144));
				double d2 = Math.sqrt((x - 1024.5) * (x - 1024.5) + (y - 144) * (y -144));
				double a1;
				double a2;
				if(x < 341.5){
					a1 = 3.14 - Math.atan((y - 144)/(341.5 - x));
					a2 = 3.14 - Math.atan((y - 144)/(1024.5 - x));
				}
				else if(x < 1024.5){
					a1 = Math.atan((y - 144)/(x - 341.5));
					a2 = 3.14 - Math.atan((y - 144)/(1024.5 - x));					
				}
				else{
					a1 = Math.atan((y - 144)/(x - 341.5));
					a2 = Math.atan((y - 144)/(x - 1024.5));						
				}
					
				double o1 = Math.asin(r/d1);
				double o2 = Math.asin(r/d2);
				ores[oreNum - remain] = new Ores();
				ores[oreNum - remain].P1minAngle = a1 - o1;
				ores[oreNum - remain].P1maxAngle = a1 + o1;
				ores[oreNum - remain].P2minAngle = a2 - o2;
				ores[oreNum - remain].P2maxAngle = a2 + o2;	
				ores[oreNum - remain].oreType = oreType;	
				ores[oreNum - remain].orePos = orePos;	
				//ores[oreNum - remain].P1minAngle = ores[oreNum - remain].P1minAngle * 180 / 3.14;
				//ores[oreNum - remain].P1maxAngle = ores[oreNum - remain].P1maxAngle * 180 / 3.14;
				//ores[oreNum - remain].P2minAngle = ores[oreNum - remain].P2minAngle * 180 / 3.14;
				//ores[oreNum - remain].P2maxAngle = ores[oreNum - remain].P2maxAngle * 180 / 3.14;
				System.out.println("i:"+orePos+" "+ores[oreNum - remain].P1minAngle + " " 
								 + ores[oreNum - remain].P1maxAngle + " "
							 + ores[oreNum - remain].P2minAngle + " "
							 + ores[oreNum - remain].P2maxAngle);
				remain--;
				
				
			}
		}
		jobj.put("ores", jarr);
		System.out.println(jobj.toString());
		return jobj;
	}
}
