import java.util.ArrayList;

public class RoomManager {
	private static ArrayList<_1v1_Room> rooms;
	public static _1v1_Room create(User u)
	{
		_1v1_Room room = new _1v1_Room(u);
		rooms.add(room);
		return room;
	}
	
	public static ArrayList<_1v1_Room> getRooms()
	{
		return rooms;
	}
}
