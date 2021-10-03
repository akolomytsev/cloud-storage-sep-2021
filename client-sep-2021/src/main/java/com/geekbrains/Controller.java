package com.geekbrains;


import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Controller implements Initializable {

    public ListView<String> clientFilesTableView;
    public ListView<String> serverFilesTableView;
    public TextField clientPath;
    public TextField serverPath;
    private Path currentDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            currentDir = Paths.get("client-sep-2021", "root").normalize().toAbsolutePath();
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            refreshClientView();
            addNavigationListeners();
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        Command msg = (Command) is.readObject();
                         switch (msg.getType()) {
                            case LIST_RESPONSE:
                                ListResponse response = (ListResponse) msg;
                                List<String> names = response.getNames();
                                refreshServerView(names); // обновляем серверный лист
                                break;
                            case PATH_RESPONSE:
                                PathResponse pathResponse = (PathResponse) msg;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                                break;
                            case FILE_MESSAGE:
                                FileMessage message = (FileMessage) msg;
                                Files.write(currentDir.resolve(message.getName()), message.getBytes()); // на клиенте пишем новый файл
                                refreshClientView(); // и обновляем лист
                                break;
                        }
                    }
                } catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
            log.error("e=", ioException);
        }
    }

    private void refreshClientView() throws IOException {
        clientPath.setText(currentDir.normalize().toAbsolutePath().toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            clientFilesTableView.getItems().clear();
            clientFilesTableView.getItems().addAll(names);
        });

    }

    private void refreshServerView(List<String> names) {
        Platform.runLater(() -> {
            serverFilesTableView.getItems().clear();
            serverFilesTableView.getItems().addAll(names);
        });
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientFilesTableView.getSelectionModel().getSelectedItem();
        FileMessage message = new FileMessage(currentDir.resolve(fileName));
        os.writeObject(message);
        os.flush();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverFilesTableView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
        os.flush();
    }

    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        if (currentDir.getParent() != null) {
            currentDir = currentDir.getParent();
            clientPath.setText(currentDir.toString());
        }
        refreshClientView();
    }

    public void serverPathUp(ActionEvent actionEvent) throws IOException {
        os.writeObject(new PathUpRequest());
        os.flush();
    }

    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }


    private void addNavigationListeners(){
        clientFilesTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() ==2) {
                String item = clientFilesTableView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)){
                    currentDir = newPath;
                    try {
                        refreshClientView();
                    }catch (IOException ioException){
                        ioException.printStackTrace();
                    }
                }
            }
        });

        serverFilesTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverFilesTableView.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new PathInRequest(item));
                    os.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }


}
