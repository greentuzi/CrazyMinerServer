class _1v1_Room
{
	private int roomID;
	private User user1,user2;
	
	_1v1_Room(User u){
		roomID = u.getID();
		u.setRoomID(roomID);
	}
	
	boolean enter(User u)
	{
		if (user1!=null && user2!=null)
		{
			return false;			//·¿¼äÂúÔ±
		}
		if (user1==null)
		{
			user1 = u;
			u.setPosInRoom(0);
			user1.setRoomID(roomID);
		}
		else
		{
			user2 = u;
			u.setRoomID(1);
			user2.setRoomID(roomID);
		}
		return true;
	}
}