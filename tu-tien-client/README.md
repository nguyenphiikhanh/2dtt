# Tu Tiên 2D — H5 client

Client độc lập của game tu tiên 2D, dựa trên template Phaser + Vue + TypeScript + Vite của Phaser. Phaser vẽ scene game; Vue quản lý các lớp UI như trạng thái kết nối và HUD. Map khởi đầu dùng tile/collision data của KPAH map 105; avatar ghép từ sprite layers và `char_part` metadata được server phục vụ.

Template nền: [phaserjs/template-vue-ts](https://github.com/phaserjs/template-vue-ts) (MIT).

## Chạy local

Yêu cầu Node.js và npm.

```powershell
npm ci
Copy-Item .env.example .env.local
npm run dev
```

Mở `http://localhost:5173`. Vite đọc URL WebSocket từ `VITE_GAME_WS_URL`, mặc định trong `.env.example` là `ws://localhost:19130/game` để kết nối với server ở `../tu-tien-server`.

```powershell
npm run typecheck
npm run build
```

## Cấu trúc

- `src/App.vue`: giao diện đăng nhập/đăng ký, danh sách và tạo nhân vật.
- `src/PhaserGame.vue`: cầu nối Vue–Phaser theo template gốc.
- `src/game`: cấu hình Phaser và các scene.
- `src/net/GameSocket.ts`: WebSocket client (`connect`, `disconnect`, gửi lệnh).
- `src/config/GameAssets.ts`: tạo URL tải ảnh từ static asset endpoint của game server.
- `src/game/data/KpahAssets.ts`: kiểu dữ liệu cho map tile/collision và khung sprite KPAH.

Khi WebSocket kết nối tới `ws://localhost:19130/game`, giao diện hỗ trợ:

- Đăng ký: `auth.register` với `username` và `password`.
- Đăng nhập: `auth.login`; phản hồi trả về tài khoản cùng tối đa ba nhân vật hiện có.
- Tạo nhân vật: `character.create` với `name`, `cultivatorType` (`NAM_TU` hoặc `NU_TU`) và `spiritType` (`THANH_LONG`, `BACH_HO`, `HUYEN_VU`, `CHU_TUOC`).
- Chọn nhân vật trả về hành trang và trang bị từ server; `character.equip` yêu cầu `characterId` và `itemId` để mặc đồ.
- Tạo nhân vật tự vào map mở đầu; nhân vật đã có dùng nút “Vào game”. Dùng WASD hoặc phím mũi tên để di chuyển, vị trí do server xác nhận và lưu.
- Đăng xuất: `auth.logout`, xóa phiên gắn với kết nối WebSocket hiện tại.

Nhân vật, item instance, cấp yêu cầu, giới tính phù hợp và slot đang mặc được lưu trong MariaDB phía server. Vue chỉ giữ dữ liệu phản hồi trong bộ nhớ để hiển thị; mọi lần mặc đồ đều được server kiểm tra và ghi DB. Các icon áo/quần lấy từ asset `KPAH/data/image/icon` và được server phục vụ tại `/assets/kpah/item-icons`.

Ảnh game được server phục vụ qua `http://localhost:19130/assets/`; `VITE_GAME_ASSET_URL` trong `.env.local` có thể đổi host/base path. Client không còn đóng gói bản sao ảnh trong `public/assets`.

Giữ xử lý render trong Phaser, UI ngoài canvas trong Vue, và để server xác thực mọi hành động ảnh hưởng trạng thái game.
