package me.jar.sock5.starter.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5InitialRequestHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DefaultSocks5InitialRequest request) throws Exception {
        if (request.decoderResult().isFailure()) {
            LOGGER.info("Not socks5 protocol");
            channelHandlerContext.fireChannelRead(request);
        } else {
            if (SocksVersion.SOCKS5.equals(request.version())) {
                DefaultSocks5InitialResponse socks5InitialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
                channelHandlerContext.writeAndFlush(socks5InitialResponse);
            }
        }
    }
}
