package me.jar.sock5.starter.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;

public class Socks5CommandRequestHandler  extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DefaultSocks5CommandRequest msg) throws Exception {
        System.out.println("目标服务器：" + msg.type() + ", 地址：" + msg.dstAddr() + "，端口：" + msg.dstPort());
        if (Socks5CommandType.CONNECT.equals(msg.type())) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Dest2ClientHandler(channelHandlerContext));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(msg.dstAddr(), msg.dstPort());
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("成功连接服务器");
                        channelHandlerContext.pipeline().addLast(new Client2DestHandler(future));
                        DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                        channelHandlerContext.writeAndFlush(commandResponse);
                    } else {
                        DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                        channelHandlerContext.writeAndFlush(commandResponse);
                    }
                }
            });
        } else {
            channelHandlerContext.fireChannelRead(msg);
        }
    }

    private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {
        private ChannelHandlerContext clientChannelContext;

        public Dest2ClientHandler(ChannelHandlerContext channelHandlerContext) {
            this.clientChannelContext = channelHandlerContext;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            clientChannelContext.writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("目标服务器断开连接");
            clientChannelContext.channel().close();
        }
    }

    private static class Client2DestHandler extends ChannelInboundHandlerAdapter {
        private ChannelFuture destChannelFuture;

        public Client2DestHandler(ChannelFuture destChannelFuture) {
            this.destChannelFuture = destChannelFuture;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            destChannelFuture.channel().writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端断开连接");
            destChannelFuture.channel().close();
        }
    }
}
