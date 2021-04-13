package com.netty.demo.demo.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 客户端连接类
 * @author Administrator
 *
 */
public class Client {

    private static class SingletonHolder {
        static final Client instance = new Client();
    }

    public static Client getInstance(){
        return SingletonHolder.instance;
    }

    private EventLoopGroup group;
    private Bootstrap b;
    private ChannelFuture cf ;

    private Client(){
        group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
                        sc.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());

                        //客户端，同样可以做定长时间，自动断开连接操作
                        //实现5秒钟，如果两端，如果数据读取，直接断开连接
                        //sc.pipeline().addLast(new ReadTimeoutHandler(5));
                        //实现5秒钟，如果两端，如果数据写入，直接断开连接
                        //sc.pipeline().addLast(new WriteTimeoutHandler(5));

                        sc.pipeline().addLast(new ClientHandler());
                    }
                });
    }

    public void connect(){
        try {
            this.cf = b.connect("127.0.0.1", 7070).sync();
            System.out.println("远程服务器已经连接, 可以进行数据交换..");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChannelFuture getChannelFuture(){

        if(this.cf == null){
            this.connect();
        }
        if(!this.cf.channel().isActive()){
            this.connect();
        }

        return this.cf;
    }

    public static void main(String[] args) throws Exception{
        final Client c = Client.getInstance();

        ChannelFuture cf = c.getChannelFuture();
        for(int i = 1; i <= 3; i++ ){
            Request request = new Request();
            request.setId("" + i);
            request.setName("pro" + i);
            request.setRequestMessage("数据信息" + i);
            cf.channel().writeAndFlush(request);
            cf.channel().writeAndFlush("\r\n");
            TimeUnit.SECONDS.sleep(4);
            System.out.println(new Date());
        }

        //再等待2秒钟
        TimeUnit.SECONDS.sleep(2);
        Request request = new Request();
        request.setId("4");
        request.setName("pro4");
        request.setRequestMessage("数据信息4");
        cf.channel().writeAndFlush(request);


        //等待关闭连接
        cf.channel().closeFuture().sync();
        System.out.println("断开连接,主线程结束..");
    }
}