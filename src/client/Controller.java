package client;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class Controller {
    @FXML private ProgressBar progressBar;
    @FXML private Text questionBox;
    @FXML private Button button1;
    @FXML private Button button2;
    @FXML private Button button3;
    @FXML private Button button4;
    @FXML private Button sendChatButton;
    @FXML private TextFlow chatBox;
    @FXML private TextField chatInput;
    @FXML private ChoiceBox<String> choiceBox;
    @FXML private VBox scoreBoard;
    @FXML private Group wait;
    @FXML private AnchorPane root;
    @FXML private AnchorPane startScreen;
    private  Button[] buttons;

    public void initialize(){
        buttons=new Button[]{button1,button2,button3,button4};
        for (int i = 0; i < buttons.length; i++) {
            int n = i+1;
            buttons[i].setOnAction(event ->{
                Program.getClient().sendAnswer(n);
                setEnableButtons(false);
            });
            buttons[i].setEffect(new Bloom());
        }
        progressBar.progressProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.doubleValue()<0.2)
                progressBar.setStyle("-fx-accent: red");
            else
                progressBar.setStyle("-fx-accent: #2889ff");
        });
        addUser("Send to All");

    }

    @FXML
    public void ready(){
        Program.getClient().ready();
        startScreen.setVisible(false);
    }

    public void setEnableButtons(boolean enable){
        for (Button button : buttons) {
            button.setDisable(!enable);
        }
        wait.setVisible(!enable);
    }

    public void setTimer(long time){
        progressBar.setProgress(1);
        Timeline timeline  = new Timeline();
        KeyValue value  = new KeyValue(progressBar.progressProperty(), 0);
        KeyFrame keyFrame  = new KeyFrame(Duration.millis(time), value);
        timeline.getKeyFrames().addAll(keyFrame);
        timeline.play();
    }

    public void setQuestion(String text){
        questionBox.setText(text);
    }

    public void setOptions(String[] options){
        setEnableButtons(true);
        for (int i = 0; i < options.length; i++) {
            buttons[i].setText(options[i]);
        }
    }

    public void addChatMsg(String sender,String msg,boolean isGlobal){
        Text text1=new Text(sender+": ");
        text1.setStyle("-fx-font-weight: bold");
        text1.setWrappingWidth(80);
        if(isGlobal)
            text1.setFill(Color.RED);
        else
            text1.setFill(Color.YELLOW);
        Text text2=new Text(msg+"\n");
        text2.setStyle("-fx-font-weight: normal");
        text2.setFill(Color.WHITE);
        text1.setWrappingWidth(80);

        chatBox.getChildren().addAll(text1, text2);
    }

    public void setScoreBoard(String[][] list) {
        Parent pane=scoreBoard.getParent();
        pane.setVisible(true);
        pane.setOpacity(0);
        sendChatButton.setDisable(false);
        scoreBoard.getChildren().clear();
        for (int i = 0; i < list.length; i++) {
            Image image = new Image(Program.class.getResourceAsStream("/Ranks/"+(i+1)+".png"));
            Group row=new Group();
            ImageView imageView=new ImageView(image);
            imageView.setFitWidth(30);
            imageView.setFitHeight(38);
            row.getChildren().add(imageView);
            imageView.setLayoutX(0);
            Label name=new Label(list[i][0]);
            name.setFont(Font.font("Arial Rounded MT Bold",20));
            name.setTextFill(Color.WHITE);
            row.getChildren().add(name);
            name.setLayoutX(60);
            name.setLayoutY(5);
            Label score=new Label(list[i][1]);
            switch (i) {
                case 0:
                    score.setTextFill(Color.GOLD);
                    break;
                case 1:
                    score.setTextFill(Color.SILVER);
                    break;
                case 2:
                    score.setTextFill(Color.rgb(153,97,39));
                    break;
                default:
                    score.setTextFill(Color.WHITE);
                    break;
            }
            score.setFont(Font.font("Arial Rounded MT Bold",20));
            row.getChildren().add(score);
            score.setLayoutX(150);
            score.setLayoutY(5);
            scoreBoard.getChildren().add(row);
        }
        ProgressIndicator progressIndicator= (ProgressIndicator) pane.lookup("#loading");
        progressIndicator.setProgress(0);
        int breakTime=Program.BREAK_TIME;
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(pane.opacityProperty(), 1)));
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(breakTime),
                        new KeyValue(pane.visibleProperty(), false)));
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(breakTime),
                        new KeyValue(sendChatButton.disableProperty(), true)));
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(breakTime*1000),
                        new KeyValue(progressIndicator.progressProperty(), 1)));
        timeline.play();
    }

    @FXML
    public void sendChat(){
        String id= choiceBox.getValue();
        String msg=chatInput.getText();
        if(!msg.isEmpty()) {
            if (id == null || id.equals("Send to All"))
                Program.getClient().sendGlobalMsg(msg);
            else {
                Program.getClient().sendPrivateMsg(id, msg);
                addChatMsg("[send to: " + id + "]", msg, false);

            }
            chatInput.setText("");
        }
    }

    @FXML
    public void addUser(String name){
        if (!name.equals(Program.getClient().getName()) && !choiceBox.getItems().contains(name)) {
            choiceBox.getItems().add(name);
        }
    }

    public void showFinalScores(){
        ObservableList<Node> list = scoreBoard.getChildren();
        root.setOpacity(0);
        Timeline timeline  = new Timeline();
        KeyValue value  = new KeyValue(root.opacityProperty(),1);
        KeyFrame keyFrame  = new KeyFrame(Duration.seconds(3), value);
        timeline.getKeyFrames().addAll(keyFrame);
        timeline.play();
        root.getChildren().clear();
        VBox vbox=new VBox();
        vbox.setLayoutX(222);
        vbox.setLayoutY(140);
        vbox.setVisible(true);
        vbox.setPrefSize(381,222);
        vbox.getChildren().addAll(list);
        root.getChildren().add(vbox);
    }
}
