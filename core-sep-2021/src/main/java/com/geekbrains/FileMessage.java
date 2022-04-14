package com.geekbrains;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Command {

    private final String name;
    private final byte[] bytes;
    //private final File file;



    public FileMessage(Path path) throws IOException {
        name = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
    }

//    public FileMessage(File file) throws IOException {
//        name = file.getName();
//        this.file = file;
//    }

//    public File getFile(){
//        return file;
//    }

    public String getName() {
        return name;
    }


    public byte[] getBytes() {
        return bytes;
    }


    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}
