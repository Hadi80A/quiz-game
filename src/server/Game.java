package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Game extends Thread{
    private final int ANSWER_TIME=15;
    private final int BREAK_TIME =30;
    private final ArrayList<ClientManager> clientsList=new ArrayList<>();
    private int membersCount;
    private int currentQuestion=0;
    private long sendTime;
    private JSONArray questions;


    public void addReady(){
        membersCount++;
        if(membersCount ==Server.MIN_CLIENTS)
            start();
    }

    @Override
    public void run(){
        initialGame();
        sendGlobalMessage("Server","game started");
        while (currentQuestion< questions.size()){
            sendQuestion();
            try {
                sleep(ANSWER_TIME*1000);
                clientsList.sort(Collections.reverseOrder(Comparator.comparingInt(ClientManager::getScore)));
                sendScoreBoard();
                sleep(BREAK_TIME *1000);
                currentQuestion++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sendToAll("End");
        System.out.println("End");
    }


    public void initialGame(){
        JSONParser jsonParser = new JSONParser();
        try {
            questions = (JSONArray) jsonParser.parse(new FileReader("json/questions.json"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }


    public void addClient(ClientManager client) {
        clientsList.add(client);
        sendGlobalMessage("Server", client.getName() + " joined to game");
    }

    public void sendToAll(String... data){
        System.out.println("SEND:"+ Arrays.toString(data));
        for (ClientManager client : clientsList) {
            client.send(data);
        }
    }

    public void sendPrivateMessage(String from,String to,String data){
        System.err.println("Message from "+from+" to "+to+":"+data);
        ClientManager client=getClient(to);
        client.send("msg",from,data);
    }


    public void sendGlobalMessage(String senderId, String data){
        sendToAll("GlobalMsg",senderId,data);
    }

    public void checkAnswer(String clientName, int question, int ans){
        JSONObject q = (JSONObject) questions.get(currentQuestion);
        int answer=Integer.parseInt(q.get("answer").toString());
        if(currentQuestion==question && ans==answer && System.currentTimeMillis()<sendTime+ANSWER_TIME*1000){
            getClient(clientName).addScore();
        }
    }

    public ClientManager getClient(String id){
        for (ClientManager client : clientsList) {
            if(client.getName().equals(id)){
                return client;
            }
        }
        return null;
    }

    public void left(String id){
        ClientManager client = getClient(id);
        clientsList.remove(client);
        if(membersCount>1)
            sendGlobalMessage("Server",  id+" left the game");
        membersCount--;
        System.out.println("client " + id+" disconnected");
    }

    public void sendQuestion(){
        JSONObject obj= (JSONObject) questions.get(currentQuestion);
        String question= (String) obj.get("question");
        String options=obj.get("options").toString();
        options=options.substring(1,options.length()-1);
        sendTime= System.currentTimeMillis();
        sendToAll("question",question,options, String.valueOf(sendTime));
    }

    public void sendScoreBoard(){
        sendToAll("scoreboard",Arrays.toString(clientsList.toArray()));
    }


}
