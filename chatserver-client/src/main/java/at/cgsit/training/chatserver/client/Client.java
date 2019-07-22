package at.cgsit.training.chatserver.client;

import at.cgsit.training.chatserver.common.ChatServerCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client{
	Socket socket;
	DataOutputStream outputStream;
	DataInputStream inputStream;
	String hostname;
	ClientThread thread;
	
	public static void main(String[] args){
		//init variables
		Client client = new Client();
		Scanner scanner = new Scanner(System.in);
		System.out.println("IP: ");
		String ip = scanner.nextLine();
		System.out.println("Port: ");
		short port = Short.parseShort(scanner.nextLine());
		try{
			client.socket = new Socket(ip, port);
			client.outputStream = new DataOutputStream(client.socket.getOutputStream());
			client.inputStream = new DataInputStream(client.socket.getInputStream());
		} catch (UnknownHostException e){
			System.err.println(e);
			System.err.println("unknown host");
		} catch (IOException e) {
			e.printStackTrace();
		}
		client.Startup(scanner);
		String input = "";
		boolean running = true;
		while(running){
			input = scanner.nextLine();
			if(!input.equals("/dis")){
				client.SendMessage(input);
			} else{
				running = false;
			} 
		}
		client.SendCommand(ChatServerCommand.CLOSECONNECTION);
		
		scanner.close();
	}
	
	public void Startup(Scanner scan){
		//Startup
		try {
			boolean accepted = false;
			String input = "";
			while(!accepted){
				if(WaitForCommand(ChatServerCommand.SENDHOSTNAME)){
					do{
						System.out.println("Please enter your name: ");
						hostname = scan.nextLine();
						if(hostname == ""){
							System.out.println("Invalide name");
						}
					} while(hostname == "");
					SendCommand(hostname);
					input = inputStream.readUTF();
					if(IsCommand(input)){
						input = GetString(input);
						if(input.equals(ChatServerCommand.ACCEPT.name())){
							accepted = true;
							System.out.println("Name accepted");
						} else {
							System.out.println("Name not accepted");
						}
					}
				}	
			}
			if(WaitForCommand(ChatServerCommand.REQUESTACCEPT)){
				SendCommand(ChatServerCommand.ACCEPT);
			}
			if(WaitForCommand(ChatServerCommand.CONNECTIONESTABLISHED)){
				System.out.println("Connection Established");
			}
		} catch (Exception e) { }
		thread = new ClientThread(this);
		thread.start();
	}
	
	public void SendCommand(ChatServerCommand command){
		try {
			outputStream.writeUTF("/" + command.name());
			outputStream.flush();
		} catch (IOException e) { e.printStackTrace(); }
	}

	public void SendCommand(String command){
		try {
			outputStream.writeUTF("/" + command);
			outputStream.flush();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public void SendMessage(String message){
		try {
			outputStream.writeUTF("." + message);
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
	
	public void DrawUI(){
		
	}
}
