package me.jar.starter;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import me.jar.constants.ProxyConstants;
import me.jar.handler.ConnectRemoteHandler;
import me.jar.utils.DecryptHandler;
import me.jar.utils.EncryptHandler;
import me.jar.utils.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Date 2021/4/23-23:45
 */
public class ServerStarter {
    static {
        String path = ServerStarter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.contains(".jar")) {
            String osName = System.getProperty("os.name");
            String tempPath;
            if (osName.contains("Windows")) {
                tempPath = path.substring(path.indexOf("/") + 1, path.indexOf(".jar"));
            } else {
                tempPath = path.substring(path.indexOf("/"), path.indexOf(".jar"));
            }
            String targetDirPath = tempPath.substring(0, tempPath.lastIndexOf("/"));
            System.out.println("target path: " + targetDirPath);
            System.setProperty("WORKDIR", targetDirPath);
        } else {
            System.out.println("current path not contain .jar file");
            System.exit(1);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStarter.class);

    private final int port;

    public ServerStarter(int port) {
        this.port = port;
    }

    public void run() {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                // 添加与客户端交互的handler
                pipeline.addLast("decrypt", new DecryptHandler());
                pipeline.addLast("encrypt", new EncryptHandler());
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
                pipeline.addLast("chunk", new ChunkedWriteHandler());
                pipeline.addLast("connectRemote", new ConnectRemoteHandler());
            }
        };
        NettyUtil.starServer(port, channelInitializer);
    }

    public static void main(String[] args) {
        if (ProxyConstants.PROPERTY.containsKey(ProxyConstants.KEY_NAME_PORT)) {
            String port = ProxyConstants.PROPERTY.get(ProxyConstants.KEY_NAME_PORT);
            try {
                int portNum = Integer.parseInt(port.trim());
                new ServerStarter(portNum).run();
            } catch (NumberFormatException e) {
                LOGGER.error("===Failed to parse number, property setting may be wrong.", e);
            }
        } else {
            LOGGER.error("===Failed to get port from property, starting server failed.");
        }
    }
}
