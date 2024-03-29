package me.jar.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import me.jar.constants.ProxyConstants;
import me.jar.utils.DecryptHandler;
import me.jar.utils.EncryptHandler;
import me.jar.utils.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Date 2021/4/27-21:50
 */
public class ConnectFarHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectFarHandler.class);

    private Channel farChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 直连far端，将数据发送过去
        if (farChannel != null && farChannel.isActive()) {
            farChannel.writeAndFlush(msg);
        } else {
            LOGGER.error("far channel not active!");
            NettyUtil.closeOnFlush(ctx.channel());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().config().setAutoRead(false);
        if (!ProxyConstants.PROPERTY.containsKey(ProxyConstants.FAR_SERVER_IP) || !ProxyConstants.PROPERTY.containsKey(ProxyConstants.KEY_NAME_PORT)) {
            LOGGER.error("===Property file has no far server ip or port, please check!");
            ctx.close();
            return;
        }

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decrypt", new DecryptHandler());
                        pipeline.addLast("encrypt", new EncryptHandler());
                        pipeline.addLast("chunk", new ChunkedWriteHandler());
                        pipeline.addLast("receiveFar", new ReceiveFarHandler(ctx.channel()));
                    }
                });
        String host = ProxyConstants.PROPERTY.get(ProxyConstants.FAR_SERVER_IP);
        int port = Integer.parseInt(ProxyConstants.PROPERTY.get(ProxyConstants.FAR_SERVER_PORT));
        bootstrap.connect(host, port)
                .addListener((ChannelFutureListener) connectFuture -> {
                    if (connectFuture.isSuccess()) {
                        LOGGER.debug(">>>Connect far server successfully.");
                        farChannel = connectFuture.channel();
                        ctx.channel().config().setAutoRead(true);
                    } else {
                        LOGGER.error("===Failed to connect to far server! host: " + host + " , port: " + port);
                        ctx.close();
                    }
                });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.debug("===Client disconnected.");
        NettyUtil.closeOnFlush(farChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("===ConnectFarHandler has caught exception, cause: {}", cause.getMessage());
        NettyUtil.closeOnFlush(farChannel);
        ctx.close();
    }
}
