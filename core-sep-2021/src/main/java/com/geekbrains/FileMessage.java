package com.geekbrains;


import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Data
public class FileMessage extends Command {

    private final String name;
    private final File file;


    public FileMessage(File file) throws IOException {
        name = file.getName();
        this.file = file;
    }


    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}
