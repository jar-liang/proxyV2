package me.jar.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.jar.utils.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Date 2021/4/27-22:17
 */
public class ReceiveFarHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveFarHandler.class);

    private final Channel clientChannel;

    public ReceiveFarHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (clientChannel.isActive()) {
            clientChannel.writeAndFlush(msg);
        } else {
            LOGGER.error("===Client channel disconnected, no transferring data.");
            NettyUtil.closeOnFlush(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.debug("===Far channel disconnected");
        NettyUtil.closeOnFlush(clientChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("===ReceiveRemoteHandler caught exception, cause: {}", cause.getMessage() + ". host: " + ctx.channel().remoteAddress().toString());
        NettyUtil.closeOnFlush(clientChannel);
        ctx.close();
    }
}
