export interface KpahMapAsset {
    id: number;
    name: string;
    tileSize: number;
    width: number;
    height: number;
    tiles: number[];
    collision: number[];
    trees: number[][];
}

export interface KpahCharacterPart {
    partId: number;
    type: number;
    avxf: number;
    avyf: number;
    avx0: number;
    avy0: number;
    avw0: number;
    avh0: number;
    x: number[][];
    y: number[][];
    w: number[][];
    h: number[][];
    dx: number[][];
    dy: number[][];
}

export interface KpahCharacterPartAsset {
    source: string;
    parts: KpahCharacterPart[];
}
