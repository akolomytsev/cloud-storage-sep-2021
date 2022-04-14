package com.geekbrains;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.FileUtils;


@Slf4j
public class Controller implements Initializable {
    private Path currentDir = Paths.get("client-sep-2021", "root").normalize().toAbsolutePath();

   // public ListView<String> fileClientView;
    public TableView<FileInfo> fileClientView;
    //public ListView<String> fileServerView;
    public TableView<FileInfo> fileServerView;
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
        String fileName = input.getText();
       //String fileName = input.getText().split(" ")[1];
        input.clear();
        Path file = currentDir.resolve(fileName);
        net.sendCommand(new FileMessage(file));
        //net.sendCommand(new FileMessage(file.toFile()));
    }


    // удалить с клиента
    public void deleteClient(ActionEvent actionEvent) throws IOException {
        String fileName = input.getText();
        //String fileName = fileClientView.getSelectionModel().getSelectedItem();
        File file = new File(String.valueOf(currentDir.resolve(fileName)));
        Delete.deleteFile(file);
        refreshClientView(Paths.get(fileName));
    }

    public void receiveArrayFiles(ActionEvent actionEvent) {
        net.sendCommand(new ListRequest());
    }

    public void updateArrayFiles(ActionEvent actionEvent) throws IOException {
        refreshClientView(currentDir);
    }

    public void receiveFile(ActionEvent actionEvent) {
        String fileName = input.getText();
        input.clear();
        Path file = Paths.get(fileName);
        net.sendCommand(new FileRequest(file));
    }


    //    Удалить с сервера
    public void deleteServer(ActionEvent actionEvent) throws IOException {
        String fileName = input.getText();
        //String fileName = fileServerView.getSelectionModel().getSelectedItem();
        //os.writeObject(new Delete(fileName));
        input.clear();
        Path file = Paths.get(fileName);
        net.sendCommand(new Delete(fileName));
    }


    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        currentDir = currentDir.getParent();
        currentDirectoryOnClient.setText(currentDir.toString());
        refreshClientView(currentDir);
    }

    public void serverPathUp(ActionEvent actionEvent) {
        net.sendCommand(new PathUpRequest());
    }


    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>(); // создаем первый столбец и описываем что в ней хранится и как выглядит,
        // а именно у нас есть данные из FileInfo  и мы его хотим преобразовать в String
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName())); // param это одна запись в таблице
        // файл инфо получили, у него запросили тип у типа запрашиваем имя (это буква D или F)
        fileTypeColumn.setPrefWidth(24); // размер нашего столбца в пикселях

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name"); // второй столбец, имя файла
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName())); // у FileInfo запрашиваем имя файла или директории
        fileNameColumn.setPrefWidth(240); // размер столбца в пикселях

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size"); // третий столбец это размер
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));// у FileInfo запрашиваем размер файла

        fileSizeColumn.setCellFactory(column ->{
            return new TableCell<FileInfo, Long>(){ // возвращаем информацию полученную у FileInfo в виде Long
                @Override
                protected void updateItem(Long item, boolean empty) { // значение ячейки и пустая она или нет
                    super.updateItem(item, empty);
                    if (item == null || empty){// если Long не заполнен или ячейка является пустой
                        setText(null); // ничего не пишем
                        setStyle("");//и ничего не отображаем
                    } else { // если же нет то
                        String text = String.format("%,d bytes", item); // форматируем текст, %,d - это разделение пробелом определенного количества символов и далее единицы измерения
                        // и отдаем item
                        if (item == - 1L){ // если размер -1L
                            text = "[DIR]";// то выводим DIR

                        }
                        setText(text); // и выводим в ячейку
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120); // размер столбца в пикселях


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // преобразуем выводимый формат даты к требуемому
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("date of last changes"); // Создаем столбец с датой последнего изменения
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf))); // у FileInfo запрашиваем дату
        // и преобразуем через наш форматер
        //
        fileDateColumn.setPrefWidth(120); // размер столбца в пикселях
//
        fileClientView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn); // добавляем все наши столбцы в таблицу
        fileClientView.getSortOrder().add(fileTypeColumn); // сортируем по типу данных

//        fileServerView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn); // добавляем все наши столбцы в таблицу
//        fileServerView.getSortOrder().add(fileTypeColumn); // сортируем по типу данных

        //addViewListener(fileClientView, input);
        disksBoxClient.getItems().clear(); //  очищаем вкладку disksBox при старте
        for (Path p : FileSystems.getDefault().getRootDirectories()) { // заполняем через стандартный метод
            // FileSystems который предоставляет инф-у о файловой системе мы берем систему по умолчанию (Default) и запрашиваем список корневых директорий
            disksBoxClient.getItems().add(p.toString()); // и добавляем в выпадающий список все диски
        }
        disksBoxClient.getSelectionModel().select(0); // выбираем по умолчанию первый из них
        try {
            //currentDir = Paths.get("client-sep-2021", "root").normalize().toAbsolutePath();
            currentDirectoryOnClient.setText(currentDir.toString());
            refreshClientView(currentDir);

            addNavigationListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
        net = Net.getInstance(cmd -> {

            switch (cmd.getType()) {
                case LIST_RESPONSE:
                    ListResponse files = (ListResponse) cmd;
                    Platform.runLater(() -> {
//                        listView.getItems().addAll(files.getFileInfos().toString());
                        //refreshServerView(files);
                    });
//                    Platform.runLater(() -> refreshServerView(listResponse.getList())); // обновляем серверный лист
                    break;
                case PATH_RESPONSE:
                    PathResponse pathResponse = (PathResponse) cmd;
                    //currentDirectoryOnServer(pathResponse.getPath());
                    currentDirectoryOnServer.setText(pathResponse.getPath());
                    break;
                case FILE_MESSAGE:
                    FileMessage fileMessage = (FileMessage) cmd;
                    Files.write(
                            currentDir.resolve(fileMessage.getName()),
                            fileMessage.getBytes()
                    );
//                    Files.write(
//                            currentDir.resolve(fileMessage.getName()),
//                            fileMessage.getBytes()
//                    );
                   // FileUtils.copyFile(fileMessage.getFile(), new File(fileMessage.getName()));
                    Platform.runLater(() -> {
                        try {
                            refreshClientView(currentDir);
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

                        fileServerView.getColumns().clear();
                        //refreshServerView(new ListResponse());

 //                       fileServerView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn); // добавляем все наши столбцы в таблицу
//                        fileServerView.getSortOrder().add(fileTypeColumn); // сортируем по типу данных
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

//    public String resolveFileType(Path path) {
//        if (Files.isDirectory(path)) {
//            return "[Dir]" + " " + path.getFileName().toString();
//        } else {
//            return "[File]" + " " + path.getFileName().toString();
//        }
//    }

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

    private void refreshClientView(Path path) throws IOException {
        try {
            currentDirectoryOnClient.setText(path.normalize().toAbsolutePath().toString()); //берем путь, нормализуем (убираем лишние символы такие как точки),
            // преобразуем к абсолютному пути и приводим к строке
            fileClientView.getItems().clear();// чистим список файлов перед заполнением, на всякий случай
            fileClientView.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList())); // получили ссылку на все элементы находящиеся по определенному пути
            //и преобразуем в виде объектов FileInfo после этого собираем в лист и передаем в таблицу
            fileClientView.sort(); // сортировка по умолчанию
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "по какой то причине не удалось обновить список файлов", ButtonType.OK);// перехватили исключение
            // вывели предупреждалку
            alert.showAndWait(); // показать эту пердупреждалку
        }
//        fileClientView.getItems().clear();
//        List<String> names = Files.list(currentDir)
//                .map(this::resolveFileType)
//                .collect(Collectors.toList());
//        fileClientView.getItems().addAll(names);
    }


    private void refreshServerView(ListResponse files) {
        fileServerView.getItems().clear();
        if (files != null)
            fileServerView.getItems().addAll(new ArrayList<>(files.getFileInfoServer));
        fileServerView.sort();
        //fileServerView.getItems().addAll(names);
    }


    // Выход по нажатию кнопки
    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

//    //Переход в папку по двойному щелчку
//    public void addNavigationListener() {
//        fileClientView.setOnMouseClicked(e -> {
//            if (e.getClickCount() == 2) {
//                String item = returnName2(fileClientView.getSelectionModel().getSelectedItem());
//                Path newPath = currentDir.resolve(item);
//                if (Files.isDirectory(newPath)) {
//                    currentDir = newPath;
//                    try {
//                        refreshClientView();
//                        currentDirectoryOnClient.setText(currentDir.toString());
//                    } catch (IOException ioException) {
//                        ioException.printStackTrace();
//                    }
//                } else {
//                    input.setText(item);
//                }
//            }
//        });
//        fileServerView.setOnMouseClicked(e -> {
//            if (e.getClickCount() == 2) {
//                String item = returnName2(fileServerView.getSelectionModel().getSelectedItem());
//                if (returnName1(fileServerView.getSelectionModel().getSelectedItem()).equals("[Dir]")) {
//                    net.sendCommand(new PathInRequest(item));
//                } else {
//                    input.setText(item);
//                }
//            }
//        });
//    }

    // выбор диска на клиенте
    public void diskSelection(ActionEvent actionEvent) throws IOException {// выбор диска на комбо боксе
        ComboBox<String> element = (ComboBox<String>)actionEvent.getSource(); // через действие (actionEvent) получаем информацию на какой диск нажал пользователь
        refreshClientView(Paths.get(element.getSelectionModel().getSelectedItem())); // и проверяем а что там выбрали
//        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource(); // через действие (actionEvent) получаем информацию на какой диск нажал пользователь
//        Path newPath = Paths.get(element.getSelectionModel().getSelectedItem());
//        currentDir = newPath;
//        refreshClientView();
//        try {
//            refreshClientView();// и проверяем а что там выбрали
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }
    }




        public void addNavigationListener() {
        fileClientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                //String item = returnName2(fileClientView.getSelectionModel().getSelectedItem());
                Path newPath = Paths.get(currentDirectoryOnClient.getText()).resolve(fileClientView.getSelectionModel().getSelectedItem().getFileName());
               // Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    try {
                        currentDir = newPath;
                        refreshClientView(currentDir);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    //currentDir = newPath;
//                    try {
//                        refreshClientView();
//                        currentDirectoryOnClient.setText(currentDir.toString());
//                    } catch (IOException ioException) {
//                        ioException.printStackTrace();
//                    }
//                } else {
//                    input.setText(item);
                }
            }
        });
//        fileServerView.setOnMouseClicked(e -> {
//            if (e.getClickCount() == 2) {
//                String item = returnName2(fileServerView.getSelectionModel().getSelectedItem());
//                if (returnName1(fileServerView.getSelectionModel().getSelectedItem()).equals("[Dir]")) {
//                    net.sendCommand(new PathInRequest(item));
//                } else {
//                    input.setText(item);
//                }
//            }
//        });
    }


}
