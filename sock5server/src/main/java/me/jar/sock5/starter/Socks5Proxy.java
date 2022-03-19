package me.jar.sock5.starter;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import me.jar.sock5.starter.handler.Socks5InitialRequestHandler;

public class Socks5Proxy {
    public static void main(String[] args) {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                // 添加socks5编码器
                pipeline.addLast("socks5encoder", Socks5ServerEncoder.DEFAULT);
                pipeline.addLast("socks5decoder", new Socks5InitialRequestDecoder());
                // 确认socks5交换方法
                pipeline.addLast("initialRequest", new Socks5InitialRequestHandler());
                pipeline.addLast("socks5CmdDecoder", new Socks5CommandRequestDecoder());
            }
        };
    }
}
