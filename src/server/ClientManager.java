package server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientManager extends Thread {
    private Socket socket;
    private Game game;
    private Scanner input;
    private PrintWriter output;
    private int score;
    public ClientManager(Socket socket) throws IOException {
        this.socket = socket;
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream())), true);
        setName(input.next());
        System.out.println(getName()+" connected");
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void send(String... args){
        StringBuilder str=new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            str.append(args[i]);
            if(i< args.length-1)
                str.append("|");
        }
        output.println(str.toString());
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            if (input.hasNext()) {
                String text = input.nextLine();
                String[] str = text.split("[|]");
                switch (str[0]) {
                    case "ready":
                        game.addReady();
                        game.sendGlobalMessage("Server",getName()+" is ready");
                        break;
                    case "answer":
                        int question = Integer.parseInt(str[1]);
                        int answer = Integer.parseInt(str[2]);
                        game.checkAnswer(getName(), question, answer);
                        break;
                    case "message":
                        if (str.length == 2)
                            game.sendGlobalMessage(getName(), str[1]);
                        else
                            game.sendPrivateMessage(getName(), str[1], str[2]);
                        break;
                }
            }
        }
        game.left(getName());
    }

    public void addScore(){
        score++;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return getName()+":"+score;
    }
}
