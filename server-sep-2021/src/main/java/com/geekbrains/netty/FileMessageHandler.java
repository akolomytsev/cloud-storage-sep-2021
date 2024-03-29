package com.geekbrains.netty;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.geekbrains.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.FileUtils;

@Slf4j


public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private static Path currentPath;
    private static Path clientPath;
    DBAuthService service = new DBAuthService();



    @Override
    public void channelActive(ChannelHandlerContext ctx) { // при активации канала
        log.debug("Client connected!");
//        System.out.println("Client connected: " + ctx.channel());
//        try {
//            dataBasesHandler.dbConnect();
//        } catch (SQLException | ClassNotFoundException e) {
//            ctx.writeAndFlush("Information: Database is not connected");
//            e.printStackTrace();
//        }
//        ctx.writeAndFlush(new ListResponse(currentPath)); // мы отправляем список файлов на сервере
//        ctx.writeAndFlush(new PathResponse(currentPath.toString())); // и где мы сейчас на сервере
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        log.debug("Received command from client: {}", cmd.getType());
        switch (cmd.getType()) {
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) cmd;
                Files.write(
                        currentPath.resolve(fileMessage.getName()), fileMessage.getBytes()
                        //currentPath.resolve(fileMessage.)
                );
                //FileUtils.copyFile(fileMessage.getFile(), new File(fileMessage.getName()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                //log.debug("Received a file {} from the client", fileMessage.getName());
                break;

            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) cmd;
                String fileName = fileRequest.getName();
                Path file = Paths.get(String.valueOf(currentPath), fileName);
                ctx.writeAndFlush(new FileMessage(file));
                //ctx.writeAndFlush(new FileMessage(file.toFile()));
                log.debug("Send file {} to the client", fileName);
                break;

            case LIST_REQUEST:

                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                log.debug("Send list of files to the client");
                break;

            case PATH_UP_REQUEST:
                if (currentPath.getParent() != null) {
                    if (clientPath.equals(currentPath)) {
                        log.debug("Above the client's folder , it is not necessary to rise");
                    } else {
                        currentPath = currentPath.getParent();
                    }
                }
                log.debug("Send list of files and current directory to the client");
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;

            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) cmd;
                Path newPAth = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPAth)) {
                    currentPath = newPAth;
                    log.debug("Send list of files and current directory to the client");
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                } else {
                    log.debug("{} is not a directory",request);
                }
                break;

            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) cmd;
                String login = authRequest.getLogin();
     //           String password = authRequest.getPassword();
                AuthResponse authResponse = new AuthResponse();
                try {
                   // if (service.findByLogin(login).equals(password)) {
                    if (true){
                        authResponse.setAuthStatus(true);
                        clientPath = Paths.get(".", login).normalize();
                        if (!Files.exists(clientPath)) { // если нет такой папки
                            Files.createDirectory(clientPath); // то создаем ее
                        }
                        currentPath = clientPath;
                    } else {
                        authResponse.setAuthStatus(false);
                    }} catch (Exception e){
                    authResponse.setAuthStatus(false);
                }
                ctx.writeAndFlush(authResponse);
                break;
                case DELETE:
                Delete delete = (Delete) cmd;
                File file1 = new File(String.valueOf(currentPath.resolve(delete.getName())));
                Delete.deleteFile(file1);
                ctx.writeAndFlush(new ListResponse(currentPath)); // шлем обновление списка файлов на сервере
                break;

            default:
                log.debug("Invalid command {}", cmd.getType());
                break;


        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("dis");
        System.out.println("Client disconnected: " + ctx.channel());
        DBAuthService.disconnect();
        ctx.close();
    }
}
