# Hướng Dẫn Dành Cho AI Agent Tiếp Quản Dự Án (AI Development Guide)

> **Dành cho:** Bất kỳ AI Agent nào tiếp nhận hoặc làm tiếp dự án này trong các phiên làm việc tiếp theo.  
> **Nhiệm vụ:** Đọc tài liệu này trước tiên để nắm rõ vị trí các tài nguyên, quy trình kiểm tra trạng thái và cách tiếp tục triển khai theo file kế hoạch [KPAH_DEVELOPMENT_PLAN.md](file:///home/khanhnt/WORKSPACE/WS/2dtt/docs/KPAH_DEVELOPMENT_PLAN.md).

---

## 1. Bản Đồ Tài Nguyên & Nguồn Dữ Liệu (Source of Truth)

Trước khi thực hiện bất kỳ thay đổi nào, AI cần xác định rõ vai trò của từng thư mục trong workspace:

```
/home/khanhnt/WORKSPACE/WS/2dtt/
├── docs/
│   ├── KPAH_DEVELOPMENT_PLAN.md   <-- KẾ HOẠCH TỔNG THỂ & CHECKLIST 6 GIAI ĐOẠN (BẮT BUỘC ĐỌC)
│   └── AI_DEVELOPMENT_GUIDE.md    <-- File hướng dẫn này
├── KPAH/                          <-- CODE THAM KHẢO GỐC (Java Ant, logic nghiệp vụ, công thức)
│   ├── src/                       <-- 19 package KPAH (services, player, map, skill, item, daos...)
│   └── data/                      <-- Sprite, icon, weapon, tilemap, sound gốc
├── kpah.sql                       <-- DỮ LIỆU THAM KHẢO GỐC (Templates: item, mob, npc, map, skill)
├── tu-tien-server/                <-- SERVER CHÍNH THỨC (Java 21 + Netty 4.2 WebSocket + Flyway)
│   ├── pom.xml
│   ├── server.properties
│   ├── docker-compose.yml
│   └── src/main/resources/db/migration/  <-- Lịch sử migration cơ sở dữ liệu
└── tu-tien-client/                <-- CLIENT CHÍNH THỨC (Phaser 3 + Vue 3 + TypeScript + Vite)
    ├── package.json
    ├── src/App.vue
    └── src/game/
```

> [!IMPORTANT]
> - **KHÔNG ĐƯỢC** phát triển theo đề tài Tu Tiên cũ (bỏ các khái niệm linh thể, đột phá cảnh giới...). Dự án đã được chuyển hướng hoàn toàn sang **tái hiện 100% cơ chế & gameplay của Khí Phách Anh Hùng (KPAH)**.
> - **Server chính thức là `tu-tien-server`** (Java 21, Netty 4.2 WebSocket). Thư mục `KPAH/` chỉ dùng làm tài liệu tham khảo đặc tả và lấy công thức/dữ liệu.

---

## 2. Quy Trình 5 Bước Tiếp Nhận & Làm Tiếp (SOP for AI Agent)

Khi người dùng yêu cầu tiếp tục làm dự án, AI **BẮT BUỘC** thực hiện theo quy trình sau:

### Bước 1: Đọc Kế Hoạch & Kiểm Tra Checkpoint Hiện Tại
1. Mở file [docs/KPAH_DEVELOPMENT_PLAN.md](file:///home/khanhnt/WORKSPACE/WS/2dtt/docs/KPAH_DEVELOPMENT_PLAN.md).
2. Tìm đến phần **Mục 3. Checklist Kế Hoạch Triển Khai**.
3. Rà soát xem mục nào đã đánh dấu `- [x]` (đã hoàn thành) và mục nào là `- [ ]` (đang chờ làm tiếp).
4. Xác định chính xác **Giai đoạn (Phase)** và **Hạng mục (Task)** tiếp theo cần làm.

### Bước 2: Kiểm Tra Mã Nguồn & Database Thực Tế
1. Kiểm tra các file migration đã có trong [tu-tien-server/src/main/resources/db/migration](file:///home/khanhnt/WORKSPACE/WS/2dtt/tu-tien-server/src/main/resources/db/migration).
2. Chạy lệnh kiểm tra git status để xem các file vừa được sửa gần nhất:
   ```bash
   git status -s
   ```
3. Xem class liên quan trong `tu-tien-server/src/main/java/vn/indiedev/tutien/` và `tu-tien-client/src/`.

### Bước 3: Tra Cứu Logic Chuẩn Từ KPAH Gốc
Trước khi viết code mới cho bất kỳ tính năng nào:
- **Nếu làm về Nhân vật / Thuộc tính / Level:** Đọc `KPAH/src/player/`, `KPAH/src/consts/Const.java`, và [PlayerDAO.java](file:///home/khanhnt/WORKSPACE/WS/2dtt/KPAH/src/daos/PlayerDAO.java).
- **Nếu làm về Map / Di chuyển / Zone:** Đọc `KPAH/src/map/` và `KPAH/src/services/MapService.java`.
- **Nếu làm về Quái vật / NPC:** Đọc `KPAH/src/services/MonsterService.java`, `NpcService.java`, và tra bảng `mob_template`, `npc_template` trong `kpah.sql`.
- **Nếu làm về Chiến đấu / Kỹ năng:** Đọc `KPAH/src/services/SkillService.java`, `BuffService.java`.
- **Nếu làm về Trang bị / Túi đồ:** Đọc `KPAH/src/services/InventoryService.java`, `ItemService.java`, và bảng `item_equipment` trong `kpah.sql`.

### Bước 4: Viết Code Theo Chuẩn Công Nghệ Của Dự Án
- **Backend (`tu-tien-server`):**
  - Giữ phong cách **Java 21 Clean Code**: Dùng `record` cho DTO/Events, non-blocking Netty, Virtual Threads cho JDBC tasks, không chặn Netty EventLoop.
  - Khi thay đổi schema database: **Luôn tạo file migration mới** (ví dụ `V5__...sql`, `V6__...sql`), tuyệt đối không sửa trực tiếp các file migration đã áp dụng trước đó.
- **Frontend (`tu-tien-client`):**
  - **Phaser 3:** Chỉ xử lý render canvas, animation, tilemap, viewport camera, hiệu ứng particle.
  - **Vue 3:** Quản lý toàn bộ giao diện HTML/CSS HUD bên ngoài (Form đăng nhập, chọn/tạo nhân vật, balo, chat, popup NPC).
  - TypeScript strict: Không dùng kiểu `any` tùy tiện, định nghĩa rõ ràng interface gói tin mạng.

### Bước 5: Biên Dịch, Chạy Thử & Cập Nhật Checklist
1. Sau khi code, bắt buộc chạy kiểm tra lỗi biên dịch:
   - Server: `cd tu-tien-server && mvn clean compile`
   - Client: `cd tu-tien-client && npm run typecheck`
2. Nếu pass hoàn toàn, mở file [docs/KPAH_DEVELOPMENT_PLAN.md](file:///home/khanhnt/WORKSPACE/WS/2dtt/docs/KPAH_DEVELOPMENT_PLAN.md) và chuyển trạng thái từ `- [ ]` sang `- [x]` cho hạng mục vừa hoàn thành.
3. Báo cáo ngắn gọn cho người dùng những gì đã làm và bước tiếp theo.

---

## 3. Bảng Tra Cứu Nhanh Thông Số KPAH Gốc

Khi cần điền thông số chuẩn KPAH vào code, sử dụng bảng sau (đã trích xuất từ `KPAH/src`):

### 3.1 5 Môn Phái (`class_id: 0..4`)
| Class ID | Tên Môn Phái | Hệ Ngũ Hành | Vũ Khí Mặc Định | Chỉ Số Gốc (STR, AGI, INT, VIT, LUK) |
|---|---|---|---|---|
| `0` | **Kiếm Khách** | Thủy (`0`) | Kiếm tre (ID 79) | `25, 20, 10, 25, 10` |
| `1` | **Chiến Binh** | Kim (`4`) | Đao gỗ (ID 86) | `30, 20, 10, 20, 10` |
| `2` | **Pháp Sư** | Hỏa (`2`) | Trượng trúc (ID 93) | `10, 25, 35, 10, 10` |
| `3` | **Đấu Sĩ** | Thổ (`3`) | Chùy gỗ (ID 100) | `20, 30, 10, 20, 10` |
| `4` | **Cung Thủ** | Mộc (`1`) | Cung nứa (ID 107) | `20, 30, 15, 15, 10` |

### 3.2 3 Quốc Gia (`nation_id: 0..2`)
- `0`: **Đông Sơn**
- `1`: **Nam Sơn**
- `2`: **Bắc Sơn**

### 3.3 Đồ Tân Thủ Khởi Đầu (Starter Items)
- **Nam (`gender = 1`):** Áo nhập môn (ID 2), Quần nhập môn (ID 28)
- **Nữ (`gender = 2`):** Áo bà ba (ID 1), Quần bà ba (ID 27)
- **Tiền tệ:** 10.000 Xu, 0 Lượng, 0 Lượng khóa
- **Bình thuốc:** 10 Bình HP cấp 1 (ID 1), 10 Bình MP cấp 1 (ID 4)
- **Vị trí bắt đầu:** Map 105 (Làng Hậu Đậu), tọa độ `(184, 280)`

---

## 4. Các Lệnh Terminal Thường Dùng

```bash
# 1. Kiểm tra build Server Java 21
cd /home/khanhnt/WORKSPACE/WS/2dtt/tu-tien-server
mvn clean compile

# 2. Chạy Server Java 21 Dev (yêu cầu MariaDB local hoặc container mariadb đang chạy)
cd /home/khanhnt/WORKSPACE/WS/2dtt/tu-tien-server
mvn compile exec:java

# 3. Kiểm tra TypeScript Client
cd /home/khanhnt/WORKSPACE/WS/2dtt/tu-tien-client
npm run typecheck

# 4. Chạy Client Vite Dev (port 5173)
cd /home/khanhnt/WORKSPACE/WS/2dtt/tu-tien-client
npm run dev

# 5. Khởi động DB và phpMyAdmin qua Docker (nếu chưa bật DB local)
cd /home/khanhnt/WORKSPACE/WS/2dtt/tu-tien-server
docker compose up -d mariadb phpmyadmin
```

---

## 5. Những Điều Tuyệt Đối Tránh (Guardrails)

1. ❌ **Không tự ý phát minh luật chơi mới:** Nếu phân vân về công thức tính sát thương, tỷ lệ đập đồ, hay thông số quái, hãy mở code `KPAH/src` hoặc `kpah.sql` ra tra cứu thay vì tự nghĩ ra.
2. ❌ **Không commit password hoặc file cấu hình nhạy cảm:** File `server.properties` đã được thêm vào `.gitignore`, chỉ commit file `server.properties.example`.
3. ❌ **Không sửa file Flyway migration cũ đã chạy:** Mọi thay đổi cấu trúc bảng bắt buộc phải tạo file mới `V5__...sql`, `V6__...sql`.
4. ❌ **Không để logic game nặng nề chạy trong luồng Netty EventLoop:** Mọi thao tác JDBC/Database phải được dispatch sang Virtual Threads hoặc worker pool.
