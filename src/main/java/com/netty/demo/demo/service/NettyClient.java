package com.netty.demo.demo.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;

public class NettyClient {

    private static final EventLoopGroup group = new NioEventLoopGroup();

    public static void main(String[] args) throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
//                    socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    socketChannel.pipeline().addLast(new StringEncoder());
                    socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(
                            Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                    // 自定义处理程序
                    socketChannel.pipeline().addLast(new ClientHandler());
                }
            });
            // 绑定端口并同步等待
            ChannelFuture channelFuture = bootstrap.connect("localhost", 7070).sync();

            channelFuture.channel().writeAndFlush("test"+"\r\n");
            //等待关闭连接
            channelFuture.channel().closeFuture().sync();
            //接收服务端返回的数据
            AttributeKey<String> key = AttributeKey.valueOf("ServerData");
            Object result = channelFuture.channel().attr(key).get();
//            System.out.println(result.toString());
            System.out.println("断开连接,主线程结束..");
        } finally {
            group.shutdownGracefully();
        }
    }

}