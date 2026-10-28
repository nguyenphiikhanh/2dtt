package vn.indiedev.tutien.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import vn.indiedev.tutien.game.GameService;
import vn.indiedev.tutien.config.ServerConfig;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class GameServer implements AutoCloseable {

    private final ServerConfig config;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ExecutorService gameExecutor = Executors.newFixedThreadPool(8, task -> {
        Thread thread = new Thread(task, "game-worker");
        thread.setDaemon(true);
        return thread;
    });
    private final GameService gameService;
    private Channel serverChannel;

    public GameServer(ServerConfig config, GameService gameService) {
        this.config = config;
        this.gameService = gameService;
    }

    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new GameWebSocketInitializer(gameService, gameExecutor));

        ChannelFuture bind = bootstrap.bind(config.host(), config.port()).sync();
        serverChannel = bind.channel();
    }

    public void awaitClose() throws InterruptedException {
        if (serverChannel != null) {
            serverChannel.closeFuture().sync();
        }
    }

    @Override
    public void close() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        gameExecutor.shutdown();
        try {
            if (!gameExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                gameExecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            gameExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
