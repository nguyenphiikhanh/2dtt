export type ConnectionState = 'disconnected' | 'connecting' | 'connected' | 'error';

export class GameSocket {
    private socket?: WebSocket;

    constructor(private readonly url: string) {}

    connect(
        onState: (state: ConnectionState) => void,
        onMessage: (message: string) => void
    ): void {
        if (this.socket?.readyState === WebSocket.OPEN ||
            this.socket?.readyState === WebSocket.CONNECTING) {
            return;
        }

        onState('connecting');
        const socket = new WebSocket(this.url);
        this.socket = socket;

        socket.addEventListener('open', () => onState('connected'), { once: true });
        socket.addEventListener('message', (event: MessageEvent<string>) => {
            onMessage(String(event.data));
        });
        socket.addEventListener('error', () => onState('error'), { once: true });
        socket.addEventListener('close', () => {
            if (this.socket === socket) {
                this.socket = undefined;
                onState('disconnected');
            }
        }, { once: true });
    }

    ping(): boolean {
        return this.send({ type: 'ping' });
    }

    send(payload: unknown): boolean {
        if (this.socket?.readyState !== WebSocket.OPEN) return false;
        this.socket.send(JSON.stringify(payload));
        return true;
    }

    disconnect(): void {
        const socket = this.socket;
        this.socket = undefined;
        if (socket && socket.readyState < WebSocket.CLOSING) {
            socket.close(1000, 'Client disconnect');
        }
    }
}
