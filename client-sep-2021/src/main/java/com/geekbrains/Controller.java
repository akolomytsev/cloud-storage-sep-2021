package com.geekbrains;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Controller implements Initializable {
    private Path currentDir = Paths.get(".").normalize().toAbsolutePath();

    public ListView<String> fileClientView;
    public ListView<String> fileServerView;
    public TextField input;
    public TextField currentDirectoryOnClient;
    public TextField currentDirectoryOnServer;

    public AnchorPane mainScene;
    public ComboBox disksBoxClient;

    public TextField loginField;
    public TextField passwordField;
    public Button Authorization;

    private Net net;

    private void addViewListener(ListView<String> lv, TextField ta) {
        lv.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    try {
                        ta.clear();
                        ta.appendText(lv.getSelectionModel().getSelectedItem());
                    } catch (NullPointerException ignored) {
                    }
                });
    }

    public void sendLoginAndPassword(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        loginField.clear();
        passwordField.clear();
        net.sendCommand(new AuthRequest(login, password));
    }

    public void sendFile(ActionEvent actionEvent) throws IOException {
        String fileName = input.getText().split(" ")[1];
        input.clear();
        Path file = currentDir.resolve(fileName);
        net.sendCommand(new FileMessage(file.toFile()));
    }

    public void receiveArrayFiles(ActionEvent actionEvent) {
        net.sendCommand(new ListRequest());
    }

    public void updateArrayFiles(ActionEvent actionEvent) throws IOException {
        refreshClientView();
    }

    public void receiveFile(ActionEvent actionEvent) {
        String fileName = input.getText();
        input.clear();
        Path file = Paths.get(fileName);
        net.sendCommand(new FileRequest(file));
    }


    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        currentDir = currentDir.getParent();
        currentDirectoryOnClient.setText(currentDir.toString());
        refreshClientView();
    }

    public void serverPathUp(ActionEvent actionEvent) {
        net.sendCommand(new PathUpRequest());
    }


    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addViewListener(fileClientView, input);
        disksBoxClient.getItems().clear(); //  очищаем вкладку disksBox при старте
        for (Path p : FileSystems.getDefault().getRootDirectories()) { // заполняем через стандартный метод
            // FileSystems который предоставляет инф-у о файловой системе мы берем систему по умолчанию (Default) и запрашиваем список корневых директорий
            disksBoxClient.getItems().add(p.toString()); // и добавляем в выпадающий список все диски
        }
        disksBoxClient.getSelectionModel().select(0); // выбираем по умолчанию первый из них
        try {
            //currentDir = Paths.get("client-sep-2021", "root").normalize().toAbsolutePath();
            currentDirectoryOnClient.setText(currentDir.toString());
            refreshClientView();
            addNavigationListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
        net = Net.getInstance(cmd -> {

            switch (cmd.getType()) {
                case LIST_RESPONSE:
                    ListResponse listResponse = (ListResponse) cmd;
                    Platform.runLater(() -> refreshServerView(listResponse.getList())); // обновляем серверный лист
                    break;
                case PATH_RESPONSE:
                    PathResponse pathResponse = (PathResponse) cmd;
                    currentDirectoryOnServer.setText(pathResponse.getPath());
                    break;
                case FILE_MESSAGE:
                    FileMessage fileMessage = (FileMessage) cmd;
//                    Files.write(
//                            //currentDir.resolve(fileMessage.getName()),
//                            //fileMessage.getBytes()
//                    );
                    Platform.runLater(() -> {
                        try {
                            refreshClientView();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    break;
                case AUTH_RESPONSE:
                    AuthResponse authResponse = (AuthResponse) cmd;
                    log.debug("AuthResponse {}", authResponse.getAuthStatus());
                    if (authResponse.getAuthStatus()) {
                        //mainScene.setVisible(true);
                        loginField.setVisible(false);
                        passwordField.setVisible(false);
                        Authorization.setVisible(false);
                        net.sendCommand(new ListRequest());
                    } else if (!authResponse.getAuthStatus()) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING, "Неверный логин или пароль",
                                    ButtonType.OK);
                            alert.showAndWait();
                        });
                    }
            }

        });

    }

    public String resolveFileType(Path path) {
        if (Files.isDirectory(path)) {
            return "[Dir]" + " " + path.getFileName().toString();
        } else {
            return "[File]" + " " + path.getFileName().toString();
        }
    }

    public String returnName2(String str) {
        String[] words = str.split(" ");
        String returnWay;
        switch (words.length) {
            case 2:
                returnWay = words[1];
                break;
            case 3:
                returnWay = words[1] + " " + words[2];
                break;
            case 4:
                returnWay = words[1] + " " + words[2] + " " + words[3];
                break;
            default:
                returnWay = words[1];
                break;
        }
        return returnWay;
    }

    public String returnName1(String str) {
        String[] words = str.split(" ");
        return words[0];
    }

    private void refreshClientView() throws IOException {
        fileClientView.getItems().clear();
        List<String> names = Files.list(currentDir)
                .map(this::resolveFileType)
                .collect(Collectors.toList());
        fileClientView.getItems().addAll(names);
    }


    private void refreshServerView(List<String> names) {
        fileServerView.getItems().clear();
        fileServerView.getItems().addAll(names);
    }


//Удалить с сервера
//    public void deleteServer(ActionEvent actionEvent) throws IOException {
//        String fileName = fileServerView.getSelectionModel().getSelectedItem();
//        os.writeObject(new Delete(fileName));
//        os.flush();
//    }
//// удалить с клиента
//    public void deleteClient(ActionEvent actionEvent) throws IOException {
//       String fileName = fileClientView.getSelectionModel().getSelectedItem();
//        File file = new File(String.valueOf(currentDir.resolve(fileName)));
//        Delete.deleteFile(file);
//        refreshClientView();
//    }

    // Выход по нажатию кнопки
    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    //Переход в папку по двойному щелчку
    private void addNavigationListener() {
        fileClientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = returnName2(fileClientView.getSelectionModel().getSelectedItem());
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try {
                        refreshClientView();
                        currentDirectoryOnClient.setText(currentDir.toString());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
               }else {
                    input.setText(item);
                }
            }
        });

        fileServerView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = returnName2(fileServerView.getSelectionModel().getSelectedItem());
                if (returnName1(fileServerView.getSelectionModel().getSelectedItem()).equals("[Dir]")) {
                    net.sendCommand(new PathInRequest(item));
                } else {
                    input.setText(item);
                }
            }
        });
    }

    // выбор диска на клиенте
    public void diskSelection(ActionEvent actionEvent) throws IOException {// выбор диска на комбо боксе
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource(); // через действие (actionEvent) получаем информацию на какой диск нажал пользователь
        Path newPath = Paths.get(element.getSelectionModel().getSelectedItem());
        currentDir = newPath;
        refreshClientView();
        try {
            refreshClientView();// и проверяем а что там выбрали
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


}
