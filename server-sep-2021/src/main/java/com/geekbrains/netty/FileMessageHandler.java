package com.geekbrains.netty;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import com.geekbrains.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private static Path currentPath;

    public FileMessageHandler() throws IOException {
        currentPath = Paths.get("server-sep-2021", "root"); // путь до нашей директории (при авторизации добавить userName,
        // который получаем как ответ от сервиса авторизации)
        if (!Files.exists(currentPath)) {// если такой директории не существует
            Files.createDirectory(currentPath); // то мы ее создаем
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // при активации канала
        ctx.writeAndFlush(new ListResponse(currentPath)); // мы отправляем список файлов на сервере
        ctx.writeAndFlush(new PathResponse(currentPath.toString())); // и где мы сейчас на сервере
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        switch (cmd.getType()) {
            case FILE_REQUEST:  // если запрос на файл
                FileRequest fileRequest = (FileRequest) cmd;
                FileMessage msg = new FileMessage(currentPath.resolve(fileRequest.getName())); // мы собираем файл из currentPath.resolve(fileRequest.getName())
                ctx.writeAndFlush(msg); // и отправляем в контекст ctx
                break;
            case FILE_MESSAGE: // на сервер летит файл
                FileMessage fileMessage = (FileMessage) cmd;// мы команду преобразуем в FileMessage
                Files.write(currentPath.resolve(fileMessage.getName()), fileMessage.getBytes()); // пересоздаем ресурс, надо разобраться
                ctx.writeAndFlush(new ListResponse(currentPath)); // шлем обновление списка файлов на сервере
                break;
            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(currentPath)); // просто обновление списка файлов на сервере
                break;
            case PATH_UP_REQUEST:
                if (currentPath.getParent() != null) { // Можем делать если у текущего пути есть перент
                    currentPath = currentPath.getParent(); // если он есть то делаем дальнейшие действия
                }
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) cmd;
                Path newPath = currentPath.resolve(request.getDir()); // новый путь newPath достаем из request.getDir()
                if (Files.isDirectory(newPath)) { //  и если newPath это директория
                    currentPath = newPath; // то создаем новый путь currentPath = newPath
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                }
                break;
        }
    }
}
