package com.geekbrains;

import java.io.File;

public class Delete extends Command{

    private final String name;

    public Delete(String name) {
        this.name = name;
    }

    public String getName(){ return name;}

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deleteFile(sub);
            }
        }
        file.delete();
    }

    @Override
    public CommandType getType() {return CommandType.DELETE;
    }
}
