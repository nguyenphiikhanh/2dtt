package vn.indiedev.tutien.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import vn.indiedev.tutien.game.GameService;
import java.util.concurrent.Executor;

final class GameWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private static final int MAX_FRAME_SIZE = 64 * 1024;
    private final GameService gameService;
    private final Executor gameExecutor;

    GameWebSocketInitializer(GameService gameService, Executor gameExecutor) {
        this.gameService = gameService;
        this.gameExecutor = gameExecutor;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(MAX_FRAME_SIZE));
        pipeline.addLast(new GameAssetHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(
                "/game", null, false, MAX_FRAME_SIZE));
        pipeline.addLast(new GameWebSocketHandler(gameService, gameExecutor));
    }
}
