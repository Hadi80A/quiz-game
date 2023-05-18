package client;

import javafx.application.Platform;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{
    private String name;
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    private int currentQuestion=-1;

    public Client(String name,int hostPort,int localPort){
        try {
            this.socket =new Socket("127.0.0.1", hostPort,null,localPort);
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
            this.name=name;
            output.println(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            if (input.hasNext()) {
                String text = input.nextLine();
                process(text);
            }
        }
    }

    public void process(String text) {
        String[] str = text.split("[|]");
        switch (str[0]) {
            case "question":
                String question = str[1];
                String[] options = str[2].replace("\"","").split(",");
                long time = Long.parseLong(str[3]);
                showQuestion(question, options, time);
                currentQuestion++;
                break;
            case "scoreboard":
                String[] list=str[1].substring(1,str[1].length()-1).split(", ");
                showScoreBoard(list);
                break;
            case "msg":
                showPrivateMsg(str[1],str[2]);
                break;
            case "GlobalMsg":
                showGlobalMsg(str[1],str[2]);
                break;
            case "End":
                endGame();
                break;
        }
    }

    public void ready(){
        output.println("ready");
    }

    public void showQuestion(String question,String[] options,long time){

        System.out.println("question "+ currentQuestion +" :");
        System.out.println(question);
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            System.out.println((i+1)+" - "+option);
        }
        int answerTime=Program.ANSWER_TIME;
        long remainingTime=answerTime*1000-(System.currentTimeMillis()-time);

        Platform.runLater(() -> {
            Controller controller = Program.getController();
            controller.setQuestion(question);
            controller.setOptions(options);
            controller.setTimer(remainingTime);
        });

    }

    public void showScoreBoard(String[] list){
        System.out.println(".:: Score Board ::.");
        System.out.println("Rank | Name | Score");
        String[][] scores=new String[list.length][2];
        for (int i = 0; i < list.length; i++) {
            String[] str = list[i].split(":");
            scores[i][0] = str[0]; //name
            scores[i][1] = str[1]; //score
            System.out.format("%d - %s  %s\n",i+1,scores[i][0],scores[i][1]);
            Platform.runLater(() -> Program.getController().addUser(str[0]));
        }
        Controller controller=Program.getController();
        Platform.runLater(() -> controller.setScoreBoard(scores));
    }


    public void sendAnswer(int answer){
        output.println("answer|"+currentQuestion+"|"+answer);
    }

    public void sendPrivateMsg(String id , String msg){
        output.println("message|"+id+"|"+msg);
    }

    public void sendGlobalMsg(String msg){
        output.println("message|"+msg);
    }

    public void showPrivateMsg(String id , String msg){
        System.out.println(id+":");
        System.out.println(msg);
        Platform.runLater(() -> Program.getController().addChatMsg(id, msg,false));
    }

    public void showGlobalMsg(String id , String msg){
        System.out.println(id+"(global):");
        System.out.println(msg);
        if(Program.getController()==null)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        Platform.runLater(() -> Program.getController().addChatMsg(id, msg,true));
    }

    public void endGame(){
        Platform.runLater(() -> Program.getController().showFinalScores());

    }

    public String getName() {
        return name;
    }
}
