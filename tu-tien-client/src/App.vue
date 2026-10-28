<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import PhaserGame from './PhaserGame.vue';
import { gameAssetUrl } from './config/GameAssets';
import { EventBus } from './game/EventBus';
import { GameSocket, type ConnectionState } from './net/GameSocket';

type CultivatorType = 'NAM_TU' | 'NU_TU';
type SpiritType = 'THANH_LONG' | 'BACH_HO' | 'HUYEN_VU' | 'CHU_TUOC';
interface CharacterItemProfile {
    id: number;
    templateId: number;
    name: string;
    slot: 'SHIRT' | 'PANTS';
    spriteStyle: number;
    iconId: number;
    genderType: CultivatorType;
    requiredLevel: number;
    equipped: boolean;
}
interface CharacterProfile {
    id: number;
    name: string;
    cultivatorType: CultivatorType;
    spiritType: SpiritType;
    realmCode: string;
    level: number;
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
    cultivatorType: CultivatorType;
    spiritType: SpiritType;
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

const socketUrl = import.meta.env.VITE_GAME_WS_URL || 'ws://localhost:19130/game';
const gameSocket = new GameSocket(socketUrl);
document.documentElement.style.setProperty('--spirit-atlas-url', `url("${gameAssetUrl('spirit-beasts.png')}")`);
document.documentElement.style.setProperty('--cultivator-atlas-url', `url("${gameAssetUrl('cultivators.png')}")`);
const connectionState = ref<ConnectionState>('disconnected');
const authMode = ref<'login' | 'register'>('login');
const username = ref('');
const password = ref('');
const characterName = ref('');
const cultivatorType = ref<CultivatorType>('NAM_TU');
const spiritType = ref<SpiritType>('THANH_LONG');
const account = ref<AccountProfile | null>(null);
const characters = ref<CharacterProfile[]>([]);
const selectedCharacter = ref<CharacterProfile | null>(null);
const enteredWorld = ref<WorldPosition | null>(null);
const notice = ref('Đang kết nối máy chủ...');
const busy = ref(false);
let requestSequence = 0;

const spiritOptions: { id: SpiritType; name: string; title: string; position: string }[] = [
    { id: 'THANH_LONG', name: 'Thanh Long', title: 'Mộc · Sinh cơ', position: '0% 0%' },
    { id: 'BACH_HO', name: 'Bạch Hổ', title: 'Kim · Sát phạt', position: '100% 0%' },
    { id: 'HUYEN_VU', name: 'Huyền Vũ', title: 'Thủy · Hộ thể', position: '0% 100%' },
    { id: 'CHU_TUOC', name: 'Chu Tước', title: 'Hỏa · Niết bàn', position: '100% 100%' },
];

const isConnected = computed(() => connectionState.value === 'connected');
const requestId = () => `r${Date.now()}-${++requestSequence}`;

const enterCharacter = (character: CharacterProfile) => {
    if (!isConnected.value || busy.value) return;
    selectedCharacter.value = character;
    busy.value = true;
    notice.value = `Đang tiến vào map đầu tiên cùng ${character.name}...`;
    if (!gameSocket.send({ type: 'character.enter', requestId: requestId(), characterId: character.id })) {
        busy.value = false;
        notice.value = 'Kết nối đã đóng. Hãy kết nối lại.';
    }
};

const sendMove = (movement: { dx: number; dy: number }) => {
    if (!enteredWorld.value || !isConnected.value) return;
    gameSocket.send({ type: 'player.move', dx: movement.dx, dy: movement.dy });
};

const leaveWorld = () => {
    enteredWorld.value = null;
    EventBus.emit('world-leave');
};

const connect = () => {
    gameSocket.connect(
        (state) => {
            connectionState.value = state;
            if (state === 'connected') notice.value = 'Đã kết nối. Đăng nhập hoặc tạo tài khoản để nhập giới.';
            if (state === 'error') notice.value = 'Không thể kết nối máy chủ. Hãy kiểm tra server và thử lại.';
            if (state === 'disconnected') {
                account.value = null;
                characters.value = [];
                selectedCharacter.value = null;
                leaveWorld();
                busy.value = false;
                notice.value = 'Đã ngắt kết nối khỏi máy chủ.';
            }
        },
        (rawMessage) => {
            let message: ServerMessage;
            try {
                message = JSON.parse(rawMessage) as ServerMessage;
            } catch {
                notice.value = 'Máy chủ gửi phản hồi không hợp lệ.';
                return;
            }
            if (message.type === 'server.ready') return;
            busy.value = false;
            if (message.type === 'auth.success' && message.account) {
                account.value = message.account;
                characters.value = message.account.characters ?? [];
                selectedCharacter.value = null;
                password.value = '';
                notice.value = `Chào mừng ${message.account.username} đến Vạn Linh Giới.`;
            } else if (message.type === 'character.created' && message.character) {
                characters.value = [...characters.value, message.character];
                selectedCharacter.value = message.character;
                characterName.value = '';
                notice.value = `Đã khai sinh nhân vật ${message.character.name}. Đang vào map đầu tiên...`;
                enterCharacter(message.character);
            } else if (message.type === 'character.entered' && message.world) {
                enteredWorld.value = message.world;
                notice.value = `${message.world.characterName} đã vào ${message.world.mapName}. Dùng WASD hoặc phím mũi tên để di chuyển.`;
                EventBus.emit('world-enter', message.world);
            } else if (message.type === 'player.moved' && message.position) {
                enteredWorld.value = message.position;
                EventBus.emit('world-position', message.position);
            } else if (message.type === 'character.updated' && message.character) {
                const updatedCharacter = message.character;
                characters.value = characters.value.map((character) =>
                    character.id === updatedCharacter.id ? updatedCharacter : character
                );
                selectedCharacter.value = updatedCharacter;
                notice.value = `Đã cập nhật trang bị cho ${updatedCharacter.name}.`;
            } else if (message.type === 'error') {
                notice.value = message.message || 'Yêu cầu chưa thể xử lý.';
            }
        }
    );
};

const submitAuth = () => {
    if (!isConnected.value || busy.value) return;
    busy.value = true;
    notice.value = authMode.value === 'register' ? 'Đang lập tài khoản...' : 'Đang đăng nhập...';
    const sent = gameSocket.send({
        type: authMode.value === 'register' ? 'auth.register' : 'auth.login',
        requestId: requestId(),
        username: username.value,
        password: password.value,
    });
    if (!sent) {
        busy.value = false;
        notice.value = 'Kết nối đã đóng. Hãy kết nối lại.';
    }
};

const submitCharacter = () => {
    if (!isConnected.value || !account.value || busy.value) return;
    busy.value = true;
    notice.value = 'Đang ghi danh nhân vật vào tiên tịch...';
    const sent = gameSocket.send({
        type: 'character.create',
        requestId: requestId(),
        name: characterName.value,
        cultivatorType: cultivatorType.value,
        spiritType: spiritType.value,
    });
    if (!sent) {
        busy.value = false;
        notice.value = 'Kết nối đã đóng. Hãy kết nối lại.';
    }
};

const equipItem = (item: CharacterItemProfile) => {
    if (!isConnected.value || !selectedCharacter.value || busy.value || item.equipped) return;
    busy.value = true;
    notice.value = `Đang mặc ${item.name}...`;
    const sent = gameSocket.send({
        type: 'character.equip',
        requestId: requestId(),
        characterId: selectedCharacter.value.id,
        itemId: item.id,
    });
    if (!sent) {
        busy.value = false;
        notice.value = 'Kết nối đã đóng. Hãy kết nối lại.';
    }
};

const logout = () => {
    gameSocket.send({ type: 'auth.logout', requestId: requestId() });
    account.value = null;
    characters.value = [];
    selectedCharacter.value = null;
    leaveWorld();
    password.value = '';
    notice.value = 'Đã đăng xuất. Hãy đăng nhập lại để tiếp tục.';
};

const disconnect = () => gameSocket.disconnect();

onMounted(() => {
    EventBus.on('player-move', sendMove);
    connect();
});
onUnmounted(disconnect);
onUnmounted(() => EventBus.off('player-move', sendMove));
</script>

<template>
    <main class="game-shell">
        <div class="game-stage">
            <PhaserGame />

            <header class="game-header">
                <div class="brand">
                    <span class="brand-mark">道</span>
                    <div>
                        <p class="eyebrow">HÀNH TRÌNH TU TIÊN</p>
                        <h1>Vạn Linh Giới</h1>
                    </div>
                </div>
                <div class="realm-pill"><span></span>{{ account ? `Đạo hữu ${account.username}` : 'Phàm nhân' }}</div>
            </header>

            <section v-if="!enteredWorld" class="account-panel" aria-label="Đăng nhập và tạo nhân vật">
                <div class="panel-topline">
                    <span class="connection-state" :class="connectionState"><i></i>{{ connectionState }}</span>
                    <button v-if="account" class="text-button" type="button" @click="logout">Đăng xuất</button>
                </div>

                <template v-if="!account">
                    <div class="panel-title">
                        <p class="eyebrow">CỔNG NHẬP TIÊN</p>
                        <h2>{{ authMode === 'login' ? 'Trở lại tiên môn' : 'Khai mở đạo lộ' }}</h2>
                        <p class="muted">{{ authMode === 'login' ? 'Đăng nhập để tiếp tục hành trình.' : 'Tạo tài khoản mới để bước vào Vạn Linh Giới.' }}</p>
                    </div>
                    <div class="auth-tabs" role="tablist" aria-label="Chế độ tài khoản">
                        <button type="button" :class="{ active: authMode === 'login' }" @click="authMode = 'login'">Đăng nhập</button>
                        <button type="button" :class="{ active: authMode === 'register' }" @click="authMode = 'register'">Tạo tài khoản</button>
                    </div>
                    <form class="form-stack" @submit.prevent="submitAuth">
                        <label class="field-label">Tên tài khoản
                            <input v-model.trim="username" autocomplete="username" minlength="3" maxlength="24" placeholder="3–24 ký tự, chữ thường, số hoặc _" required />
                        </label>
                        <label class="field-label">Mật khẩu
                            <input v-model="password" :autocomplete="authMode === 'register' ? 'new-password' : 'current-password'" type="password" minlength="8" maxlength="128" placeholder="Ít nhất 8 ký tự" required />
                        </label>
                        <button class="button button-primary submit-button" type="submit" :disabled="!isConnected || busy">
                            {{ busy ? 'Đang xử lý…' : authMode === 'login' ? 'Đăng nhập vào game' : 'Tạo tài khoản' }}
                        </button>
                    </form>
                </template>

                <template v-else>
                    <div class="panel-title character-heading">
                        <p class="eyebrow">TIÊN TỊCH · {{ characters.length }}/3</p>
                        <h2>{{ characters.length ? 'Nhân vật của đạo hữu' : 'Khai sinh nhân vật' }}</h2>
                        <p class="muted">Chọn giới tính tu sĩ và linh thể hộ đạo.</p>
                    </div>

                    <div v-if="characters.length" class="character-list">
                            <button v-for="character in characters" :key="character.id" type="button" class="character-row" :class="{ active: selectedCharacter?.id === character.id }" @click="selectedCharacter = character">
                            <span class="character-avatar" :style="{ '--sprite-position': spiritOptions.find((spirit) => spirit.id === character.spiritType)?.position || '0% 0%' }"></span>
                            <span class="character-details"><strong>{{ character.name }}</strong><small>{{ character.cultivatorType === 'NAM_TU' ? 'Nam Tu' : 'Nữ Tu' }} · {{ character.realmCode }} · Cấp {{ character.level }}</small></span>
                            <span class="row-spirit">{{ spiritOptions.find((spirit) => spirit.id === character.spiritType)?.name }}</span>
                            </button>
                    </div>

                    <section v-if="selectedCharacter" class="equipment-panel" aria-label="Hành trang và trang bị">
                        <div class="equipment-heading"><span><strong>Hành trang</strong><small>Dữ liệu do máy chủ quản lý</small></span><span>{{ selectedCharacter.items.length }} món</span></div>
                        <article v-for="item in selectedCharacter.items" :key="item.id" class="equipment-row">
                            <span class="equipment-icon"><img :src="gameAssetUrl(`kpah/item-icons/${item.iconId}.png`)" :alt="item.name" /></span>
                            <span class="equipment-details"><strong>{{ item.name }}</strong><small>{{ item.slot === 'SHIRT' ? 'Áo' : 'Quần' }} · Cấp yêu cầu {{ item.requiredLevel }}</small></span>
                            <button class="equip-button" type="button" :disabled="item.equipped || busy || selectedCharacter.level < item.requiredLevel" @click="equipItem(item)">
                                {{ item.equipped ? 'Đang mặc' : selectedCharacter.level < item.requiredLevel ? `Cấp ${item.requiredLevel}` : 'Mặc' }}
                            </button>
                        </article>
                    </section>

                    <button v-if="selectedCharacter" class="button button-primary submit-button" type="button" :disabled="!isConnected || busy" @click="enterCharacter(selectedCharacter)">
                        {{ busy ? 'Đang vào map…' : `Vào game với ${selectedCharacter.name}` }}
                    </button>

                    <form v-if="characters.length < 3" class="creation-form" @submit.prevent="submitCharacter">
                        <label class="field-label">Tên nhân vật
                            <input v-model.trim="characterName" autocomplete="off" minlength="3" maxlength="18" placeholder="3–18 ký tự" required />
                        </label>

                        <fieldset class="choice-fieldset">
                            <legend>Đường tu hành</legend>
                            <div class="gender-options">
                                <button type="button" class="gender-card" :class="{ selected: cultivatorType === 'NAM_TU' }" @click="cultivatorType = 'NAM_TU'">
                                    <span class="gender-portrait male" aria-hidden="true"></span><span><strong>Nam Tu</strong><small>Kiên định · Cương mãnh</small></span><span class="choice-check">✓</span>
                                </button>
                                <button type="button" class="gender-card" :class="{ selected: cultivatorType === 'NU_TU' }" @click="cultivatorType = 'NU_TU'">
                                    <span class="gender-portrait female" aria-hidden="true"></span><span><strong>Nữ Tu</strong><small>Linh động · Tinh diệu</small></span><span class="choice-check">✓</span>
                                </button>
                            </div>
                        </fieldset>

                        <fieldset class="choice-fieldset spirit-fieldset">
                            <legend>Linh thể hộ đạo</legend>
                            <div class="spirit-grid">
                                <button v-for="spirit in spiritOptions" :key="spirit.id" type="button" class="spirit-card" :class="[{ selected: spiritType === spirit.id }, `spirit-${spirit.id.toLowerCase()}`]" :style="{ '--sprite-position': spirit.position }" @click="spiritType = spirit.id">
                                    <span class="spirit-art" aria-hidden="true"></span>
                                    <span class="spirit-copy"><strong>{{ spirit.name }}</strong><small>{{ spirit.title }}</small></span>
                                    <span class="choice-check">✓</span>
                                </button>
                            </div>
                        </fieldset>

                        <button class="button button-primary submit-button" type="submit" :disabled="!isConnected || busy">
                            {{ busy ? 'Đang ghi danh…' : 'Khai sinh nhân vật' }}
                        </button>
                    </form>
                    <p v-else class="limit-note">Đạo hữu đã đạt giới hạn 3 nhân vật trên tài khoản.</p>
                </template>

                <p class="notice" aria-live="polite">{{ notice }}</p>
                <div v-if="!isConnected" class="reconnect-row">
                    <span>Server {{ socketUrl }}</span>
                    <button class="text-button" type="button" @click="connect">Kết nối lại</button>
                </div>
            </section>

            <aside v-if="enteredWorld" class="world-hud">
                <span class="world-emblem">道</span>
                <span class="world-copy"><strong>{{ enteredWorld.characterName }}</strong><small>{{ enteredWorld.mapName }} · Map {{ enteredWorld.mapId }}</small></span>
                <button class="text-button" type="button" @click="leaveWorld">Chọn nhân vật</button>
                <p>Di chuyển bằng <kbd>W A S D</kbd> hoặc <kbd>↑ ↓ ← →</kbd></p>
            </aside>

            <footer class="game-footer">
                <span>VẠN LINH GIỚI</span>
                <span>{{ enteredWorld ? 'WASD / phím mũi tên · Di chuyển trong map' : 'Chọn linh thể · Bước vào tiên đồ' }}</span>
            </footer>
        </div>
    </main>
</template>
