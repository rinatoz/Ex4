package gameClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import Server.game_service;
import dataStructure.edge_data;
import dataStructure.node_data;

public class MyManageGame 
{
   private  MyGameGUI my;
   
   public MyManageGame()
   {
	   this.my=null;
   }
   
   public MyManageGame (MyGameGUI m)
   {
	   this.my=m;
   }
   
   public  void update(game_service game,List<myFruits> f,List<Robot> p)
	{
		this.my.setGame(game);
		this.my.setFruits(f);
		this.my.setRobots(p);
	}
   
	public void Auto()
	{
		game_service gameSer=this.my.getGame();
		String info =gameSer.toString();
		JSONObject line;
		int countRobot=0;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			countRobot= ttt.getInt("robots");
		}
		catch (JSONException e) {
			e.printStackTrace();
			}
		int countFruit=gameSer.getFruits().size();//number of fruit in the game
		int index=0;int v;
		while(countFruit>0&&countRobot>0)//put the robot near to the fruit
		{
			if(this.my.getFruits().get(index).whichfruit==false)
				v=this.my.getFruits().get(index).edge(this.my.getG()).getSrc();
			else
				v=this.my.getFruits().get(index).edge(this.my.getG()).getDest();
			this.my.getGame().addRobot(v);
			countFruit--;countRobot--;index++;
		}
		//Create a list of the edge
		int numOfVer=this.my.getG().nodeSize();
		int key[]=new int[numOfVer];
		int j=0;
		for(Iterator<node_data> verIter=this.my.getG().getV().iterator();verIter.hasNext();) 
		{
			int point=verIter.next().getKey();
			key[j]=point;j++;
		}
		while(countRobot>0)	//if there is more robot than fruit put them in a random place
		{
			int random=(int)(Math.random()*key.length);
			this.my.getGame().addRobot(key[random]);
			countRobot--;
		}
		my.addRobots(this.my.getGame());
	}
	
	public  void moveRobotsAuto(KML_logger kml)
	{
		List<Integer> destList = new ArrayList<Integer>();
		List<String> log = this.my.getGame().move();
		if(log!=null) {
			long t = this.my.getGame().timeToEnd();
			for(int i=0;i<log.size();i++) //for each robots find his next destination 
			{
				destList.add(this.my.getRobots().get(i).dest);
			}
			for(int i=0;i<log.size();i++) //for each robots find his next destination 
			{
				try {
					int rid=this.my.getRobots().get(i).id;
					int src=this.my.getRobots().get(i).src;
					int dest=this.my.getRobots().get(i).dest;
					if(dest==-1) 
					{	
						dest = nextNode(src,destList); //find the closet fruit
						destList.add(dest);
						this.my.getGame().chooseNextEdge(rid, dest);
						System.out.println("Turn to node: "+dest+"  time to end:"+(t/1000));
						System.out.println((new JSONObject(log.get(i))).getJSONObject("Robot"));
						String location=new JSONObject(log.get(i)).getJSONObject("Robot").get("pos").toString();
						//System.out.println(location);
						KML_logger.fillPlacemark(kml.fw,location);
					}
				} 
				catch (JSONException e) {e.printStackTrace();}
			}
		}
	}

	private  int nextNode(int src,List<Integer>destList) 
	{
		double minpath=Double.POSITIVE_INFINITY;
		edge_data e=null;
		int Final=0;
		boolean isDest=false;
		for(int i=0;i<this.my.getFruits().size();i++) //find the fruit that is closest to the src
		{
			double dist;List<node_data> node;int dest;
		 	e=this.my.getFruits().get(i).edge(this.my.getG());
		 	if(this.my.getFruits().get(i).whichfruit==true) //if its apple
		 	{
		 		dist=this.my.getGa().shortestPathDist(src, e.getSrc());
		 		node=this.my.getGa().shortestPath(src, e.getSrc());
		 		if(node.size()==1)
		 			dest= e.getDest();
		 		else
		 			dest=node.get(1).getKey();
		 	}
		 	else//if its banana
		 	{
		 		dist=this.my.getGa().shortestPathDist(src, e.getDest());
		 		node=this.my.getGa().shortestPath(src, e.getDest());
		 		if(node.size()==1)
		 			dest= e.getSrc();
		 		else
		 			dest=node.get(1).getKey();
		 	}
			if(dist<minpath&&!destList.contains(dest))//check if its better result and if the dest is not in the list
			{ 
				 	isDest=true;
					minpath=dist;
					Final=dest;
			}
		}
		if(isDest)
			return Final;
		else //if there isnt a fruit available for this robots
		{
			e=this.my.getFruits().get(0).edge(this.my.getG());
			if(src==e.getSrc()) 
			{
				return e.getDest();
			}
			else
				if(src==e.getDest())
					return e.getSrc();
			List<node_data> node=this.my.getGa().shortestPath(src, e.getSrc());
			return (node.get(1).getKey());
		}		
	}
}
