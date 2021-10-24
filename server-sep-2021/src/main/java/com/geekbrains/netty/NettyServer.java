package com.geekbrains.netty;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public class NettyServer {

    private int PORT = 8184;
    private ChannelFuture channelFuture;

    public NettyServer() {

        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new FileMessageHandler()

                            );
                        }
                    });
            channelFuture = serverBootstrap.bind(PORT).sync();
            log.debug("Server started " + new Date());
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e) {
            log.error("Server exception: Stacktrace: ", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
