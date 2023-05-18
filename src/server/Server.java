package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private static ServerSocket serverSocket;
    public static final int MIN_CLIENTS=3;
    private static int clientCount =0;
    private static int maxClients;
    private static Game game;
    public static int port;

    public static void main(String[] args) {
        try {
            readJson();
            serverSocket=new ServerSocket(port);
            System.out.println("server Started!");
            game=new Game();
            while (clientCount<maxClients){
                try {
                    Socket socket= serverSocket.accept();
                    ClientManager client=new ClientManager(socket);
                    client.start();
                    clientCount++;
                    game.addClient(client);
                    client.setGame(game);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    public static void readJson() throws IOException, ParseException {
        JSONArray jsonArray=(JSONArray) new JSONParser().parse(new FileReader("json/users.json"));
        port =  Integer.parseInt(((JSONObject) jsonArray.get(0)).get("port").toString());
        maxClients=jsonArray.size();
    }

}
