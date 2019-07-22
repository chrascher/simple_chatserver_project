package at.cgsit.training.chatserver;

import at.cgsit.training.chatserver.common.ChatServerCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread {
	Socket socket;
	Server server;
	String clientname;
	DataInputStream inputStream = null;
	DataOutputStream outputStream = null;
	
	public ServerThread(Socket iSocket, Server IServer) {
		socket = iSocket;
		server = IServer;
	}
	
	public void Startup(){
		//Startup
		try {
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e1) { e1.printStackTrace(); };
		
		//excaningData
		try{
			String input = " ";
			do{
				SendCommand(ChatServerCommand.SENDHOSTNAME);
				input = inputStream.readUTF();
				if(IsCommand(input)){
					input = GetString(input);
					if(!server.HostnameUsed(input)){
						clientname = input;
						server.hostnames.add(clientname.toUpperCase());
						SendCommand(ChatServerCommand.ACCEPT);
					}
					else{
						SendCommand(ChatServerCommand.DENY);
					}
				}
			} while(!input.equals(clientname));
			boolean waiting = true;
			while(waiting){
				waiting = false;
				SendCommand(ChatServerCommand.REQUESTACCEPT);
				if(!WaitForCommand(ChatServerCommand.ACCEPT)){
					waiting = true;
					SendCommand(ChatServerCommand.INVALIDRESPONS);
				}
			}
			SendCommand(ChatServerCommand.CONNECTIONESTABLISHED);
			server.SendToAllClients(clientname + " has joined!", null);
		} catch(IOException e){ e.printStackTrace(); }
	}
	
	public void run() {
		Startup();
		
		//running
		boolean running = true;
		String input;
		try {
			do{
				input = inputStream.readUTF();
				if(IsCommand(input)){
					if(GetString(input).equals(ChatServerCommand.CLOSECONNECTION.name())){
						running = false;
					}
				}
				else{
					server.SendToAllClients(clientname + ": " + GetString(input), this);
				}
			} while(running);
		} catch (IOException e) { e.printStackTrace(); }
		try {
			server.SendToAllClients(clientname + " has disconnected", null);
			inputStream.close();
			outputStream.close();
			socket.close();
		} catch (IOException e) { e.printStackTrace(); }
		server.RemoveThread(this);
	}
	
	public void SendCommand(ChatServerCommand command){
		try {
			outputStream.writeUTF("/" + command.name());
			outputStream.flush();
		} catch (IOException e) { e.printStackTrace(); }
	}

	public static String GetString(String string){
		return string.substring(1);
	}
	
	public static boolean IsCommand(String string){
		if(string.charAt(0) == '/'){
			return true;
		}
		return false;
	}
	
	public boolean WaitForCommand(ChatServerCommand command){
		String recived = "";
		try {
			recived = inputStream.readUTF();
			if(GetString(recived).equals(command.name())){
				return true;
			}
		} catch (IOException e) { e.printStackTrace(); }
		return false;
	}
}
