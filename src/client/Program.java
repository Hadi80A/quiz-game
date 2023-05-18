package client;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Program extends Application {
    private static Controller controller;
    private static Client client;
    public static final int ANSWER_TIME=15;
    public static final int BREAK_TIME=30;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/program.fxml"));
        Parent root = loader.load();
        controller=loader.getController();
        primaryStage.setTitle(client.getName());
        Scene scene=new Scene(root, 600, 500);
        String style = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().addAll(style);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static Controller getController() {
        return controller;
    }

    public static Client getClient() {
        return client;
    }

    public static int getNumber(){
        int n = -1;
        try {
            String cmd = "jps -l | findstr /R /C:\"client.Program\" | find /c /v \"\"";
            String[] arg = {
                    "cmd",
                    "/C",
                    cmd
            };
            Process proc = Runtime.getRuntime().exec(arg);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            n= Integer.parseInt(stdInput.readLine());
        }catch (IOException e) {
            e.printStackTrace();
        }
        return n;
    }

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray jsonArray=(JSONArray) jsonParser.parse(new FileReader("json/users.json"));
            int hostPort=  Integer.parseInt(((JSONObject) jsonArray.get(0)).get("port").toString());
            JSONObject user = (JSONObject) jsonArray.get(getNumber());
            String name= (String) user.get("name");
            int localPort=  Integer.parseInt((user.get("port").toString()));
            client=new Client(name,hostPort,localPort);
            Thread thread = new Thread(client);
            thread.start();
            launch(args);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
