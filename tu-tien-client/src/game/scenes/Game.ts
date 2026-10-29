import { Scene } from 'phaser';
import { EventBus } from '../EventBus';
import type { KpahCharacterPartAsset, KpahMapAsset } from '../data/KpahAssets';

export interface WorldPosition {
    characterId: number;
    characterName: string;
    classId: number;
    className: string;
    gender: number;
    headId: number;
    nationId: number;
    mapId: number;
    mapName: string;
    x: number;
    y: number;
    mapWidth: number;
    mapHeight: number;
}

const MAP_SCALE = 2;
const MOVE_SPEED = 96; // world pixels per second (~192 screen pixels/sec)
const IDLE_BREATH_TICK = 320; // 320ms per breath step (640ms cycle), matching snappy retro KPAH idle rhythm

// Authentic KPAH step-to-frame mapping:
// Steps 0..5 in walk cycle -> frames [0, 1, 2, 0, 3, 4] matching exact dx/dy offsets
const STEP_TO_FRAME = [0, 1, 2, 0, 3, 4];

interface PartLayer {
    image: Phaser.GameObjects.Image;
    part: KpahCharacterPartAsset['parts'][number];
    type: number;
    partId: number;
}

interface NpcEntity {
    container: Phaser.GameObjects.Container;
    layers: PartLayer[];
    name: string;
    role: string;
    dialogue: string;
}

export class Game extends Scene {
    private characterSpriteLayers: PartLayer[] = [];
    private facingDirection = 0; // Authentic KPAH: 0: Down, 1: Up, 2: Left, 3: Right
    private walkTimer = 0;
    private idleTimer = 0;
    private isWalking = false;
    private player?: Phaser.GameObjects.Container;
    private cursors?: Phaser.Types.Input.Keyboard.CursorKeys;
    private wasd?: Record<'W' | 'A' | 'S' | 'D', Phaser.Input.Keyboard.Key>;
    private world?: WorldPosition;
    private readonly mapData: KpahMapAsset;
    private readonly characterParts: KpahCharacterPartAsset;

    // NPC list with layered sprites & idle breathing
    private npcs: NpcEntity[] = [];

    // Smooth movement & client prediction
    private localX = 0;
    private localY = 0;
    private syncedX = 0;
    private syncedY = 0;
    private syncElapsed = 0;

    // Environmental effects
    private waterRipples?: Phaser.GameObjects.Graphics;
    private rippleTime = 0;

    // Interactive Dialogue Box
    private dialogBox?: Phaser.GameObjects.Container;

    constructor() {
        super('Game');
        this.mapData = {} as KpahMapAsset;
        this.characterParts = {} as KpahCharacterPartAsset;
    }

    create(data?: { world?: WorldPosition }) {
        Object.assign(this.mapData, this.cache.json.get('kpah-map-105') as KpahMapAsset);
        Object.assign(this.characterParts, this.cache.json.get('kpah-character-parts') as KpahCharacterPartAsset);

        this.cameras.main.setBackgroundColor('#141f19');
        this.prepareCharacterFrames();
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
        this.updateWaterRipples(delta);
        this.idleTimer += delta;
        this.updateNpcIdle(delta);

        if (!this.world || !this.player || !this.cursors || !this.wasd) return;

        const left = this.cursors.left.isDown || this.wasd.A.isDown;
        const right = this.cursors.right.isDown || this.wasd.D.isDown;
        const up = this.cursors.up.isDown || this.wasd.W.isDown;
        const down = this.cursors.down.isDown || this.wasd.S.isDown;

        const dirX = (right ? 1 : 0) - (left ? 1 : 0);
        const dirY = (down ? 1 : 0) - (up ? 1 : 0);
        const moving = dirX !== 0 || dirY !== 0;

        if (moving) {
            // Update facing direction:
            // 0: Down (Nhìn xuống / Chính diện)
            // 1: Up (Nhìn lên / Quay lưng)
            // 2: Left (Nhìn sang trái)
            // 3: Right (Nhìn sang phải)
            if (dirX < 0) {
                this.facingDirection = 2; // Left
            } else if (dirX > 0) {
                this.facingDirection = 3; // Right
            } else if (dirY < 0) {
                this.facingDirection = 1; // Up
            } else if (dirY > 0) {
                this.facingDirection = 0; // Down
            }

            // Continuous smooth movement with collision & wall sliding
            const length = Math.hypot(dirX, dirY);
            const dtSec = delta / 1000;
            const stepDist = (MOVE_SPEED * dtSec) / length;
            const nextX = this.localX + dirX * stepDist;
            const nextY = this.localY + dirY * stepDist;

            if (this.isWalkable(nextX, nextY)) {
                this.localX = nextX;
                this.localY = nextY;
            } else if (this.isWalkable(nextX, this.localY)) {
                // Horizontal slide along obstacle
                this.localX = nextX;
            } else if (this.isWalkable(this.localX, nextY)) {
                // Vertical slide along obstacle
                this.localY = nextY;
            }

            // Update container position and Y-sorting depth
            this.player.setPosition(this.localX * MAP_SCALE, this.localY * MAP_SCALE);
            this.player.setDepth(this.localY * MAP_SCALE + 10);

            // Animate walking sprites with step-synced dx/dy
            this.updateCharacterAnimation(this.facingDirection, true, delta);
            this.isWalking = true;

            // Network position sync (throttled every 80ms)
            this.syncElapsed += delta;
            if (this.syncElapsed >= 80) {
                this.syncElapsed = 0;
                this.sendPositionSync();
            }
        } else {
            // Idle state: breathing animation & idle standing pose
            if (this.isWalking) {
                this.isWalking = false;
                this.walkTimer = 0;
                this.sendPositionSync(); // sync final stop position
            }
            this.updateCharacterAnimation(this.facingDirection, false, delta);
        }
    }

    /**
     * Slice and cache exact sprite frames for all character parts in character-parts.json
     */
    private prepareCharacterFrames() {
        if (!this.characterParts.parts) return;
        for (const part of this.characterParts.parts) {
            const textureKey = `kpah-cloth-${part.type}-${part.partId}`;
            if (!this.textures.exists(textureKey)) continue;
            const texture = this.textures.get(textureKey);

            for (let dir = 0; dir < 4; dir++) {
                const framesCount = part.x[dir]?.length ?? 0;
                for (let f = 0; f < framesCount; f++) {
                    const frameKey = `part_${part.type}_${part.partId}_d${dir}_f${f}`;
                    if (!texture.has(frameKey)) {
                        const px = part.x[dir][f];
                        const py = part.y[dir][f];
                        const pw = part.w[dir][f];
                        const ph = part.h[dir][f];
                        if (pw > 0 && ph > 0) {
                            texture.add(frameKey, 0, px, py, pw, ph);
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders authentic KPAH retro pixel-art terrain, village buildings, trees, and scenery
     */
    private drawStarterMap() {
        const cellSize = this.mapData.tileSize * MAP_SCALE;
        const width = this.mapData.width * cellSize;
        const height = this.mapData.height * cellSize;

        // Base background
        const ground = this.add.graphics();
        ground.fillStyle(0x192b20, 1).fillRect(0, 0, width, height);

        // Ground Tiles Rendering (seamless without grid borders)
        for (let index = 0; index < this.mapData.tiles.length; index++) {
            const tileId = this.mapData.tiles[index];
            const blocked = (this.mapData.collision[index] & 2) === 2;
            const tileX = (index % this.mapData.width) * cellSize;
            const tileY = Math.floor(index / this.mapData.width) * cellSize;

            this.renderTile(ground, tileId, blocked, tileX, tileY, cellSize);
        }

        // River / Pond water shimmering layer
        this.waterRipples = this.add.graphics().setDepth(2);

        // Scenery: Village Trees & Buildings from map data
        for (const [treeId, tileX, tileY] of this.mapData.trees) {
            if (tileX === 255) continue;
            this.addVillageProp(treeId, tileX, tileY, cellSize);
        }

        // Village Canopy Banyan Trees (Bóng mát cây đa làng KPAH)
        this.addVillageProp(0, 1, 1, cellSize);
        this.addVillageProp(10, 17, 2, cellSize);
        this.addVillageProp(11, 0, 16, cellSize);
        this.addVillageProp(1, 18, 16, cellSize);

        // Village NPCs (Đầy đủ nhân vật làng KPAH với hoạt ảnh idle & đối thoại)
        this.initVillageNpcs(cellSize);

        // Village Waypoint Gate at (10, 19)
        this.addVillageWaypoint(10, 19, cellSize);

        // Falling leaf atmospheric particles
        this.createAtmosphericParticles(width, height);

        // Decorative Map Title Header
        const headerContainer = this.add.container(20, 16).setDepth(999);
        const headerBg = this.add.graphics();
        headerBg.fillStyle(0x0e1713, 0.85);
        headerBg.fillRoundedRect(0, 0, 360, 32, 6);
        headerBg.lineStyle(1.5, 0xc8a867, 0.7);
        headerBg.strokeRoundedRect(0, 0, 360, 32, 6);
        headerContainer.add(headerBg);

        const titleText = this.add.text(12, 7, 'KHÍ PHÁCH ANH HÙNG · BẢN ĐỒ 105 (LÀNG HẬU ĐẬU)', {
            fontFamily: 'serif, sans-serif',
            fontSize: '12px',
            color: '#fbe2aa',
            fontStyle: 'bold',
            stroke: '#111915',
            strokeThickness: 2,
        });
        headerContainer.add(titleText);
    }

    /**
     * Renders seamless pixel-art terrain without artificial grid lines
     */
    private renderTile(g: Phaser.GameObjects.Graphics, tileId: number, blocked: boolean, x: number, y: number, size: number) {
        // Water tiles (channel stream)
        if (tileId >= 162 && tileId <= 181) {
            g.fillStyle(0x194e70, 1).fillRect(x, y, size, size);
            g.fillStyle(0x226692, 0.6).fillRect(x, y + 2, size, size - 4);
            if (tileId >= 162 && tileId <= 166) {
                // Bank edge transition
                g.fillStyle(0x4a5d38, 0.9).fillRect(x, y, size, 4);
            }
            return;
        }

        // Road / dirt path tiles
        const isRoad = [23, 26, 27, 54, 55, 56, 57, 58, 59, 62, 70, 71, 78, 79, 83, 84, 85, 86].includes(tileId);
        if (isRoad) {
            // Warm sandy dirt road (seamless)
            g.fillStyle(0xb59762, 1).fillRect(x, y, size, size);
            g.fillStyle(0xc5a975, 0.45).fillRect(x + 2, y + 2, size - 4, size - 4);
            // Subtle road pebbles
            if ((tileId + x) % 5 === 0) {
                g.fillStyle(0x8a6e42, 0.55).fillRect(x + 6, y + 8, 3, 2);
            }
            if ((tileId + y) % 7 === 0) {
                g.fillStyle(0x8a6e42, 0.55).fillRect(x + 18, y + 16, 4, 3);
            }
            return;
        }

        // Blocked boundary cliffs / rock walls
        if (blocked) {
            g.fillStyle(0x263a2c, 1).fillRect(x, y, size, size);
            g.fillStyle(0x314a38, 0.5).fillRect(x, y, size, 6);
            return;
        }

        // Lush grass field (Village meadow)
        const grassHues = [0x3a6140, 0x406a46, 0x365a3c, 0x44704b];
        const baseColor = grassHues[(tileId + Math.floor(x / size) + Math.floor(y / size)) % grassHues.length];
        g.fillStyle(baseColor, 1).fillRect(x, y, size, size);

        // Micro grass blades
        g.fillStyle(0x528259, 0.55);
        g.fillRect(x + 4, y + 8, 2, 4);
        g.fillRect(x + 18, y + 18, 2, 4);

        // Countryside wildflowers
        if ((tileId + x) % 11 === 0) {
            g.fillStyle(0xffe27a, 0.9).fillRect(x + 12, y + 10, 3, 3);
        } else if ((tileId + y) % 13 === 0) {
            g.fillStyle(0xffffff, 0.85).fillRect(x + 20, y + 8, 2, 2);
        }
    }

    /**
     * Water ripple animation update
     */
    private updateWaterRipples(delta: number) {
        if (!this.waterRipples) return;
        this.rippleTime += delta * 0.003;
        this.waterRipples.clear();

        const cellSize = this.mapData.tileSize * MAP_SCALE;
        for (let index = 0; index < this.mapData.tiles.length; index++) {
            const tileId = this.mapData.tiles[index];
            if (tileId >= 162 && tileId <= 181) {
                const x = (index % this.mapData.width) * cellSize;
                const y = Math.floor(index / this.mapData.width) * cellSize;
                const offset = Math.sin(this.rippleTime + (x * 0.05)) * 3;
                this.waterRipples.fillStyle(0x6ad0ff, 0.35);
                this.waterRipples.fillRect(x + 6 + offset, y + 10, 12, 2);
                this.waterRipples.fillRect(x + 14 - offset, y + 20, 10, 2);
            }
        }
    }

    /**
     * Adds an authentic village prop (house, well, fence, tree) with shadow and depth
     */
    private addVillageProp(treeId: number, tileX: number, tileY: number, cellSize: number) {
        const textureKey = `kpah-tree-${treeId}`;
        if (!this.textures.exists(textureKey)) return;

        const posX = (tileX + 0.5) * cellSize;
        const posY = (tileY + 1) * cellSize;

        // Realistic soft drop shadow
        const shadow = this.add.ellipse(posX, posY - 2, 36 * MAP_SCALE, 12 * MAP_SCALE, 0x07110c, 0.45)
            .setDepth(posY - 1);

        // Prop sprite
        const prop = this.add.image(posX, posY, textureKey)
            .setOrigin(0.5, 1)
            .setScale(MAP_SCALE)
            .setDepth(posY);

        // Sway animation for foliage/banyan trees
        if ([0, 1, 10, 11].includes(treeId)) {
            shadow.setScale(1.2, 1);
            this.tweens.add({
                targets: prop,
                angle: { from: -0.8, to: 0.8 },
                duration: 2600 + treeId * 300,
                yoyo: true,
                repeat: -1,
                ease: 'Sine.easeInOut'
            });
        }
    }

    /**
     * Initializes all standard village NPCs with complete human sprites & interactive dialogue
     */
    private initVillageNpcs(cellSize: number) {
        this.npcs = [];

        // 1. Trưởng Làng Hậu Đậu (Elder NPC - Nhiệm Vụ Tân Thủ) at (9, 9)
        this.createVillageNpc(
            'Trưởng Làng Hậu Đậu',
            '[ Nhiệm vụ ]',
            9, 9, 2, cellSize, true,
            'Chào mừng hào kiệt đến với Làng Hậu Đậu! Hãy ghé thăm Thợ Rèn để trang bị vũ khí, mua bình hồi phục từ Dì Út trước khi tiến vào cổng làng ra ngoại thành diệt quái trừ gian.'
        );

        // 2. Thợ Rèn Thiết Bị (Blacksmith) at (13, 9) beside weapon rack
        this.createVillageNpc(
            'Thợ Rèn Thiết Bị',
            '[ Rèn đúc ]',
            13, 9, 0, cellSize, false,
            'Binh khí ngũ đại phái kiếm, đao, trượng, cung, búa đều do bổn tiệm rèn đúc sắc bén phi thường! Cần chế tạo vũ khí gì cứ đến tìm ta.'
        );

        // 3. Dì Út Dược Sĩ (Potion Merchant) at (7, 9) beside medicine jar
        this.createVillageNpc(
            'Dì Út (Dược Phẩm)',
            '[ Bình máu / Mana ]',
            7, 9, 1, cellSize, false,
            'Dược thảo gia truyền bổ khí dưỡng huyết! Thuốc đỏ hồi máu HP, thuốc xanh hồi sinh lực Mana cực nhanh cho các vị hiệp khách.'
        );

        // 4. Bà Tám Tạp Hóa (General Merchant) at (13, 11) beside market stall
        this.createVillageNpc(
            'Bà Tám (Tạp Hóa)',
            '[ Cửa hàng tạp hóa ]',
            13, 11, 3, cellSize, false,
            'Cửa hàng ta có đủ ngọc bội, túi càn khôn, phù về thành và các vật phẩm hữu ích cho tân thủ giang hồ.'
        );

        // 5. Xa Phu Làng (Coachman) at (13, 14) beside horse
        this.createVillageNpc(
            'Xa Phu (Ngựa Xe)',
            '[ Dịch chuyển ]',
            13, 14, 0, cellSize, false,
            'Chiến mã câu dặm trường! Hào kiệt có muốn cưỡi ngựa nhanh chóng di chuyển đến các đại thành thị Đông Sơn, Nam Sơn hay Bắc Sơn không?'
        );
    }

    /**
     * Creates a fully dressed human NPC character with shadow, labels, and click interaction
     */
    private createVillageNpc(
        name: string,
        role: string,
        tileX: number,
        tileY: number,
        partId: number,
        cellSize: number,
        hasQuest: boolean,
        dialogue: string
    ) {
        const posX = (tileX + 0.5) * cellSize;
        const posY = (tileY + 1) * cellSize;

        const npcContainer = this.add.container(posX, posY).setDepth(posY);

        // Soft drop shadow
        const shadow = this.add.ellipse(0, 0, 22 * MAP_SCALE, 7 * MAP_SCALE, 0x050f0a, 0.45);
        npcContainer.add(shadow);

        const layers: PartLayer[] = [];

        // Full layered human body: Pants (0), Shirt (1), Head (2), Hair (3)
        for (const type of [0, 1, 2, 3]) {
            const part = this.characterParts.parts?.find((p) => p.type === type && p.partId === partId);
            if (!part) continue;

            const textureKey = `kpah-cloth-${type}-${partId}`;
            const frameKey = `part_${type}_${partId}_d0_f0`;
            const image = this.add.image(0, 0, textureKey, frameKey)
                .setOrigin(0, 0)
                .setScale(MAP_SCALE);

            const dx = (part.dx[0]?.[0] ?? 0) * MAP_SCALE;
            const dy = (part.dy[0]?.[0] ?? 0) * MAP_SCALE;
            image.setPosition(dx, dy);

            npcContainer.add(image);
            layers.push({ image, part, type, partId });
        }

        // Floating Role Subtitle
        const roleLabel = this.add.text(0, -32 * MAP_SCALE, role, {
            fontFamily: 'serif, sans-serif',
            fontSize: '10px',
            color: '#fdd835',
            fontStyle: 'bold',
            stroke: '#111914',
            strokeThickness: 2.5,
        }).setOrigin(0.5, 1);
        npcContainer.add(roleLabel);

        // Floating Name Header
        const nameLabel = this.add.text(0, -38 * MAP_SCALE, name, {
            fontFamily: 'serif, sans-serif',
            fontSize: '11px',
            color: '#ffe58f',
            fontStyle: 'bold',
            stroke: '#111914',
            strokeThickness: 3,
        }).setOrigin(0.5, 1);
        npcContainer.add(nameLabel);

        // Optional Pulsing Quest Indicator [ ? ]
        if (hasQuest) {
            const questMark = this.add.text(0, -48 * MAP_SCALE, '[ ? ]', {
                fontFamily: 'sans-serif',
                fontSize: '13px',
                color: '#ffd700',
                fontStyle: 'bold',
                stroke: '#121e17',
                strokeThickness: 3.5,
            }).setOrigin(0.5, 1);
            npcContainer.add(questMark);

            this.tweens.add({
                targets: questMark,
                y: -52 * MAP_SCALE,
                duration: 800,
                yoyo: true,
                repeat: -1,
                ease: 'Sine.easeInOut'
            });
        }

        // Click interaction for NPC Dialogue
        npcContainer.setSize(32 * MAP_SCALE, 48 * MAP_SCALE);
        npcContainer.setInteractive({ useHandCursor: true });
        npcContainer.on('pointerdown', () => {
            this.showNpcDialogue(name, role, dialogue);
        });

        this.npcs.push({ container: npcContainer, layers, name, role, dialogue });
    }

    /**
     * Updates idle breathing animation for all village NPCs
     */
    private updateNpcIdle(_delta: number) {
        const breathCycle = Math.floor(this.idleTimer / IDLE_BREATH_TICK) % 2;
        const breathOffset = breathCycle === 1 ? -1 : 0;

        for (const npc of this.npcs) {
            for (const { image, part, type } of npc.layers) {
                const baseDx = part.dx[0]?.[0] ?? 0;
                const baseDy = part.dy[0]?.[0] ?? 0;
                // Torso (1), Head (2), Hair (3) gently breathe up and down; Pants (0) remain grounded
                const dy = (type === 1 || type === 2 || type === 3) ? baseDy + breathOffset : baseDy;
                image.setPosition(baseDx * MAP_SCALE, dy * MAP_SCALE);
            }
        }
    }

    /**
     * Shows interactive retro KPAH parchment dialogue modal
     */
    private showNpcDialogue(npcName: string, role: string, content: string) {
        this.dialogBox?.destroy();

        const centerX = this.cameras.main.worldView.centerX;
        const centerY = this.cameras.main.worldView.centerY + 120;

        const box = this.add.container(centerX, centerY).setDepth(1500);

        // Background
        const bg = this.add.graphics();
        bg.fillStyle(0x0e1713, 0.94);
        bg.fillRoundedRect(-240, -75, 480, 150, 8);
        bg.lineStyle(2, 0xcda969, 0.9);
        bg.strokeRoundedRect(-240, -75, 480, 150, 8);
        box.add(bg);

        // Header Title
        const title = this.add.text(-220, -62, `${npcName} ${role}`, {
            fontFamily: 'serif, sans-serif',
            fontSize: '14px',
            color: '#fbe2aa',
            fontStyle: 'bold',
            stroke: '#101b15',
            strokeThickness: 3
        });
        box.add(title);

        // Dialogue Content
        const bodyText = this.add.text(-220, -32, content, {
            fontFamily: 'sans-serif',
            fontSize: '12px',
            color: '#e5ded1',
            wordWrap: { width: 440 },
            lineSpacing: 4
        });
        box.add(bodyText);

        // Close Button
        const closeBtn = this.add.text(210, -64, '[ X ]', {
            fontFamily: 'sans-serif',
            fontSize: '13px',
            color: '#e05d5d',
            fontStyle: 'bold'
        }).setInteractive({ useHandCursor: true });
        closeBtn.on('pointerdown', () => box.destroy());
        box.add(closeBtn);

        // Dismiss on clicking outside after short delay
        this.time.delayedCall(300, () => {
            const listener = () => {
                box.destroy();
                this.input.off('pointerdown', listener);
            };
            this.input.once('pointerdown', listener);
        });

        this.dialogBox = box;
    }

    /**
     * Adds Waypoint portal at village exit
     */
    private addVillageWaypoint(tileX: number, tileY: number, cellSize: number) {
        const posX = (tileX + 0.5) * cellSize;
        const posY = (tileY + 0.5) * cellSize;

        const wpContainer = this.add.container(posX, posY).setDepth(posY);

        // Pulsing energy circle
        const ring = this.add.ellipse(0, 0, 48, 20, 0x2cd9c5, 0.35);
        const inner = this.add.ellipse(0, 0, 32, 12, 0x7efff1, 0.65);
        wpContainer.add([ring, inner]);

        this.tweens.add({
            targets: [ring, inner],
            scaleX: 1.15,
            scaleY: 1.15,
            alpha: 0.85,
            duration: 1100,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        });

        const label = this.add.text(0, -22, 'Cổng Làng (Ra Ngoại Thành)', {
            fontFamily: 'sans-serif',
            fontSize: '11px',
            color: '#a3fbeb',
            fontStyle: 'bold',
            stroke: '#10221b',
            strokeThickness: 3
        }).setOrigin(0.5, 1);
        wpContainer.add(label);
    }

    /**
     * Ambient floating leaves across the map
     */
    private createAtmosphericParticles(mapW: number, mapH: number) {
        const particles = this.add.graphics().setDepth(800);
        const leaves: { x: number; y: number; vx: number; vy: number; rot: number; color: number }[] = [];

        for (let i = 0; i < 24; i++) {
            leaves.push({
                x: Phaser.Math.Between(0, mapW),
                y: Phaser.Math.Between(0, mapH),
                vx: Phaser.Math.FloatBetween(12, 28),
                vy: Phaser.Math.FloatBetween(8, 20),
                rot: Phaser.Math.FloatBetween(0, Math.PI * 2),
                color: [0x85bb5c, 0xa4d468, 0xd4ba55][i % 3]
            });
        }

        this.time.addEvent({
            delay: 35,
            loop: true,
            callback: () => {
                particles.clear();
                for (const leaf of leaves) {
                    leaf.x += leaf.vx * 0.035;
                    leaf.y += leaf.vy * 0.035;
                    leaf.rot += 0.03;
                    if (leaf.x > mapW) leaf.x = 0;
                    if (leaf.y > mapH) leaf.y = 0;

                    particles.fillStyle(leaf.color, 0.65);
                    particles.fillCircle(leaf.x, leaf.y, 2.5);
                }
            }
        });
    }

    /**
     * Checks if a world position (in 16px tile coordinate space) is walkable
     */
    private isWalkable(worldX: number, worldY: number): boolean {
        const radius = 5;
        const checkPoints = [
            { x: worldX - radius, y: worldY - 2 },
            { x: worldX + radius, y: worldY - 2 },
            { x: worldX - radius, y: worldY + 2 },
            { x: worldX + radius, y: worldY + 2 }
        ];

        for (const pt of checkPoints) {
            const tileX = Math.floor(pt.x / this.mapData.tileSize);
            const tileY = Math.floor(pt.y / this.mapData.tileSize);
            if (tileX < 0 || tileY < 0 || tileX >= this.mapData.width || tileY >= this.mapData.height) {
                return false;
            }
            const index = tileY * this.mapData.width + tileX;
            if ((this.mapData.collision[index] & 2) === 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates authentic KPAH player glowing feet aura circle ("Vòng tròn chân")
     */
    private createFeetCircle(): Phaser.GameObjects.Container {
        const container = this.add.container(0, 0);

        const g = this.add.graphics();

        // Radii for ground plane ellipse (elliptical perspective projection)
        const rx = 16 * MAP_SCALE; // 32px
        const ry = 7.5 * MAP_SCALE; // 15px

        // Translucent inner magic pool
        g.fillStyle(0x1ec8b5, 0.18);
        g.fillEllipse(0, 0, rx * 2, ry * 2);

        // Ambient outer glow
        g.lineStyle(3, 0x14a090, 0.35);
        g.strokeEllipse(0, 0, (rx + 2) * 2, (ry + 1.5) * 2);

        // Core sharp glowing stroke
        g.lineStyle(1.8, 0x38f5e0, 0.95);
        g.strokeEllipse(0, 0, rx * 2, ry * 2);

        // Inner highlight ring
        g.lineStyle(1, 0x7dfffa, 0.55);
        g.strokeEllipse(0, 0, (rx - 4) * 2, (ry - 2.5) * 2);

        // 4 cardinal accent notch markers (distinctive KPAH character halo)
        const markColor = 0xfff6cf;
        g.fillStyle(markColor, 0.95);
        // West notch
        g.fillRect(-rx - 2, -1.5, 4, 3);
        // East notch
        g.fillRect(rx - 2, -1.5, 4, 3);
        // North notch
        g.fillRect(-1.5, -ry - 2, 3, 4);
        // South notch
        g.fillRect(-1.5, ry - 2, 3, 4);

        container.add(g);

        // Smooth breathing / pulsing aura tween
        this.tweens.add({
            targets: container,
            scaleX: { from: 0.95, to: 1.06 },
            scaleY: { from: 0.95, to: 1.06 },
            alpha: { from: 0.75, to: 1 },
            duration: 900,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        });

        return container;
    }

    /**
     * Spawns character in the game world with layered clothing and floating HUD
     */
    private enterWorld(world: WorldPosition) {
        this.world = world;
        this.localX = world.x;
        this.localY = world.y;
        this.syncedX = world.x;
        this.syncedY = world.y;
        this.facingDirection = 0;
        this.walkTimer = 0;
        this.idleTimer = 0;
        this.isWalking = false;

        this.player?.destroy();
        this.characterSpriteLayers = [];

        // Base shadow at feet
        const shadow = this.add.ellipse(0, 0, 22 * MAP_SCALE, 7 * MAP_SCALE, 0x050f0a, 0.45);
        // Authentic KPAH glowing feet aura circle ("Vòng tròn ở chân")
        const feetCircle = this.createFeetCircle();
        const layers: Phaser.GameObjects.GameObject[] = [shadow, feetCircle];

        // KPAH starter clothing: gender 1: Nam (partId 0), gender 2: Nữ (partId 1)
        const partId = world.gender === 2 ? 1 : 0;

        // Layer order: 0: Pants, 1: Shirt, 2: Head, 3: Hair
        for (const type of [0, 1, 2, 3]) {
            const part = this.characterParts.parts.find((p) => p.type === type && p.partId === partId);
            if (!part) continue;

            const textureKey = `kpah-cloth-${type}-${partId}`;
            const frameKey = `part_${type}_${partId}_d0_f0`;
            const image = this.add.image(0, 0, textureKey, frameKey)
                .setOrigin(0, 0)
                .setScale(MAP_SCALE);

            layers.push(image);
            this.characterSpriteLayers.push({ image, part, type, partId });
        }

        // Floating Mini HP Bar snugly above head
        const barW = 32 * MAP_SCALE;
        const barH = 3.5 * MAP_SCALE;
        const hpBarBg = this.add.rectangle(0, -33 * MAP_SCALE, barW, barH, 0x141f19, 0.9)
            .setStrokeStyle(1, 0x2e4638);
        const hpBarFill = this.add.rectangle(-barW / 2 + 1, -33 * MAP_SCALE, barW - 2, barH - 2, 0xe03b3b, 1)
            .setOrigin(0, 0.5);
        layers.push(hpBarBg, hpBarFill);

        // Floating Character Name & Class Header snugly above HP bar
        const nameText = `${world.characterName} [${world.className}]`;
        const nameLabel = this.add.text(0, -38 * MAP_SCALE, nameText, {
            fontFamily: 'serif, sans-serif',
            fontSize: '11px',
            color: '#fff3cd',
            fontStyle: 'bold',
            stroke: '#121e17',
            strokeThickness: 3.5,
        }).setOrigin(0.5, 1);
        layers.push(nameLabel);

        // Player Container
        this.player = this.add.container(this.localX * MAP_SCALE, this.localY * MAP_SCALE, layers)
            .setDepth(this.localY * MAP_SCALE + 10);

        // Initial idle pose
        this.updateCharacterAnimation(0, false, 0);

        // Camera Setup with smooth follow lerp
        const mapPixelWidth = this.mapData.width * this.mapData.tileSize * MAP_SCALE;
        const mapPixelHeight = this.mapData.height * this.mapData.tileSize * MAP_SCALE;
        this.cameras.main.setBounds(0, 0, mapPixelWidth, mapPixelHeight);
        this.cameras.main.startFollow(this.player, true, 0.08, 0.08);
    }

    /**
     * Updates character sprite frames and micro-offsets for 4-direction movement & idle breathing
     */
    private updateCharacterAnimation(dir: number, isWalking: boolean, delta: number) {
        if (this.characterSpriteLayers.length === 0) return;

        let stepIndex = 0;
        let walkFrameIdx = 0;

        if (isWalking) {
            this.walkTimer += delta;
            stepIndex = Math.floor(this.walkTimer / 110) % 6;
            walkFrameIdx = STEP_TO_FRAME[stepIndex];
        }

        // Idle gentle breathing offset (1 pixel vertical breath on chest & head)
        const breathCycle = Math.floor(this.idleTimer / IDLE_BREATH_TICK) % 2;
        const breathOffset = (!isWalking && breathCycle === 1) ? -1 : 0;

        for (const { image, part, type, partId } of this.characterSpriteLayers) {
            // Parts 0 & 1 (pants, shirt) have 5 frames; Parts 2 & 3 (head, hair) have 1 frame
            const frameIdx = part.x[dir]?.length > 1 && isWalking ? walkFrameIdx : 0;
            const frameKey = `part_${type}_${partId}_d${dir}_f${frameIdx}`;

            const texture = this.textures.get(`kpah-cloth-${type}-${partId}`);
            if (texture?.has(frameKey)) {
                image.setFrame(frameKey);
            }

            // Authentic KPAH dx/dy offsets relative to character feet anchor
            const baseDx = part.dx[dir]?.[stepIndex] ?? 0;
            const baseDy = part.dy[dir]?.[stepIndex] ?? 0;

            // Apply idle breathing to torso & head when standing still
            const idleDy = (!isWalking && (type === 1 || type === 2 || type === 3))
                ? baseDy + breathOffset
                : baseDy;

            image.setPosition(baseDx * MAP_SCALE, idleDy * MAP_SCALE);
        }
    }

    /**
     * Sends delta movement packets to server clamped to MAX_MOVE_STEP (24)
     */
    private sendPositionSync() {
        const dx = Math.round(this.localX - this.syncedX);
        const dy = Math.round(this.localY - this.syncedY);
        if (dx === 0 && dy === 0) return;

        // Clamp to server's MAX_MOVE_STEP
        const clampedDx = Phaser.Math.Clamp(dx, -24, 24);
        const clampedDy = Phaser.Math.Clamp(dy, -24, 24);

        this.syncedX += clampedDx;
        this.syncedY += clampedDy;
        EventBus.emit('player-move', { dx: clampedDx, dy: clampedDy });
    }

    /**
     * Reconciles server position updates smoothly without teleport stutter
     */
    private applyPosition(position: WorldPosition) {
        if (!this.world || position.characterId !== this.world.characterId || !this.player) return;

        const distance = Math.hypot(position.x - this.localX, position.y - this.localY);
        if (distance > 28) {
            // Significant desync or server teleport: snap to authoritative server position
            this.localX = position.x;
            this.localY = position.y;
            this.syncedX = position.x;
            this.syncedY = position.y;
            this.player.setPosition(this.localX * MAP_SCALE, this.localY * MAP_SCALE);
            this.player.setDepth(this.localY * MAP_SCALE + 10);
        } else {
            // Normal movement update: acknowledge server baseline without jarring snap
            this.syncedX = position.x;
            this.syncedY = position.y;
        }
    }

    private leaveWorld() {
        this.world = undefined;
        this.player?.destroy();
        this.player = undefined;
        this.characterSpriteLayers = [];
        this.npcs = [];
        this.dialogBox?.destroy();
        this.cameras.main.stopFollow();
        this.cameras.main.setScroll(0, 0);
    }
}
