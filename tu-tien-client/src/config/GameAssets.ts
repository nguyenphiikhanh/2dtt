const socketUrl = import.meta.env.VITE_GAME_WS_URL || 'ws://localhost:19130/game';

const defaultAssetBaseUrl = () => {
    const url = new URL(socketUrl);
    url.protocol = url.protocol === 'wss:' ? 'https:' : 'http:';
    url.pathname = '/assets/';
    url.search = '';
    url.hash = '';
    return url.toString();
};

const configuredBaseUrl = import.meta.env.VITE_GAME_ASSET_URL || defaultAssetBaseUrl();
const assetBaseUrl = configuredBaseUrl.endsWith('/') ? configuredBaseUrl : `${configuredBaseUrl}/`;

export const gameAssetUrl = (path: string): string =>
    new URL(path.replace(/^\/+/, ''), assetBaseUrl).toString();
