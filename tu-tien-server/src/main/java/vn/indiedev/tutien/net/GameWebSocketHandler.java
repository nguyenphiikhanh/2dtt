package vn.indiedev.tutien.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import vn.indiedev.tutien.game.AccountProfile;
import vn.indiedev.tutien.game.CharacterProfile;
import vn.indiedev.tutien.game.GameException;
import vn.indiedev.tutien.game.GameService;
import vn.indiedev.tutien.game.WorldPosition;

import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

final class GameWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final AttributeKey<Long> ACCOUNT_ID = AttributeKey.valueOf("accountId");
    private static final AttributeKey<Long> ACTIVE_CHARACTER_ID = AttributeKey.valueOf("activeCharacterId");

    private final GameService gameService;
    private final Executor gameExecutor;

    GameWebSocketHandler(GameService gameService, Executor gameExecutor) {
        this.gameService = gameService;
        this.gameExecutor = gameExecutor;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            send(context, JSON.createObjectNode()
                    .put("type", "server.ready")
                    .put("protocolVersion", 1));
        }
        super.userEventTriggered(context, event);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame frame) {
        final JsonNode request;
        try {
            request = JSON.readTree(frame.text());
        } catch (Exception exception) {
            sendError(context, null, "invalid_json", "Dữ liệu gửi lên không hợp lệ.");
            return;
        }

        if (request == null || !request.isObject()) {
            sendError(context, null, "invalid_request", "Yêu cầu không hợp lệ.");
            return;
        }
        String type = request.path("type").asText("");
        String requestId = request.path("requestId").isTextual()
                ? request.path("requestId").asText() : null;

        switch (type) {
            case "ping" -> send(context, JSON.createObjectNode().put("type", "pong").put("requestId", requestId));
            case "auth.register", "auth.login" -> authenticate(context, request, requestId, type.equals("auth.register"));
            case "auth.logout" -> {
                context.channel().attr(ACCOUNT_ID).set(null);
                context.channel().attr(ACTIVE_CHARACTER_ID).set(null);
                send(context, JSON.createObjectNode().put("type", "auth.logged_out").put("requestId", requestId));
            }
            case "character.create" -> createCharacter(context, request, requestId);
            case "character.enter" -> enterWorld(context, request, requestId);
            case "player.move" -> movePlayer(context, request, requestId);
            case "character.equip" -> equipItem(context, request, requestId);
            default -> sendError(context, requestId, "unknown_command", "Lệnh không được hỗ trợ.");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        if (isPeerDisconnect(cause)) {
            context.close();
            return;
        }
        System.err.println("Unhandled network pipeline failure: " + cause.getClass().getSimpleName());
        cause.printStackTrace(System.err);
        context.close();
    }

    private boolean isPeerDisconnect(Throwable cause) {
        for (Throwable current = cause; current != null; current = current.getCause()) {
            if (current instanceof IOException || current instanceof ClosedChannelException) {
                return true;
            }
        }
        return false;
    }

    private void authenticate(ChannelHandlerContext context, JsonNode request, String requestId, boolean register) {
        String username = request.path("username").asText("");
        char[] password = request.path("password").asText("").toCharArray();
        boolean accepted = submit(context, requestId, () -> {
            try {
                AccountProfile account = register
                        ? gameService.register(username, password)
                        : gameService.login(username, password);
                context.channel().attr(ACCOUNT_ID).set(account.id());
                ObjectNode response = JSON.createObjectNode()
                        .put("type", "auth.success")
                        .put("requestId", requestId)
                        .put("createdAccount", register);
                response.set("account", JSON.valueToTree(account));
                send(context, response);
            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        });
        if (!accepted) {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void createCharacter(ChannelHandlerContext context, JsonNode request, String requestId) {
        Long accountId = context.channel().attr(ACCOUNT_ID).get();
        if (accountId == null) {
            sendError(context, requestId, "unauthenticated", "Vui lòng đăng nhập trước.");
            return;
        }
        String name = request.path("name").asText("");
        int classId = request.path("classId").asInt(0);
        int gender = request.path("gender").asInt(request.has("cultivatorType") && "NU_TU".equalsIgnoreCase(request.path("cultivatorType").asText()) ? 2 : 1);
        int headId = request.path("headId").asInt(0);
        int nationId = request.path("nationId").asInt(0);

        submit(context, requestId, () -> {
            CharacterProfile character = gameService.createCharacter(accountId, name, classId, gender, headId, nationId);
            ObjectNode response = JSON.createObjectNode()
                    .put("type", "character.created")
                    .put("requestId", requestId);
            response.set("character", JSON.valueToTree(character));
            send(context, response);
        });
    }

    private void equipItem(ChannelHandlerContext context, JsonNode request, String requestId) {
        Long accountId = context.channel().attr(ACCOUNT_ID).get();
        if (accountId == null) {
            sendError(context, requestId, "unauthenticated", "Vui lòng đăng nhập trước.");
            return;
        }
        long characterId = request.path("characterId").asLong(0);
        long itemId = request.path("itemId").asLong(0);
        submit(context, requestId, () -> {
            CharacterProfile character = gameService.equipItem(accountId, characterId, itemId);
            ObjectNode response = JSON.createObjectNode()
                    .put("type", "character.updated")
                    .put("requestId", requestId);
            response.set("character", JSON.valueToTree(character));
            send(context, response);
        });
    }

    private void enterWorld(ChannelHandlerContext context, JsonNode request, String requestId) {
        Long accountId = context.channel().attr(ACCOUNT_ID).get();
        long characterId = request.path("characterId").asLong(0);
        if (accountId == null) {
            sendError(context, requestId, "unauthenticated", "Vui lòng đăng nhập trước.");
            return;
        }
        submit(context, requestId, () -> {
            WorldPosition world = gameService.enterWorld(accountId, characterId);
            context.channel().attr(ACTIVE_CHARACTER_ID).set(characterId);
            ObjectNode response = JSON.createObjectNode().put("type", "character.entered").put("requestId", requestId);
            response.set("world", JSON.valueToTree(world));
            send(context, response);
        });
    }

    private void movePlayer(ChannelHandlerContext context, JsonNode request, String requestId) {
        Long accountId = context.channel().attr(ACCOUNT_ID).get();
        Long characterId = context.channel().attr(ACTIVE_CHARACTER_ID).get();
        if (accountId == null || characterId == null) {
            sendError(context, requestId, "not_in_world", "Hãy vào bản đồ bằng một nhân vật trước.");
            return;
        }
        int dx = request.path("dx").asInt(0);
        int dy = request.path("dy").asInt(0);
        submit(context, requestId, () -> {
            WorldPosition position = gameService.moveCharacter(accountId, characterId, dx, dy);
            ObjectNode response = JSON.createObjectNode().put("type", "player.moved").put("requestId", requestId);
            response.set("position", JSON.valueToTree(position));
            send(context, response);
        });
    }

    private boolean submit(ChannelHandlerContext context, String requestId, GameTask task) {
        try {
            gameExecutor.execute(() -> {
                try {
                    task.run();
                } catch (GameException exception) {
                    sendError(context, requestId, exception.code(), exception.getMessage());
                } catch (SQLException exception) {
                    System.err.println("Database operation failed: " + exception.getSQLState());
                    sendError(context, requestId, "server_error", "Máy chủ chưa thể xử lý yêu cầu.");
                } catch (RuntimeException exception) {
                    System.err.println("Game operation failed: " + exception.getClass().getSimpleName());
                    sendError(context, requestId, "server_error", "Máy chủ chưa thể xử lý yêu cầu.");
                }
            });
            return true;
        } catch (RejectedExecutionException exception) {
            sendError(context, requestId, "server_busy", "Máy chủ đang bận, vui lòng thử lại.");
            return false;
        }
    }

    private void sendError(ChannelHandlerContext context, String requestId, String code, String message) {
        ObjectNode response = JSON.createObjectNode().put("type", "error").put("code", code).put("message", message);
        if (requestId != null) {
            response.put("requestId", requestId);
        }
        send(context, response);
    }

    private void send(ChannelHandlerContext context, JsonNode response) {
        if (!context.channel().isActive()) {
            return;
        }
        try {
            context.writeAndFlush(new TextWebSocketFrame(JSON.writeValueAsString(response)));
        } catch (Exception exception) {
            context.close();
        }
    }

    @FunctionalInterface
    private interface GameTask {
        void run() throws SQLException, GameException;
    }
}
