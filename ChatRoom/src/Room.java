abstract class Room {
	int roomID;
	enum roomState{empty,full,playing};
	roomState state;
	int[] users;
	boolean[] ready;
	int playerNum = 0;
	boolean enter(int ID)
	{
		for (int i=0;i<users.length;++i)
		{
			if (users[i]==0)
			{
				users[i] = ID;
				++playerNum;
				if (playerNum==users.length)
				{
					state = roomState.full;
				}
				return true;
			}
		}
		return false;
	}
	
	boolean exit(int ID)
	{
		for (int i=0;i<users.length;++i)
		{
			if (users[i]==ID)
			{
				users[i]=0;
				ready[i]=false;
				--playerNum;
				if (playerNum==0)
				{
					state = roomState.empty;
				}
				return true;
			}
		}
		return false;
	}
	
	boolean ready(int ID)			//若全部ready，则返回true
	{
		boolean playing = false;
		for (int i=0;i<ready.length;++i)
		{
			if (users[i] == ID)
			{
				ready[i]=true;
			}
			playing&=ready[i];
		}
		if (playing)
		{
			state = roomState.playing;
		}
		return playing;
	}
}

class _1v1Room extends Room{
}
