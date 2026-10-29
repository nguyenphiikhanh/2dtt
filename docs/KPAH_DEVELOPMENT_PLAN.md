# Kế Hoạch Phát Triển MMORPG Khí Phách Anh Hùng (KPAH H5)

> **Tài liệu đặc tả kiến trúc & Lộ trình triển khai (Development Roadmap & Checklist)**  
> **Backend:** Java 21 + Netty 4.2 WebSocket Server (`tu-tien-server`)  
> **Frontend:** Phaser 3 + Vue 3 + TypeScript + Vite (`tu-tien-client`)  
> **Reference Model:** Source code & Database KPAH (`/KPAH` & `kpah.sql`)

---

## 1. Tổng quan Kiến trúc Hệ thống (System Architecture)

Hệ thống được xây dựng theo mô hình Client-Server hiện đại, tách biệt hoàn toàn giữa Engine hiển thị và Core logic máy chủ:

```mermaid
graph TD
    Client["Client H5 (Phaser 3 + Vue 3)"] <--> |WebSocket (JSON Frames)| Netty["Netty 4.2 EventLoop (Java 21)"]
    Netty <--> Dispatcher["Event & Packet Dispatcher"]
    Dispatcher <--> WorldMgr["World & Zone Manager (AOI Spatial Grid)"]
    Dispatcher <--> GameServices["Game Services (Auth, Char, Combat, Inventory, Skill)"]
    GameServices <--> HikariPool["HikariCP Connection Pool (Virtual Threads)"]
    HikariPool <--> DB[("MariaDB (Flyway Versioned Schema)")]
```

### 1.1 Server Stack (`tu-tien-server`)
- **Ngôn ngữ & Runtime:** Java 21 LTS (tận dụng Virtual Threads, Records, Sealed Interfaces, Pattern Matching).
- **Mạng (Network):** Netty 4.2.19 WebSocket server (Event-driven Non-blocking I/O, gánh hàng chục nghìn kết nối đồng thời).
- **Cơ sở dữ liệu (Database):** MariaDB 10+ với Connection Pool **HikariCP**.
- **Quản lý Migration:** **Flyway** kiểm soát lịch sử và phiên bản schema (`V1`, `V2`, `V3`...).
- **Build Tool:** Apache Maven 3.9+.

### 1.2 Client Stack (`tu-tien-client`)
- **Engine Game Canvas:** **Phaser 3.88+** (chịu trách nhiệm render Map tilemap, collision, animation đa tầng nhân vật, hiệu ứng chiêu thức).
- **Giao diện HUD & UI:** **Vue 3.5** (quản lý form đăng nhập/đăng ký, tạo nhân vật, balo hành trang, bảng kỹ năng, thông tin chỉ số, chat, menu NPC).
- **Ngôn ngữ:** TypeScript 5.7+ (Strict type checking).
- **Bundler:** Vite 6+.

---

## 2. Đặc tả Nghiệp vụ KPAH gốc (Core Specifications)

### 2.1 Hệ thống 5 Môn phái & Ngũ hành
| ID | Môn phái | Hệ ngũ hành | Vũ khí đặc trưng | Chỉ số tân thủ (STR, AGI, INT, VIT, LUK) |
|---|---|---|---|---|
| `0` | **Kiếm Khách** | Thủy (Băng) | Kiếm (ID 79) | `25, 20, 10, 25, 10` |
| `1` | **Chiến Binh** | Kim | Đao (ID 86) | `30, 20, 10, 20, 10` |
| `2` | **Pháp Sư** | Hỏa | Trượng (ID 93) | `10, 25, 35, 10, 10` |
| `3` | **Đấu Sĩ** | Thổ | Búa / Chùy (ID 100) | `20, 30, 10, 20, 10` |
| `4` | **Cung Thủ** | Mộc | Cung (ID 107) | `20, 30, 15, 15, 10` |

### 2.2 Hệ thống 3 Quốc Gia
- `0`: **Đông Sơn**
- `1`: **Nam Sơn**
- `2`: **Bắc Sơn**

### 2.3 Trang bị & Ngoại trang khởi đầu
- **Nam:** Áo nhập môn (ID 2), Quần nhập môn (ID 28)
- **Nữ:** Áo bà ba (ID 1), Quần bà ba (ID 27)
- **Vật phẩm hỗ trợ:** 10 Bình Máu HP (ID 1), 10 Bình Mana MP (ID 4), 10.000 Xu, 0 Lượng.
- **Bản đồ xuất phát:** Map 105 (Làng Hậu Đậu), tọa độ `(x: 184, y: 280)`.

---

## 3. Checklist Kế Hoạch Triển Khai (Milestones & Checklist)

### 📌 Giai đoạn 1: Xác thực & Quản lý Nhân vật KPAH (Auth & Character Lifecycle)
> **Mục tiêu:** Hoàn thiện 100% flow Đăng nhập $\rightarrow$ Hiển thị danh sách 3 slot nhân vật $\rightarrow$ Tạo nhân vật mới (5 phái, 3 quốc gia, Nam/Nữ) $\rightarrow$ Vào game.

- [x] **1.1. Database Schema Migration (Flyway)**
  - [x] Tạo migration `V5` cập nhật bảng `characters` chuẩn KPAH: `class_id` (0..4), `gender` (1: Nam, 2: Nữ), `head_id`, `nation_id` (0..2), `element_id` (Ngũ hành).
  - [x] Thêm các trường lưu trữ chỉ số tiềm năng khởi đầu (`strength`, `agility`, `spirit`, `health`, `luck`, `base_points`, `skill_points`).
  - [x] Thêm cột tiền tệ: `xu`, `luong`, `luong_khoa` và thuộc tính `hp`, `max_hp`, `mp`, `max_mp`.
- [x] **1.2. Server Service & Packet Handler (Java 21)**
  - [x] Hoàn thiện `GameService`: Đăng ký, đăng nhập băm PBKDF2-HMAC-SHA256.
  - [x] Hoàn thiện nghiệp vụ nhân vật chuẩn KPAH:
    - [x] `listCharacters`: Trả về danh sách tối đa 3 nhân vật với đầy đủ chỉ số, môn phái, quốc gia, trang bị đang mặc.
    - [x] `createCharacter`: Chọn 5 môn phái (Kiếm Khách, Chiến Binh, Pháp Sư, Đấu Sĩ, Cung Thủ), Giới tính Nam/Nữ, 3 Quốc gia (Đông Sơn, Nam Sơn, Bắc Sơn); tự động khởi tạo trang phục nhập môn và vũ khí tân thủ (Kiếm tre 79, Đao tre 86, Bút tre 93, Thổ búa 100, Cung tre 107), 10.000 Xu, và trang bị dự phòng trong hành trang.
    - [x] `enterWorld` & `moveCharacter`: Di chuyển với tốc độ chuẩn KPAH, lưu vị trí và kiểm tra va chạm map 105.
- [x] **1.3. Client UI (Vue 3 + Phaser 3)**
  - [x] Thiết kế màn hình Đăng ký / Đăng nhập phong cách KPAH.
  - [x] Màn hình chọn nhân vật: Hiển thị 3 bục nhân vật (Slot 1, 2, 3), thông tin level, môn phái, quốc gia, máu/mana, xu, và các món đồ đang mặc.
  - [x] Màn hình tạo nhân vật: Thẻ chọn 5 môn phái KPAH kèm mô tả, hệ ngũ hành, vũ khí; chọn Nam/Nữ và chọn 1 trong 3 Quốc gia.
  - [x] In-game HUD: Thanh máu HP (đỏ), thanh Mana MP (xanh), tọa độ Map 105, hiển thị trang bị đang mặc và hành trang với icon tải trực tiếp từ server `/assets/kpah/item-icons/`.

---

### 📌 Giai đoạn 2: Thế giới thời gian thực & Đồng bộ Không gian (World & Spatial Sync)
> **Mục tiêu:** Hệ thống Map, Zone (Khu vực), Spatial Grid AOI (Area of Interest). Nhiều người chơi cùng xuất hiện trong một bản đồ nhìn thấy nhau di chuyển và chat.

- [ ] **2.1. World & Zone Architecture (Server)**
  - [ ] Xây dựng class `GameMap` và `GameZone` (mỗi map gồm nhiều zone từ 0 đến N).
  - [ ] Spatial Grid / AOI (Area of Interest): Chỉ gửi gói tin tọa độ của người chơi trong phạm vi màn hình (Viewport).
  - [ ] Game Loop chu kỳ (Tickrate 100ms – 10 FPS) cập nhật trạng thái thế giới.
- [x] **2.2. Đồ họa Bản đồ, NPC & Hoạt ảnh Di chuyển Chuẩn KPAH (Map, NPC & Animation Overhaul)**
  - [x] **Chuẩn hóa Hướng & Khớp Sprite Di chuyển 4 hướng:** Sửa chuẩn xác index hướng KPAH gốc (`0: Xuống`, `1: Lên - quay lưng`, `2: Trái`, `3: Phải`) khi bấm WASD/Mũi tên. Khớp chu kỳ 6 bước đi `STEP_TO_FRAME = [0, 1, 2, 0, 3, 4]` với mảng vi sai `dx, dy` tương ứng từng frame, loại bỏ hoàn toàn hiện tượng lệch frame hay quay sai mặt khi di chuyển.
  - [x] **Nhịp Thở Idle Sống Động (Fast Retro Breathing):** Tăng tốc nhịp thở đứng yên từ 700ms lên 320ms/nhịp (chu kỳ 640ms) giật nhẹ 1px ở lồng ngực, đầu và tóc cực kỳ sống động và nảy theo đúng phong cách J2ME KPAH gốc; nhịp thở của NPC chạy liên tục không bị khựng lại khi người chơi di chuyển.
  - [x] **Vòng Tròn Hào Quang Chân Nhân Vật (Player Feet Halo Ring):** Bổ sung vòng tròn năng lượng lam ngọc (`#38f5e0`) huyền thoại dưới chân nhân vật người chơi với bóng đổ tiếp đất, 4 điểm ngấn năng lượng tứ phương (cardinal markers) và hiệu ứng thở hào quang (pulsing tween) xoay vòng uyển chuyển.
  - [x] **Đầy đủ dàn NPC Làng Hậu Đậu:** Xây dựng đầy đủ 5 nhân vật NPC chuẩn KPAH với 4 lớp trang phục người hoàn chỉnh (Quần, Áo, Đầu, Tóc), bóng đổ mềm, nhịp thở idle sống động và đối thoại tương tác khi click:
    - **Trưởng Làng Hậu Đậu** (Tọa độ 9, 9 - NPC nhiệm vụ chính, biểu tượng `[ ? ]` nhấp nhô)
    - **Thợ Rèn Thiết Bị** (Tọa độ 13, 9 - Cạnh giá vũ khí, rèn đúc ngũ đại phái)
    - **Dì Út Dược Sĩ** (Tọa độ 7, 9 - Cạnh vò thuốc, bán bình máu HP / mana MP)
    - **Bà Tám Tạp Hóa** (Tọa độ 13, 11 - Cạnh sạp hàng, bán trang bị & vật phẩm)
    - **Xa Phu Làng** (Tọa độ 13, 14 - Cạnh ngựa chiến, dịch chuyển qua các thành thị)
  - [x] **Hộp thoại Đối thoại Retro KPAH:** Click chuột vào NPC sẽ mở bảng đối thoại phong cách cuộn giấy hoàng kim cổ điển, hiển thị lời thoại hướng dẫn và nút đóng.
  - [x] **Mặt đường & Bãi cỏ liền mạch:** Bỏ khung viền ô vuông (inset border) trên từng tile giúp đường làng cát sỏi và bãi cỏ xanh mịn màng liền mạch, không còn bị chia ô bàn cờ.
  - [x] **Căn chỉnh thanh Máu & Tên:** Kéo hạ vị trí thanh HP và tên nhân vật xuống sát đỉnh đầu (`-33px` và `-38px`) vừa vặn, không còn bị treo lơ lửng trên không trung.
- [ ] **2.3. Hệ thống Chat**
  - [ ] Kênh chat Khu vực (Zone).
  - [ ] Kênh chat Thế giới (Toàn server).
  - [ ] Kênh chat Riêng (Mật).

---

### 📌 Giai đoạn 3: Trích xuất Data KPAH, Quái vật & NPC (Mobs & NPC System)
> **Mục tiêu:** Import toàn bộ Template từ `kpah.sql` vào cơ sở dữ liệu server. Quái vật spawn tự động, tuần tra, hồi sinh. NPC đối thoại và chuyển map.

- [ ] **3.1. Data Extraction Pipeline**
  - [ ] Viết tool migrate nạp toàn bộ dữ liệu từ `kpah.sql`:
    - [ ] `item_templates`: Hàng trăm trang bị vũ khí, quần áo, phụ kiện.
    - [ ] `mob_templates`: Toàn bộ quái vật, chỉ số HP, phòng thủ, sát thương, exp thưởng.
    - [ ] `npc_templates`: Toàn bộ NPC trong các làng và thành phố.
    - [ ] `map_templates`: Dữ liệu map, tilemap, tọa độ liên kết cổng chuyển map.
- [ ] **3.2. Monster Life-cycle & AI**
  - [ ] `MobSpawner`: Tự sinh quái theo vị trí định sẵn trên map.
  - [ ] AI Quái vật cơ bản: Trạng thái Đứng yên (Idle), Đi tuần (Patrol xung quanh tổ), Đuổi đánh khi bị tấn công hoặc khi người chơi vào tầm aggro, Chết và Hồi sinh sau thời gian delay.
- [ ] **3.3. NPC & Dịch chuyển Bản đồ**
  - [ ] NPC Render trên Client (sprite NPC, tên NPC màu vàng).
  - [ ] Menu đối thoại NPC khi người chơi click/tiến lại gần.
  - [ ] NPC Xa Phu / Cổng dịch chuyển: Chuyển qua lại giữa các bản đồ KPAH.

---

### 📌 Giai đoạn 4: Động cơ Chiến đấu & Kỹ năng 5 Phái (Combat & Skills)
> **Mục tiêu:** Tái hiện 100% công thức sát thương KPAH, đánh quái mất máu, kỹ năng 5 môn phái, lên cấp và cộng điểm tiềm năng.

- [ ] **4.1. Công thức Toán học Chiến đấu KPAH (Combat Formulas)**
  - [ ] Tính Sát thương cơ bản: Dựa trên Công vật lý, Công phép, Điểm tiềm năng theo phái.
  - [ ] Tính Giảm sát thương qua Phòng thủ vật lý & Kháng phép.
  - [ ] Tính Khắc hệ Ngũ hành: Thủy khắc Hỏa, Hỏa khắc Kim, Kim khắc Mộc, Mộc khắc Thổ, Thổ khắc Thủy (tăng/giảm % sát thương).
  - [ ] Tính Tỷ lệ Bạo kích (Chí mạng) và Né tránh.
- [ ] **4.2. Hệ thống Kỹ năng (Skill Tree)**
  - [ ] Hệ thống chiêu thức của 5 môn phái KPAH.
  - [ ] Cơ chế chọn mục tiêu (Target Lock: Target Quái hoặc Player).
  - [ ] Kiểm tra tiêu hao Mana (MP), thời gian hồi chiêu (Cooldown).
  - [ ] Render animation hiệu ứng kỹ năng trên Phaser 3.
- [ ] **4.3. Cấp độ (Level), EXP & Điểm tiềm năng**
  - [ ] Giết quái nhận EXP, rơi vật phẩm.
  - [ ] Lên cấp: Tự động hồi đầy HP/MP, cộng điểm Tiềm năng (Base Point) và điểm Kỹ năng (Skill Point).
  - [ ] Giao diện Vue 3 cho phép người chơi tự phân bổ điểm vào: Sức mạnh, Nhanh nhẹn, Tinh thần, Thể lực.

---

### 📌 Giai đoạn 5: Hành trang, Kinh tế & Rơi đồ (Inventory, Items & Economy)
> **Mục tiêu:** Quản lý túi đồ, trang bị 8 món, nhặt đồ rớt từ quái, cửa hàng NPC, cường hóa/đập đồ và khảm ngọc.

- [ ] **5.1. Hệ thống Túi đồ (Inventory Management)**
  - [ ] Balo chứa đồ (Mặc định 20..30 ô).
  - [ ] Trang bị 8 vị trí: Nón, Áo, Quần, Giày, Nhẫn, Dây chuyền, Ngọc bôi, Vũ khí.
  - [ ] Mặc / Tháo trang bị: Tự động cập nhật chỉ số nhân vật (Công, Thủ, Máu) và thay đổi ngoại hình sprite nhân vật.
- [ ] **5.2. Rơi đồ & Nhặt đồ (Drop Table & Pickup)**
  - [ ] Quái chết sinh ra Item / Xu rơi trên mặt đất tại tọa độ map.
  - [ ] Thời gian bảo lưu vật phẩm cho người tiêu diệt trước khi người khác có thể nhặt.
- [ ] **5.3. Cửa hàng NPC & Mua bán**
  - [ ] Mua bình máu, mana, phù về làng, trang bị tân thủ bằng Xu/Lượng.
  - [ ] Bán đồ trong balo lấy Xu.
- [ ] **5.4. Cường hóa (Đập đồ) & Khảm ngọc**
  - [ ] Nâng cấp trang bị từ +1 đến +15 với tỷ lệ thành công/thất bại chuẩn KPAH.
  - [ ] Ghép ngọc, khảm ngọc vào lỗ trang bị.

---

### 📌 Giai đoạn 6: Thú cưỡi, Bang hội & Tính năng Nâng cao (Social & End-Game)
> **Mục tiêu:** Thú cưỡi, Bang hội, Bảng xếp hạng, PK và Đấu trường.

- [ ] **6.1. Hệ thống Thú Cưỡi (Horse / Animal)**
  - [ ] Trang bị thú cưỡi (Ngựa trắng, Ngựa đen, Xích thố, Thần thú).
  - [ ] Tăng tốc độ di chuyển và cộng chỉ số thuộc tính.
- [ ] **6.2. Bang Hội (Clan System)**
  - [ ] Thành lập bang, quỹ bang, nâng cấp kỹ năng bang.
  - [ ] Chat bang hội, thành viên bang.
- [ ] **6.3. Chế độ PK & Lôi đài**
  - [ ] Bật/tắt cờ PK (Hòa bình, PK đồ sát, PK quốc gia).
  - [ ] Điểm sát khí, phạt điểm tội ác khi giết người vô tội.
  - [ ] Bảng xếp hạng Cao Thủ, Phú Hào.

---

## 4. Kế hoạch Bắt tay Triển khai Ngay Hôm Nay

Chúng ta sẽ thực hiện tuần tự theo quy trình:
1. **Bước 1.1:** Viết Flyway migration mới cho `tu-tien-server` để chuẩn hóa bảng `characters` theo 5 phái, 3 quốc gia, giới tính và chỉ số tiềm năng KPAH.
2. **Bước 1.2:** Cập nhật Backend `CharacterService` & `GameWebSocketHandler` để hỗ trợ flow danh sách 3 nhân vật và tạo nhân vật chuẩn KPAH.
3. **Bước 1.3:** Nâng cấp UI Client (`tu-tien-client`) với Vue 3 để người chơi đăng nhập, chọn và tạo nhân vật KPAH trực quan.
