package com.netty.demo.demo.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    // 服务端NIO线程组
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    public ChannelFuture start(String host, int port) {
        ChannelFuture channelFuture = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
//                            socketChannel.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
                            // 自定义服务处理
                            // LineBasedFrameDecoder能够将接收到的数据在行尾进行拆分。
                            // 设置解码帧的最大长度，如果帧的长度超过此值抛出异常
//                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            //需要注意的是，netty并没有提供一个DelimiterBasedFrameDecoder对应的编码器实现(笔者没有找到)，因此在发送端需要自行编码添加分隔符，如 \r \n分隔符
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));

                            //实现5秒钟，如果两端，如果数据读取，直接断开连接
//                            socketChannel.pipeline().addLast(new ReadTimeoutHandler(5));
//实现5秒钟，如果两端，如果数据写入，直接断开连接
                            //socketChannel.pipeline().addLast(new WriteTimeoutHandler(5));
                            // 服务处理
                            socketChannel.pipeline().addLast(new ServerHandler());
//                            socketChannel.pipeline().addLast(new ServerHandlerMarshalling());
                        }
                    });
            // 绑定端口并同步等待
            channelFuture = bootstrap.bind(host, port).sync();

//            channelFuture.channel().closeFuture().sync();
            logger.info("======Start Up Success!========="+port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelFuture;
    }

    public void close() {
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        logger.info("======Shutdown Netty Server Success!=========");
    }
}