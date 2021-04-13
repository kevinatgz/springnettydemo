package com.netty.demo.demo.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 连接到服务器时触发
     */
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.copiedBuffer("current time", CharsetUtil.UTF_8));
//    }
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        for (int i = 0; i < 10; i++) {
//            ctx.write(Unpooled.copiedBuffer("current time"+i+" \n", CharsetUtil.UTF_8));
////            ctx.write(Unpooled.copiedBuffer("current time", CharsetUtil.UTF_8));
//        }
//        ctx.flush();
//    }

    /**
     * 消息到来时触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("Current Time: " + buf.toString(CharsetUtil.UTF_8));
    }

    /**
     * 发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}