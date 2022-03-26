package me.jar.sock5.starter;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import me.jar.sock5.starter.handler.Socks5CommandRequestHandler;
import me.jar.sock5.starter.handler.Socks5InitialRequestHandler;
import me.jar.utils.NettyUtil;

public class Socks5Proxy {
    private static int port = 9999; // todo 后面要动态读取

    public static void main(String[] args) {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                // 添加socks5编码器
                pipeline.addLast("socks5encoder", Socks5ServerEncoder.DEFAULT);
                // byte to message
                pipeline.addLast("socks5decoder", new Socks5InitialRequestDecoder());
                // 回复客户端不使用授权
                pipeline.addLast("initialRequest", new Socks5InitialRequestHandler());
                // byte to message
                pipeline.addLast("socks5CmdDecoder", new Socks5CommandRequestDecoder());
                // 连接服务器，设置转发
                pipeline.addLast("sock5Handler", new Socks5CommandRequestHandler());
            }
        };

        NettyUtil.starServer(port, channelInitializer);
    }
}
