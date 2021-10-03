package com.geekbrains;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// класс для описания хранимой информации в таблице
public class FileInfo {

    public enum FileType { // конструкция которая указывает имя файла или директории,
        // а так же, в дальнейшем, будет обозначать файл это или директория
        FILE("F"), DIRECTIRY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }

    }
    private String fileName; //имя файла или директории
    private FileType type; // тип
    private long size; // размер
    private LocalDateTime lastModified; // дата последнего изменения

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    // по указанному пути построить FileInfo
    public FileInfo(Path path){
        try {
            this.fileName = path.getFileName().toString(); // выдергиваем имя файла и преобразуем к строке
            this.size = Files.size(path); // по указанному пути узнаем его размер в байтах
            this.type = Files.isDirectory(path) ? FileType.DIRECTIRY : FileType.FILE; // если мы смотрим на директорию то будет директория, а если нет то файл
            if (this.type == FileType.DIRECTIRY){//если это директория
                this.size = -1L;//  то меняем размер на -1L
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(6)); // запрос даты последней модификации
            // со сдвигом часов на +6
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }
}
