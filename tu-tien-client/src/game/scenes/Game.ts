import { Scene } from 'phaser';
import { EventBus } from '../EventBus';
import type { KpahCharacterPartAsset, KpahMapAsset } from '../data/KpahAssets';

interface WorldPosition {
    characterId: number;
    characterName: string;
    cultivatorType: 'NAM_TU' | 'NU_TU';
    spiritType: string;
    mapId: number;
    mapName: string;
    x: number;
    y: number;
    mapWidth: number;
    mapHeight: number;
}

const MAP_SCALE = 2;

export class Game extends Scene {
    private characterSpriteLayers: { image: Phaser.GameObjects.Image; part: KpahCharacterPartAsset['parts'][number] }[] = [];
    private walkFrame = 0;
    private player?: Phaser.GameObjects.Container;
    private cursors?: Phaser.Types.Input.Keyboard.CursorKeys;
    private wasd?: Record<'W' | 'A' | 'S' | 'D', Phaser.Input.Keyboard.Key>;
    private world?: WorldPosition;
    private readonly mapData: KpahMapAsset;
    private readonly characterParts: KpahCharacterPartAsset;
    private movementElapsed = 0;

    constructor() {
        super('Game');
        this.mapData = {} as KpahMapAsset;
        this.characterParts = {} as KpahCharacterPartAsset;
    }

    create(data?: { world?: WorldPosition }) {
        Object.assign(this.mapData, this.cache.json.get('kpah-map-105') as KpahMapAsset);
        Object.assign(this.characterParts, this.cache.json.get('kpah-character-parts') as KpahCharacterPartAsset);
        this.cameras.main.setBackgroundColor('#17231e');
        this.drawStarterMap();
        if (this.input.keyboard) {
            this.cursors = this.input.keyboard.createCursorKeys();
            this.wasd = this.input.keyboard.addKeys('W,A,S,D') as Record<'W' | 'A' | 'S' | 'D', Phaser.Input.Keyboard.Key>;
        }
        EventBus.on('world-enter', this.enterWorld, this);
        EventBus.on('world-position', this.applyPosition, this);
        EventBus.on('world-leave', this.leaveWorld, this);
        this.events.once(Phaser.Scenes.Events.SHUTDOWN, () => {
            EventBus.off('world-enter', this.enterWorld, this);
            EventBus.off('world-position', this.applyPosition, this);
            EventBus.off('world-leave', this.leaveWorld, this);
        });
        EventBus.emit('current-scene-ready', this);
        if (data?.world) this.enterWorld(data.world);
    }

    update(_time: number, delta: number) {
        if (!this.world || !this.player || !this.cursors || !this.wasd) return;
        this.movementElapsed += delta;
        if (this.movementElapsed < 100) return;
        this.movementElapsed = 0;
        const left = this.cursors.left.isDown || this.wasd.A.isDown;
        const right = this.cursors.right.isDown || this.wasd.D.isDown;
        const up = this.cursors.up.isDown || this.wasd.W.isDown;
        const down = this.cursors.down.isDown || this.wasd.S.isDown;
        const dx = (right ? 8 : 0) - (left ? 8 : 0);
        const dy = (down ? 8 : 0) - (up ? 8 : 0);
        if (!dx && !dy) return;
        EventBus.emit('player-move', { dx, dy });
    }

    private drawStarterMap() {
        const cellSize = this.mapData.tileSize * MAP_SCALE;
        const width = this.mapData.width * cellSize;
        const height = this.mapData.height * cellSize;
        const ground = this.add.graphics();
        ground.fillStyle(0x304937, 1).fillRect(0, 0, width, height);

        for (let index = 0; index < this.mapData.tiles.length; index++) {
            const tileId = this.mapData.tiles[index];
            const blocked = (this.mapData.collision[index] & 2) === 2;
            const x = (index % this.mapData.width) * cellSize;
            const y = Math.floor(index / this.mapData.width) * cellSize;
            const color = this.tileColor(tileId, blocked);
            ground.fillStyle(color, 1).fillRect(x, y, cellSize, cellSize);
            ground.fillStyle(blocked ? 0x243b2d : 0xe0bc76, blocked ? 0.16 : 0.13);
            ground.fillRect(x + 3, y + 3, cellSize - 6, cellSize - 6);
        }
        ground.lineStyle(4, 0xd2bd89, 0.75).strokeRect(2, 2, width - 4, height - 4);

        this.add.text(20, 16, 'TÂN THỦ LINH ĐỊA  ·  KPAH 105', {
            fontFamily: 'Georgia, serif', fontSize: '16px', color: '#f0dfb0',
            stroke: '#17231e', strokeThickness: 4, letterSpacing: 1,
        }).setDepth(10);

        for (const [treeId, tileX, tileY] of this.mapData.trees) {
            if (tileX === 255) continue;
            const tree = this.add.image((tileX + 0.5) * cellSize, (tileY + 1) * cellSize, `kpah-tree-${treeId}`)
                .setOrigin(0.5, 1)
                .setScale(MAP_SCALE)
                .setDepth((tileY + 1) * cellSize);
            tree.setAlpha(0.96);
        }
    }

    private tileColor(tileId: number, blocked: boolean): number {
        if (blocked) {
            const greens = [0x35543b, 0x3c5c40, 0x456346, 0x304a38, 0x496849];
            return greens[(tileId + this.mapData.id) % greens.length];
        }
        const pathColors = [0x92794e, 0xa58a59, 0xb19a68, 0x9a8053];
        return pathColors[(tileId + this.mapData.width) % pathColors.length];
    }

    private enterWorld(world: WorldPosition) {
        this.world = world;
        this.player?.destroy();
        const anchor = this.add.ellipse(0, 0, 32, 11, 0x0d1812, 0.48);
        const layers: Phaser.GameObjects.GameObject[] = [anchor];
        this.characterSpriteLayers = [];
        const partId = world.cultivatorType === 'NAM_TU' ? 0 : 1;
        for (const type of [0, 1, 2, 3]) {
            const part = this.characterParts.parts.find((candidate) => candidate.type === type && candidate.partId === partId);
            if (!part) continue;
            const textureKey = `kpah-cloth-${type}-${partId}`;
            const texture = this.textures.get(textureKey);
            const frameName = 'standing';
            if (!texture.has(frameName)) {
                texture.add(frameName, 0, part.avx0, part.avy0, part.avw0, part.avh0);
            }
            const layer = this.add.image(part.avxf * MAP_SCALE, part.avyf * MAP_SCALE, textureKey, frameName)
                .setOrigin(0, 0)
                .setScale(MAP_SCALE);
            layers.push(layer);
            this.characterSpriteLayers.push({ image: layer, part });
        }
        const name = this.add.text(0, -152, world.characterName, {
            fontFamily: 'Georgia, serif', fontSize: '14px', color: '#fff2ca',
            stroke: '#18231d', strokeThickness: 4,
        }).setOrigin(0.5, 1);
        layers.push(name);
        this.player = this.add.container(world.x * MAP_SCALE, world.y * MAP_SCALE, layers)
            .setDepth(world.y * MAP_SCALE + 10);
        this.cameras.main.setBounds(0, 0, this.mapData.width * this.mapData.tileSize * MAP_SCALE,
            this.mapData.height * this.mapData.tileSize * MAP_SCALE);
        this.cameras.main.centerOn(world.x * MAP_SCALE, world.y * MAP_SCALE);
    }

    private applyPosition(position: WorldPosition) {
        if (!this.world || position.characterId !== this.world.characterId || !this.player) return;
        const oldX = this.world.x;
        const oldY = this.world.y;
        this.world.x = position.x;
        this.world.y = position.y;
        this.player.setX(position.x * MAP_SCALE).setDepth(position.y * MAP_SCALE + 10);
        if (position.x !== oldX || position.y !== oldY) {
            const direction = position.y > oldY ? 0 : position.y < oldY ? 3 : position.x < oldX ? 1 : 2;
            this.walkFrame = (this.walkFrame + 1) % 6;
            for (const { image, part } of this.characterSpriteLayers) {
                const dx = part.dx[direction]?.[this.walkFrame] ?? 0;
                const dy = part.dy[direction]?.[this.walkFrame] ?? 0;
                image.setPosition((part.avxf + dx) * MAP_SCALE, (part.avyf + dy) * MAP_SCALE);
            }
            this.player.setScale(position.x < oldX ? -1 : 1, 1);
        } else {
            for (const { image, part } of this.characterSpriteLayers) {
                image.setPosition(part.avxf * MAP_SCALE, part.avyf * MAP_SCALE);
            }
        }
    }

    private leaveWorld() {
        this.world = undefined;
        this.player?.destroy();
        this.player = undefined;
        this.characterSpriteLayers = [];
        this.cameras.main.setScroll(0, 0);
    }
}
