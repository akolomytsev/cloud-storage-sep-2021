package com.geekbrains;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// для отображения списка файлов на сервере
@Getter
public class ListResponse extends Command{

    private final List<FileInfo> fileInfoServer;
    public int getFileInfoServer;


    public ListResponse(Path path) throws IOException {
        Stream<FileInfo> list = Files.list(path).map(FileInfo::new);
        fileInfoServer = list.collect(Collectors.toList());
        list.close();
    }

//    private final List<String> list;
//
//    public ListResponse(Path path) throws IOException {
//        list = Files.list(path)
//                .map(this::resolveFileType)
//                .collect(Collectors.toList());
//    }

//    private String resolveFileType(Path path) {
//        if (Files.isDirectory(path)) {
//            return "[Dir]" + " " + path.getFileName().toString();
//        } else {
//            return "[File]" + " " + path.getFileName().toString();
//        }
//    }
//    public List<String> getList() {
//        return list;
//    }

    @Override
    public CommandType getType() {return CommandType.LIST_RESPONSE;}
}
