package org.htlleo;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.htlleo.client.SocketThread;
import org.htlleo.logic.Share;
import org.htlleo.models.Message;
import org.htlleo.models.MessageDistributor;
import org.htlleo.pattern.Observer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class MainController implements Observer, Initializable {

    @FXML
    public TextArea txtContent;
    @FXML
    public TextField txtPort;
    @FXML
    public Button btnStart;
    @FXML
    public Button btnStop;

    private Share share;
    private ServerSocket serverSocket;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtContent.setDisable(false);
        txtContent.setEditable(false);

        txtPort.setText("3333");
        txtPort.setDisable(false);

        btnStart.setDefaultButton(true);
        btnStart.setDisable(false);

        btnStop.setDisable(true);

        MessageDistributor.getInstance().addObserver(this);
    }

    private int counter = 0;
    @Override
    public void notify(Object sender, Object args) {
        if (args instanceof Message) {
            Platform.runLater(() ->
            {
                if (counter++ % 100 == 0) {
                    txtContent.clear();
                }
                txtContent.appendText(args.toString() + "\n");
            });
        }
        else if (sender instanceof Share) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Share share = (Share)sender;
            Message message = new Message();

            message.setId(UUID.randomUUID());
            message.setCommand("ShareValueChanged");
            message.setFrom(share.getName());
            message.setBody("Time: " + LocalDateTime.now().format(formatter) + " Value: " + share.getValue() + " EUR");
            MessageDistributor.getInstance().addMessage(message);
        }
    }

    public void onStart(ActionEvent actionEvent) {
        if (serverSocket == null) {

            int port = Integer.parseInt(txtPort.getText());

            share = new Share("MSFT", 100.0);
            share.addObserver(this);
            share.start();

            txtContent.clear();
            Thread t = new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        SocketThread socketThread = new SocketThread(socket);

                        MessageDistributor.getInstance().addObserver(socketThread);
                        socketThread.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverSocket = null;
            });

            t.setDaemon(true);
            t.start();

            txtPort.setDisable(true);
            btnStart.setDisable(true);
            btnStop.setDisable(false);
            System.out.println( "Observable Server is running..." );
        }
    }

    public void onStop(ActionEvent actionEvent) {
        if (serverSocket != null) {
            share.stop();
            share.removeObserver(this);
            share = null;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            txtPort.setDisable(false);
            btnStart.setDisable(false);
            btnStop.setDisable(true);
        }
    }
}
