<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import PhaserGame from './PhaserGame.vue';
import { gameAssetUrl } from './config/GameAssets';
import { EventBus } from './game/EventBus';
import { GameSocket, type ConnectionState } from './net/GameSocket';

interface CharacterItemProfile {
    id: number;
    templateId: number;
    name: string;
    slot: string;
    spriteStyle: number;
    iconId: number;
    genderType: string;
    requiredLevel: number;
    equipped: boolean;
}

interface CharacterProfile {
    id: number;
    name: string;
    classId: number;
    className: string;
    gender: number;
    headId: number;
    nationId: number;
    nationName: string;
    elementId: number;
    elementName: string;
    level: number;
    strength: number;
    agility: number;
    spirit: number;
    health: number;
    luck: number;
    basePoints: number;
    skillPoints: number;
    exp: number;
    hp: number;
    maxHp: number;
    mp: number;
    maxMp: number;
    xu: number;
    luong: number;
    luongKhoa: number;
    items: CharacterItemProfile[];
}

interface AccountProfile {
    id: number;
    username: string;
    characters: CharacterProfile[];
}

interface WorldPosition {
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

interface ServerMessage {
    type?: string;
    message?: string;
    account?: AccountProfile;
    character?: CharacterProfile;
    world?: WorldPosition;
    position?: WorldPosition;
}

const KPAH_CLASSES = [
    {
        id: 0,
        name: 'Kiếm Khách',
        element: 'Thủy',
        weapon: 'Kiếm',
        description: 'Thân pháp phiêu dật, kiếm khí sắc bén, né tránh và chí mạng xuất chúng.',
        stats: 'Sức mạnh: 25 · Nhanh nhẹn: 20 · Thể lực: 25',
        icon: '⚔️'
    },
    {
        id: 1,
        name: 'Chiến Binh',
        element: 'Kim',
        weapon: 'Đao',
        description: 'Cận chiến dũng mãnh, đao pháp cương mãnh, ngoại công và sức chịu đựng cực cao.',
        stats: 'Sức mạnh: 30 · Nhanh nhẹn: 20 · Thể lực: 20',
        icon: '🪓'
    },
    {
        id: 2,
        name: 'Pháp Sư',
        element: 'Hỏa',
        weapon: 'Bút / Trượng',
        description: 'Tấn công tầm xa uy lực, nội công thâm hậu, chế ngự ngũ hành hỏa diệm.',
        stats: 'Sức mạnh: 10 · Nhanh nhẹn: 25 · Tinh thần: 35',
        icon: '🪄'
    },
    {
        id: 3,
        name: 'Đấu Sĩ',
        element: 'Thổ',
        weapon: 'Búa / Chùy',
        description: 'Sinh lực dồi dào, phòng ngự vững chắc như bàn thạch, sinh ra để xông pha trận mạc.',
        stats: 'Sức mạnh: 20 · Nhanh nhẹn: 30 · Thể lực: 20',
        icon: '🔨'
    },
    {
        id: 4,
        name: 'Cung Thủ',
        element: 'Mộc',
        weapon: 'Cung',
        description: 'Bách bộ xuyên dương, tốc độ xạ tiễn cực nhanh, linh hoạt nơi chiến trường.',
        stats: 'Sức mạnh: 20 · Nhanh nhẹn: 30 · Thể lực: 15',
        icon: '🏹'
    }
];

const KPAH_NATIONS = [
    { id: 0, name: 'Đông Sơn', icon: '🏯', title: 'Hùng vĩ trấn đông' },
    { id: 1, name: 'Nam Sơn', icon: '🏔️', title: 'Núi non hiểm trở' },
    { id: 2, name: 'Bắc Sơn', icon: '🌲', title: 'Rừng thiêng cổ kính' }
];

const socketUrl = import.meta.env.VITE_GAME_WS_URL || 'ws://localhost:19130/game';
const gameSocket = new GameSocket(socketUrl);

const connectionState = ref<ConnectionState>('disconnected');
const authMode = ref<'login' | 'register'>('login');
const username = ref('');
const password = ref('');
const account = ref<AccountProfile | null>(null);
const characters = ref<CharacterProfile[]>([]);
const selectedCharacter = ref<CharacterProfile | null>(null);
const enteredWorld = ref<WorldPosition | null>(null);

// Form tạo nhân vật
const showCreateForm = ref(false);
const characterName = ref('');
const selectedClassId = ref(0);
const selectedGender = ref(1); // 1: Nam, 2: Nữ
const selectedNationId = ref(0); // 0: Đông Sơn, 1: Nam Sơn, 2: Bắc Sơn

const notice = ref('Đang kết nối máy chủ...');
const busy = ref(false);
let requestSequence = 0;

const isConnected = computed(() => connectionState.value === 'connected');
const requestId = () => `r${Date.now()}-${++requestSequence}`;

const equippedItems = computed(() => {
    return selectedCharacter.value?.items.filter(i => i.equipped) || [];
});

const bagItems = computed(() => {
    return selectedCharacter.value?.items.filter(i => !i.equipped) || [];
});

const enterCharacter = (character: CharacterProfile) => {
    if (!isConnected.value || busy.value) return;
    selectedCharacter.value = character;
    busy.value = true;
    notice.value = `Đang vào Bản đồ 105 cùng ${character.name}...`;
    if (!gameSocket.send({ type: 'character.enter', requestId: requestId(), characterId: character.id })) {
        busy.value = false;
        notice.value = 'Mất kết nối tới máy chủ. Vui lòng thử lại.';
    }
};

const sendMove = (movement: { dx: number; dy: number }) => {
    if (!enteredWorld.value || !isConnected.value) return;
    gameSocket.send({ type: 'player.move', dx: movement.dx, dy: movement.dy });
};

const equipItem = (item: CharacterItemProfile) => {
    if (!selectedCharacter.value || !isConnected.value || busy.value) return;
    busy.value = true;
    notice.value = `Đang mặc ${item.name}...`;
    gameSocket.send({
        type: 'character.equip',
        requestId: requestId(),
        characterId: selectedCharacter.value.id,
        itemId: item.id
    });
};

const submitAuth = () => {
    if (!username.value.trim() || !password.value || !isConnected.value || busy.value) return;
    busy.value = true;
    notice.value = authMode.value === 'login' ? 'Đang xác thực tài khoản...' : 'Đang đăng ký tài khoản mới...';
    gameSocket.send({
        type: authMode.value === 'login' ? 'auth.login' : 'auth.register',
        requestId: requestId(),
        username: username.value.trim(),
        password: password.value
    });
};

const submitCreateCharacter = () => {
    if (!characterName.value.trim() || !isConnected.value || busy.value) return;
    busy.value = true;
    notice.value = 'Đang khởi tạo nhân vật KPAH...';
    gameSocket.send({
        type: 'character.create',
        requestId: requestId(),
        name: characterName.value.trim(),
        classId: selectedClassId.value,
        gender: selectedGender.value,
        headId: 0,
        nationId: selectedNationId.value
    });
};

const logout = () => {
    gameSocket.send({ type: 'auth.logout', requestId: requestId() });
    account.value = null;
    characters.value = [];
    selectedCharacter.value = null;
    enteredWorld.value = null;
    showCreateForm.value = false;
    EventBus.emit('world-leave');
    notice.value = 'Đã đăng xuất khỏi máy chủ.';
};

const leaveGameToLobby = () => {
    enteredWorld.value = null;
    EventBus.emit('world-leave');
    notice.value = 'Đã trở về sảnh chọn nhân vật.';
};

onMounted(() => {
    gameSocket.connect(
        (state) => {
            connectionState.value = state;
            if (state === 'connected') {
                notice.value = 'Máy chủ KPAH sẵn sàng. Mời đăng nhập.';
            } else if (state === 'connecting') {
                notice.value = 'Đang kết nối WebSocket...';
            } else {
                notice.value = 'Mất kết nối máy chủ.';
            }
        },
        (message: string) => {
            busy.value = false;
            let response: ServerMessage;
            try {
                response = JSON.parse(message) as ServerMessage;
            } catch {
                return;
            }

            if (response.type === 'error') {
                notice.value = response.message || 'Thao tác không thành công.';
                return;
            }

            if (response.type === 'auth.registered' || response.type === 'auth.success') {
                if (response.account) {
                    account.value = response.account;
                    characters.value = response.account.characters || [];
                    notice.value = `Xin chào ${response.account.username}!`;
                    if (characters.value.length === 0) {
                        showCreateForm.value = true;
                    } else {
                        showCreateForm.value = false;
                        selectedCharacter.value = characters.value[0];
                    }
                }
                return;
            }

            if (response.type === 'character.created' && response.character) {
                characters.value.push(response.character);
                selectedCharacter.value = response.character;
                showCreateForm.value = false;
                characterName.value = '';
                notice.value = `Nhân vật ${response.character.name} (${response.character.className}) đã khởi tạo thành công!`;
                return;
            }

            if (response.type === 'character.entered' && response.world) {
                enteredWorld.value = response.world;
                notice.value = `Đã vào ${response.world.mapName} tại tọa độ (${response.world.x}, ${response.world.y}).`;
                EventBus.emit('world-enter', response.world);
                return;
            }

            if (response.type === 'player.moved' && response.position) {
                if (enteredWorld.value) {
                    enteredWorld.value.x = response.position.x;
                    enteredWorld.value.y = response.position.y;
                }
                EventBus.emit('world-position', response.position);
                return;
            }

            if (response.type === 'character.updated' && response.character) {
                selectedCharacter.value = response.character;
                const idx = characters.value.findIndex(c => c.id === response.character!.id);
                if (idx >= 0) {
                    characters.value[idx] = response.character;
                }
                notice.value = 'Cập nhật trang bị thành công.';
                return;
            }
        }
    );

    EventBus.on('player-move', sendMove);
});

onUnmounted(() => {
    EventBus.off('player-move', sendMove);
    gameSocket.disconnect();
});
</script>

<template>
    <div class="game-wrapper">
        <PhaserGame />

        <!-- LỚP GIAO DIỆN (UI OVERLAY) -->
        <div class="ui-layer">
            <!-- THANH TRẠNG THÁI TRÊN CÙNG -->
            <header class="top-nav">
                <div class="brand">
                    <span class="game-badge">KPAH H5</span>
                    <h1>Khí Phách Anh Hùng</h1>
                </div>
                <div class="sys-status">
                    <span class="connection-indicator" :class="connectionState"></span>
                    <span class="notice-text">{{ notice }}</span>
                    <button v-if="account" class="btn-logout" @click="logout">Đăng xuất</button>
                </div>
            </header>

            <!-- 1. MÀN HÌNH ĐĂNG NHẬP / ĐĂNG KÝ -->
            <div v-if="!account" class="center-modal">
                <div class="panel-box auth-panel">
                    <div class="panel-header">
                        <h2>{{ authMode === 'login' ? 'Đăng Nhập Máy Chủ' : 'Tạo Tài Khoản Mới' }}</h2>
                        <div class="tabs">
                            <button :class="{ active: authMode === 'login' }" @click="authMode = 'login'">Đăng nhập</button>
                            <button :class="{ active: authMode === 'register' }" @click="authMode = 'register'">Đăng ký</button>
                        </div>
                    </div>
                    <form @submit.prevent="submitAuth" class="auth-form">
                        <label>
                            <span>Tài khoản:</span>
                            <input v-model="username" type="text" placeholder="Tên tài khoản (3-24 ký tự)" required />
                        </label>
                        <label>
                            <span>Mật khẩu:</span>
                            <input v-model="password" type="password" placeholder="Mật khẩu (tối thiểu 8 ký tự)" required />
                        </label>
                        <button type="submit" class="btn-primary" :disabled="busy || !isConnected">
                            {{ busy ? 'Đang xử lý...' : (authMode === 'login' ? 'Vào Game' : 'Đăng Ký Ngay') }}
                        </button>
                    </form>
                </div>
            </div>

            <!-- 2. MÀN HÌNH CHỌN & TẠO NHÂN VẬT KPAH -->
            <div v-else-if="!enteredWorld" class="center-modal wide-modal">
                <!-- 2.1 DANH SÁCH 3 SLOT NHÂN VẬT -->
                <div v-if="!showCreateForm" class="panel-box char-select-panel">
                    <div class="panel-header">
                        <h2>Danh Sách Nhân Vật ({{ characters.length }}/3)</h2>
                        <button v-if="characters.length < 3" class="btn-secondary" @click="showCreateForm = true">
                            + Tạo Nhân Vật Mới
                        </button>
                    </div>

                    <div class="char-slots-grid">
                        <div
                            v-for="char in characters"
                            :key="char.id"
                            class="char-card"
                            :class="{ active: selectedCharacter?.id === char.id }"
                            @click="selectedCharacter = char"
                        >
                            <div class="char-header">
                                <span class="char-class-badge">{{ char.className }}</span>
                                <span class="char-element-badge">{{ char.elementName }}</span>
                            </div>
                            <div class="char-body">
                                <h3>{{ char.name }}</h3>
                                <p class="char-level">Cấp độ: <strong>{{ char.level }}</strong></p>
                                <p class="char-nation">Quốc gia: <strong>{{ char.nationName }}</strong> ({{ char.gender === 1 ? 'Nam' : 'Nữ' }})</p>
                                <div class="char-stats">
                                    <span>HP: {{ char.hp }}/{{ char.maxHp }}</span>
                                    <span>Xu: {{ char.xu.toLocaleString() }}</span>
                                </div>
                            </div>
                            <div class="char-equip-preview">
                                <div v-for="item in char.items.filter(i => i.equipped)" :key="item.id" class="equip-badge" :title="item.name">
                                    <img :src="gameAssetUrl('kpah/item-icons/' + item.iconId + '.png')" :alt="item.name" />
                                </div>
                            </div>
                            <button class="btn-enter" @click.stop="enterCharacter(char)" :disabled="busy">
                                Vào Game
                            </button>
                        </div>

                        <!-- SLOT TRỐNG -->
                        <div v-for="n in (3 - characters.length)" :key="'empty-' + n" class="char-card empty-card" @click="showCreateForm = true">
                            <span class="plus-icon">+</span>
                            <p>Tạo nhân vật mới</p>
                        </div>
                    </div>
                </div>

                <!-- 2.2 FORM TẠO NHÂN VẬT MỚI (5 PHÁI, 3 QUỐC GIA) -->
                <div v-else class="panel-box char-create-panel">
                    <div class="panel-header">
                        <h2>Tạo Nhân Vật Khí Phách Anh Hùng</h2>
                        <button class="btn-secondary" @click="showCreateForm = false">Quay lại</button>
                    </div>

                    <form @submit.prevent="submitCreateCharacter" class="create-form">
                        <!-- TÊN & GIỚI TÍNH -->
                        <div class="form-row">
                            <label class="name-input-group">
                                <span>Tên nhân vật:</span>
                                <input v-model="characterName" type="text" placeholder="Đặt tên (3–18 ký tự)" required />
                            </label>
                            <div class="gender-group">
                                <span>Giới tính:</span>
                                <div class="gender-options">
                                    <button type="button" :class="{ selected: selectedGender === 1 }" @click="selectedGender = 1">
                                        👨 Nam
                                    </button>
                                    <button type="button" :class="{ selected: selectedGender === 2 }" @click="selectedGender = 2">
                                        👩 Nữ
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- 5 MÔN PHÁI KPAH -->
                        <div class="class-selection-section">
                            <h3>Chọn Môn Phái:</h3>
                            <div class="class-cards-grid">
                                <div
                                    v-for="cls in KPAH_CLASSES"
                                    :key="cls.id"
                                    class="class-card"
                                    :class="{ selected: selectedClassId === cls.id }"
                                    @click="selectedClassId = cls.id"
                                >
                                    <div class="class-icon">{{ cls.icon }}</div>
                                    <h4>{{ cls.name }}</h4>
                                    <span class="element-tag">{{ cls.element }} · {{ cls.weapon }}</span>
                                    <p class="desc">{{ cls.description }}</p>
                                    <small class="stats-preview">{{ cls.stats }}</small>
                                </div>
                            </div>
                        </div>

                        <!-- 3 QUỐC GIA KPAH -->
                        <div class="nation-selection-section">
                            <h3>Chọn Quốc Gia:</h3>
                            <div class="nation-cards-grid">
                                <div
                                    v-for="nat in KPAH_NATIONS"
                                    :key="nat.id"
                                    class="nation-card"
                                    :class="{ selected: selectedNationId === nat.id }"
                                    @click="selectedNationId = nat.id"
                                >
                                    <span class="nation-icon">{{ nat.icon }}</span>
                                    <h4>{{ nat.name }}</h4>
                                    <small>{{ nat.title }}</small>
                                </div>
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn-primary btn-large" :disabled="busy || !characterName.trim()">
                                {{ busy ? 'Đang tạo...' : 'Xác Nhận Tạo Nhân Vật' }}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- 3. IN-GAME HUD KHI ĐÃ VÀO THẾ GIỚI -->
            <div v-else class="ingame-hud">
                <!-- THANH NHÂN VẬT GÓC TRÊN TRÁI -->
                <div class="player-hud-card">
                    <div class="hud-top">
                        <span class="player-name">{{ enteredWorld.characterName }}</span>
                        <span class="player-class-tag">{{ enteredWorld.className }}</span>
                        <span class="player-nation-tag">{{ enteredWorld.gender === 1 ? 'Nam' : 'Nữ' }}</span>
                    </div>
                    <div class="hud-bars">
                        <div class="bar-container hp-bar">
                            <div class="bar-fill" :style="{ width: '100%' }"></div>
                            <span class="bar-text">HP: {{ selectedCharacter?.hp || 100 }}/{{ selectedCharacter?.maxHp || 100 }}</span>
                        </div>
                        <div class="bar-container mp-bar">
                            <div class="bar-fill" :style="{ width: '100%' }"></div>
                            <span class="bar-text">MP: {{ selectedCharacter?.mp || 100 }}/{{ selectedCharacter?.maxMp || 100 }}</span>
                        </div>
                    </div>
                    <div class="hud-coords">
                        <span>📍 {{ enteredWorld.mapName }} ({{ enteredWorld.x }}, {{ enteredWorld.y }})</span>
                        <span class="xu-val">🪙 {{ selectedCharacter?.xu.toLocaleString() || '10,000' }} xu</span>
                    </div>
                </div>

                <!-- BẢNG HÀNH TRANG & TRANG BỊ GÓC DƯỚI PHẢI -->
                <div class="inventory-hud-card">
                    <h4>Trang Bị Đang Mặc</h4>
                    <div class="equip-slots-row">
                        <div v-for="item in equippedItems" :key="item.id" class="equip-slot-box" :title="item.name">
                            <img :src="gameAssetUrl('kpah/item-icons/' + item.iconId + '.png')" :alt="item.name" />
                            <span class="slot-tag">{{ item.slot }}</span>
                        </div>
                    </div>

                    <div v-if="bagItems.length > 0" class="bag-section">
                        <h4>Hành Trang Dự Phòng</h4>
                        <div class="bag-slots-row">
                            <div v-for="item in bagItems" :key="item.id" class="bag-item-box" @click="equipItem(item)" :title="'Nhấn để mặc ' + item.name">
                                <img :src="gameAssetUrl('kpah/item-icons/' + item.iconId + '.png')" :alt="item.name" />
                                <span class="action-tag">Mặc</span>
                            </div>
                        </div>
                    </div>

                    <div class="hud-controls">
                        <small>Điều khiển: <strong>WASD</strong> hoặc <strong>Phím Mũi Tên</strong></small>
                        <button class="btn-lobby" @click="leaveGameToLobby">Rời Game</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.game-wrapper {
    position: relative;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    background-color: #0b1410;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    color: #f3efe6;
    user-select: none;
}

.ui-layer {
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 10;
    display: flex;
    flex-direction: column;
}

.ui-layer * {
    pointer-events: auto;
}

/* TOP NAV */
.top-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 20px;
    background: linear-gradient(180deg, rgba(11, 20, 16, 0.95) 0%, rgba(11, 20, 16, 0.6) 80%, transparent 100%);
}

.brand {
    display: flex;
    align-items: center;
    gap: 10px;
}

.game-badge {
    background: #d4a017;
    color: #17231e;
    font-weight: 900;
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 4px;
    letter-spacing: 0.5px;
}

.brand h1 {
    margin: 0;
    font-size: 18px;
    color: #faebd7;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
}

.sys-status {
    display: flex;
    align-items: center;
    gap: 12px;
}

.connection-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #e74c3c;
}

.connection-indicator.connected {
    background: #2ecc71;
    box-shadow: 0 0 8px #2ecc71;
}

.notice-text {
    font-size: 13px;
    color: #e0d5c1;
}

.btn-logout {
    background: #34495e;
    color: #fff;
    border: none;
    padding: 4px 10px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
}

/* MODALS */
.center-modal {
    margin: auto;
    display: flex;
    justify-content: center;
    align-items: center;
}

.wide-modal {
    max-width: 960px;
    width: 90%;
}

.panel-box {
    background: rgba(18, 29, 24, 0.94);
    border: 1px solid rgba(212, 160, 23, 0.35);
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.7);
    border-radius: 8px;
    padding: 24px;
    color: #f7f1e5;
    backdrop-filter: blur(8px);
}

.auth-panel {
    width: 360px;
}

.panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(212, 160, 23, 0.2);
    padding-bottom: 12px;
    margin-bottom: 20px;
}

.panel-header h2 {
    margin: 0;
    font-size: 18px;
    color: #f5d76e;
}

.tabs button {
    background: transparent;
    border: none;
    color: #95a5a6;
    padding: 6px 12px;
    cursor: pointer;
    font-size: 13px;
}

.tabs button.active {
    color: #f5d76e;
    font-weight: bold;
    border-bottom: 2px solid #f5d76e;
}

.auth-form label {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 14px;
    font-size: 13px;
    color: #dcd6cd;
}

.auth-form input,
.create-form input {
    background: rgba(0, 0, 0, 0.4);
    border: 1px solid #4a6552;
    padding: 10px 12px;
    border-radius: 4px;
    color: #fff;
    font-size: 14px;
    outline: none;
}

.auth-form input:focus,
.create-form input:focus {
    border-color: #f5d76e;
}

.btn-primary {
    background: linear-gradient(180deg, #d4a017 0%, #b8860b 100%);
    color: #1a2520;
    font-weight: bold;
    border: none;
    padding: 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 15px;
    width: 100%;
    margin-top: 6px;
    box-shadow: 0 4px 12px rgba(212, 160, 23, 0.3);
}

.btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.btn-secondary {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #f5d76e;
    padding: 6px 14px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
}

/* CHAR SLOTS */
.char-slots-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
}

.char-card {
    background: rgba(10, 18, 14, 0.65);
    border: 1px solid #2e4738;
    border-radius: 6px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    cursor: pointer;
    transition: all 0.2s ease;
}

.char-card:hover,
.char-card.active {
    border-color: #f5d76e;
    background: rgba(26, 43, 34, 0.85);
    transform: translateY(-2px);
}

.char-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
}

.char-class-badge {
    background: #c0392b;
    color: #fff;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 11px;
    font-weight: bold;
}

.char-element-badge {
    background: #2980b9;
    color: #fff;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 11px;
}

.char-body h3 {
    margin: 4px 0 8px;
    font-size: 16px;
    color: #f5d76e;
}

.char-level, .char-nation {
    margin: 2px 0;
    font-size: 12px;
    color: #bdc3c7;
}

.char-stats {
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
    font-size: 11px;
    color: #a2b9aa;
    background: rgba(0, 0, 0, 0.3);
    padding: 4px 6px;
    border-radius: 4px;
}

.char-equip-preview {
    display: flex;
    gap: 6px;
    margin: 12px 0;
}

.equip-badge {
    width: 32px;
    height: 32px;
    background: rgba(0, 0, 0, 0.5);
    border: 1px solid #3d5a47;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.equip-badge img {
    width: 24px;
    height: 24px;
    image-rendering: pixelated;
}

.btn-enter {
    background: #27ae60;
    color: #fff;
    border: none;
    padding: 8px;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
    font-size: 13px;
    width: 100%;
}

.empty-card {
    align-items: center;
    justify-content: center;
    min-height: 200px;
    border: 2px dashed #34495e;
}

.empty-card .plus-icon {
    font-size: 36px;
    color: #7f8c8d;
}

.empty-card p {
    color: #95a5a6;
    font-size: 13px;
}

/* CHAR CREATE FORM */
.char-create-panel {
    max-height: 85vh;
    overflow-y: auto;
}

.form-row {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
}

.name-input-group {
    flex: 2;
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 13px;
}

.gender-group {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 13px;
}

.gender-options {
    display: flex;
    gap: 8px;
}

.gender-options button {
    flex: 1;
    background: rgba(0, 0, 0, 0.4);
    border: 1px solid #3d5a47;
    color: #dcd6cd;
    padding: 8px;
    border-radius: 4px;
    cursor: pointer;
}

.gender-options button.selected {
    border-color: #f5d76e;
    background: rgba(212, 160, 23, 0.2);
    color: #f5d76e;
    font-weight: bold;
}

.class-selection-section h3,
.nation-selection-section h3 {
    font-size: 14px;
    color: #f5d76e;
    margin: 14px 0 8px;
}

.class-cards-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
}

.class-card {
    background: rgba(0, 0, 0, 0.35);
    border: 1px solid #334e3e;
    border-radius: 6px;
    padding: 12px 8px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
}

.class-card.selected {
    border-color: #f5d76e;
    background: rgba(212, 160, 23, 0.15);
    transform: scale(1.02);
}

.class-icon {
    font-size: 24px;
    margin-bottom: 4px;
}

.class-card h4 {
    margin: 4px 0;
    font-size: 14px;
    color: #fff;
}

.element-tag {
    font-size: 11px;
    color: #e67e22;
    font-weight: bold;
    display: block;
    margin-bottom: 6px;
}

.desc {
    font-size: 11px;
    color: #a4b0be;
    line-height: 1.3;
    margin-bottom: 6px;
}

.stats-preview {
    font-size: 10px;
    color: #7bed9f;
    display: block;
}

.nation-cards-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
}

.nation-card {
    background: rgba(0, 0, 0, 0.35);
    border: 1px solid #334e3e;
    border-radius: 6px;
    padding: 10px;
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
}

.nation-card.selected {
    border-color: #f5d76e;
    background: rgba(212, 160, 23, 0.15);
}

.nation-icon {
    font-size: 20px;
}

.nation-card h4 {
    margin: 0;
    font-size: 14px;
    color: #fff;
}

.nation-card small {
    color: #95a5a6;
    font-size: 11px;
}

.btn-large {
    padding: 14px;
    font-size: 16px;
    margin-top: 18px;
}

/* INGAME HUD */
.ingame-hud {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 12px 20px;
    height: 100%;
}

.player-hud-card {
    background: rgba(14, 24, 19, 0.9);
    border: 1px solid #3d5a47;
    border-radius: 6px;
    padding: 10px 14px;
    width: 240px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6);
}

.hud-top {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
}

.player-name {
    font-weight: bold;
    font-size: 14px;
    color: #f5d76e;
}

.player-class-tag, .player-nation-tag {
    font-size: 11px;
    padding: 1px 5px;
    border-radius: 3px;
    background: #27ae60;
    color: #fff;
}

.hud-bars {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.bar-container {
    height: 14px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 3px;
    overflow: hidden;
    position: relative;
}

.hp-bar .bar-fill {
    background: linear-gradient(90deg, #e74c3c, #c0392b);
    height: 100%;
}

.mp-bar .bar-fill {
    background: linear-gradient(90deg, #3498db, #2980b9);
    height: 100%;
}

.bar-text {
    position: absolute;
    inset: 0;
    font-size: 10px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    text-shadow: 0 1px 2px #000;
}

.hud-coords {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    color: #bdc3c7;
    margin-top: 6px;
}

.xu-val {
    color: #f1c40f;
    font-weight: bold;
}

/* INVENTORY HUD CARD */
.inventory-hud-card {
    background: rgba(14, 24, 19, 0.9);
    border: 1px solid #3d5a47;
    border-radius: 6px;
    padding: 12px;
    width: 240px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6);
    margin-top: auto;
}

.inventory-hud-card h4 {
    margin: 0 0 6px;
    font-size: 12px;
    color: #f5d76e;
}

.equip-slots-row, .bag-slots-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
}

.equip-slot-box, .bag-item-box {
    width: 36px;
    height: 36px;
    background: rgba(0, 0, 0, 0.5);
    border: 1px solid #4a6552;
    border-radius: 4px;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
}

.equip-slot-box img, .bag-item-box img {
    width: 26px;
    height: 26px;
    image-rendering: pixelated;
}

.slot-tag, .action-tag {
    position: absolute;
    bottom: -4px;
    right: -4px;
    font-size: 8px;
    background: #111;
    color: #a8b5a0;
    padding: 0 2px;
    border-radius: 2px;
}

.action-tag {
    background: #27ae60;
    color: #fff;
}

.hud-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding-top: 6px;
}

.hud-controls small {
    font-size: 10px;
    color: #95a5a6;
}

.btn-lobby {
    background: #c0392b;
    color: #fff;
    border: none;
    padding: 4px 8px;
    border-radius: 3px;
    font-size: 11px;
    cursor: pointer;
}
</style>
