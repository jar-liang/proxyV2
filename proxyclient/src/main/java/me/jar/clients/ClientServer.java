package me.jar.clients;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import me.jar.constants.ProxyConstants;
import me.jar.handler.ConnectFarHandler;
import me.jar.utils.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Description
 * @Date 2021/4/27-21:31
 */
public class ClientServer {
    static {
        String path = ClientServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServer.class);

    private final int port;

    public ClientServer(int port) {
        this.port = port;
    }

    public void run() {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast("connectFar", new ConnectFarHandler());
            }
        };
        NettyUtil.starServer(port, channelInitializer);
    }

    public static void main(String[] args) {
        if (ProxyConstants.PROPERTY.containsKey(ProxyConstants.KEY_NAME_PORT)) {
            String port = ProxyConstants.PROPERTY.get(ProxyConstants.KEY_NAME_PORT);
            try {
                int portNum = Integer.parseInt(port.trim());
                new ClientServer(portNum).run();
            } catch (NumberFormatException e) {
                LOGGER.error("===Failed to parse number, property setting may be wrong.", e);
            }
        } else {
            LOGGER.error("===Failed to get port from property, starting server failed.");
        }
    }
}
