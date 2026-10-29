# Tu Tiên 2D H5 — server

Server Java 21 độc lập với dự án KPAH. Nền tảng hiện có:

- Netty 4.2 WebSocket tại `/game`.
- HTTP static assets tại `/assets/**` trên cùng cổng server.
- MariaDB qua HikariCP.
- Flyway versioned SQL migrations trong `src/main/resources/db/migration`.
- Flyway migration tạo `accounts`, `characters`, cùng loại tu sĩ và linh thể.
- Đăng ký/đăng nhập với mật khẩu băm PBKDF2-HMAC-SHA256.
- Tạo tối đa ba nhân vật mỗi tài khoản; lưu lựa chọn Nam Tu/Nữ Tu và một trong Tứ Linh.
- Hành trang và áo/quần đang mặc được lưu server-side; server kiểm tra quyền sở hữu, giới tính và cấp yêu cầu khi đổi đồ.
- Nhân vật mới vào map mở đầu theo mẫu KPAH (map 105, điểm xuất hiện 184, 280); vị trí được lưu server-side và bước di chuyển được xác thực/giới hạn bởi server.
- Dữ liệu map 105, collision, placements cây, `char_part` và sprite sheets quần áo KPAH được đóng gói dưới `static/assets/kpah` và phục vụ qua `/assets/**`.

## Yêu cầu

- JDK 21+
- Maven 3.9+
- MariaDB/MySQL đang chạy.

## Tạo database local

```sql
CREATE DATABASE tu_tien CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tu_tien'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON tu_tien.* TO 'tu_tien'@'localhost';
```

Chỉnh file `server.properties` trong thư mục gốc server để cấu hình kết nối database và port. File này bị Git bỏ qua để không commit mật khẩu. Khi chia sẻ source, dùng `server.properties.example` làm mẫu. Biến môi trường cùng tên vẫn có thể ghi đè config file.

Chạy server (PowerShell / Bash):

```bash
cd tu-tien-server
mvn compile exec:java
```

## Chạy nhanh bằng Docker & Docker Compose

Khởi động toàn bộ stack gồm `app` (tu-tien-server), `mariadb` (MariaDB 10), và `phpmyadmin` (web quản lý DB tại port 8080):

```bash
cd tu-tien-server
docker compose up -d
```

- **Server WebSocket & HTTP static:** `ws://localhost:19130/game` và `http://localhost:19130/assets/...`
- **phpMyAdmin:** `http://localhost:8080` (đã cấu hình tự động đăng nhập bằng tài khoản `root`)
- **MariaDB:** `localhost:3306` (database: `tu_tien`)
- Flyway migration tự động được thực thi ngay khi container `app` khởi động.

Dừng toàn bộ dịch vụ:

```bash
docker compose down
```

Khi chạy, Flyway tự áp dụng migration chưa chạy và ghi trạng thái vào bảng `flyway_schema_history`. Mỗi thay đổi schema tiếp theo cần tạo file mới theo dạng `V2__ten_thay_doi.sql`, `V3__...`; không sửa migration đã áp dụng trên môi trường dùng chung. Map KPAH được trích lại bằng `python tools/import_kpah_starter_data.py`.

## Thử WebSocket

Kết nối WebSocket tới `ws://localhost:19130/game`; ảnh tải bằng HTTP từ `http://localhost:19130/assets/...` trên cùng cổng. Ví dụ `http://localhost:19130/assets/kpah/item-icons/176.png`. Server cho phép CORS cho các file ảnh công khai để client Vite ở cổng `5173` dùng được. Server gửi `{"type":"server.ready","protocolVersion":1}` sau handshake. Lệnh tài khoản và nhân vật gửi dưới dạng JSON text frame, trường `requestId` được phản chiếu trong phản hồi:

```json
{"type":"auth.register","requestId":"r1","username":"dao_huu","password":"mat-khau-tu-8-ky-tu"}
{"type":"auth.login","requestId":"r2","username":"dao_huu","password":"mat-khau-tu-8-ky-tu"}
{"type":"character.create","requestId":"r3","name":"ThanhVan","cultivatorType":"NU_TU","spiritType":"CHU_TUOC"}
{"type":"character.enter","requestId":"r4","characterId":1}
{"type":"player.move","dx":8,"dy":0}
{"type":"character.equip","requestId":"r5","characterId":1,"itemId":2}
{"type":"auth.logout","requestId":"r6"}
```

`itemId` là ID instance item trong `characters[].items[]` server trả về, không phải `templateId`. Đăng nhập/đăng ký trả `auth.success` kèm `account`, danh sách nhân vật và item/trang bị do server tải từ DB; tạo nhân vật trả `character.created`; `character.enter` trả `character.entered` cùng map và tọa độ; `player.move` nhận từng bước dx/dy tối đa 8 đơn vị và trả tọa độ server đã lưu qua `player.moved`; mặc item trả `character.updated`. Lỗi trả `type: error`, mã `code` ổn định và thông báo tiếng Việt. `ping` trả `pong`. Phiên xác thực gắn với kết nối WebSocket; đăng xuất hoặc đóng kết nối sẽ kết thúc phiên. Tác vụ hash mật khẩu và truy vấn JDBC chạy ngoài Netty event loop trên worker pool.

Validation phía server: username 3–24 ký tự ASCII chữ/số/gạch dưới; password 8–128 ký tự; tên nhân vật 3–18 chữ/số/gạch dưới; tối đa 3 nhân vật/tài khoản. Migration mới được tự áp dụng khi server khởi động.

## Cấu hình

File `server.properties` được đọc từ thư mục làm việc khi chạy server. Có thể chọn file khác bằng biến `SERVER_CONFIG`. Biến môi trường được ưu tiên hơn giá trị trong file.
`sslMode=disable` trong URL dành cho database local không bật TLS; môi trường production nên cấu hình TLS cho database.

```properties
server.host=0.0.0.0
server.port=19130
db.url=jdbc:mariadb://127.0.0.1:3306/tu_tien?useUnicode=true&characterEncoding=utf8&sslMode=disable
db.user=tu_tien
db.password=change-me
```

| Biến | Mặc định |
|---|---|
| `SERVER_HOST` | `0.0.0.0` |
| `SERVER_PORT` | `19130` |
| `DB_URL` | `jdbc:mariadb://127.0.0.1:3306/tu_tien?...&sslMode=disable` |
| `DB_USER` | `tu_tien` |
| `DB_PASSWORD` | `change-me` |
