-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3307
-- Thời gian đã tạo: Th12 31, 2023 lúc 12:11 PM
-- Phiên bản máy phục vụ: 10.4.22-MariaDB
-- Phiên bản PHP: 8.0.15

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `kpah`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `chars`
--

CREATE TABLE `chars` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `figure` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

--
-- Đang đổ dữ liệu cho bảng `chars`
--

INSERT INTO `chars` (`id`, `name`, `figure`) VALUES
(1, 'gfdgdf', '[0,2,0,0]');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `gems`
--

CREATE TABLE `gems` (
  `id` int(11) NOT NULL,
  `rID` double NOT NULL,
  `number` double NOT NULL,
  `price` double NOT NULL,
  `ilock` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `items`
--

CREATE TABLE `items` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `style` varchar(255) NOT NULL,
  `index` int(11) NOT NULL,
  `he` int(11) NOT NULL,
  `gender` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `durable` int(11) NOT NULL,
  `idIcon` double NOT NULL,
  `ndayLoan` int(11) NOT NULL,
  `price` double NOT NULL,
  `attb` double NOT NULL,
  `clazz` float NOT NULL,
  `plus` float NOT NULL,
  `colorItem` int(11) NOT NULL,
  `allAttribute` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `maps`
--

CREATE TABLE `maps` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `mobs` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

--
-- Đang đổ dữ liệu cho bảng `maps`
--

INSERT INTO `maps` (`id`, `name`, `mobs`) VALUES
(0, 'Map0', '[]'),
(1, 'Map1', '[]');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `mobs`
--

CREATE TABLE `mobs` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL,
  `hp` int(11) DEFAULT NULL,
  `level` tinyint(4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

--
-- Đang đổ dữ liệu cho bảng `mobs`
--

INSERT INTO `mobs` (`id`, `name`, `type`, `hp`, `level`) VALUES
(1, 'Nhím', -1, 200, -1),
(2, 'Sâu', -1, 300, -1),
(3, 'Giọt nước', -1, 450, -1),
(4, 'Gà điên', -1, 650, -1),
(5, 'Rắn lục', -1, 900, -1),
(6, 'Ma trơi', -1, 1200, -1),
(7, 'Nắp ấm', -1, 1550, -1),
(8, 'Rệp quỷ', -1, 1950, -1),
(9, 'Chuột cống', -1, 2400, -1),
(10, 'Quỷ hoa', -1, 3000, -1),
(11, 'Sâu róm', -1, 3660, -1),
(12, 'Lửa ma', -1, 4380, -1),
(13, 'Giọt nước lớn', -1, 5160, -1),
(14, 'Gà con', -1, 9960, -1),
(15, 'Heo mọi', -1, 6000, -1),
(16, 'Bọ cạp', -1, 6900, -1),
(17, 'Rùa đỏ', -1, 5160, -1),
(18, 'Nhện tím', -1, 18600, -1),
(19, 'Nấm ma', -1, 7860, -1),
(20, 'Quỷ sinh hoa', -1, 8880, -1),
(21, 'Rết', -1, 6900, -1),
(22, 'Gà khổng lồ', -1, 30150, -1),
(23, 'Rắn khổng lồ', -1, 32630, -1),
(24, 'Bướm khổng lồ', -1, 9960, -1),
(25, 'Bóng ma', -1, 15310, -1),
(26, 'Thủy nhãn', -1, 13770, -1),
(27, 'Skeleton', -1, 18600, -1),
(28, 'Cọp khổng lồ', -1, 12300, -1),
(29, 'Cá sấu', -1, 22170, -1),
(30, 'ếch quỷ', -1, 24060, -1),
(31, 'Dế khổng lồ', -1, 37830, -1),
(32, 'Nấm tinh', -1, 35190, -1),
(33, 'Trâu núi', -1, 40550, -1),
(34, 'Sơn tặc', -1, 43350, -1),
(35, 'Hải tặc', -1, 46230, -1),
(36, 'Long Trụ', -1, 50000000, -1),
(37, 'Long trụ phụ', -1, 25000000, -1),
(38, 'Thuồng luồng', -1, 500000, -1),
(39, 'Thằn lằn', -1, 200000000, -1),
(40, 'Rết đỏ', -1, 53678, -1),
(41, 'Rết tía', -1, 106654, -1),
(42, 'Rết xanh', -1, 75098, -1),
(43, 'Liên hoa trụ', -1, 20000000, -1),
(44, 'Tử kê', -1, 118730, -1),
(45, 'Hồng kê', -1, 57634, -1),
(46, 'Tướng thủ thành', -1, 80000000, -1),
(47, 'Rắn mang bành', -1, 100918, -1),
(48, 'Sư tử', -1, 193830, -1),
(49, 'ốc ma', -1, 460530, -1),
(50, 'Bướm nâu', -1, 79894, -1),
(51, 'Ma cây', -1, 560530, -1),
(52, 'Huyết ma', -1, 90030, -1),
(53, 'Cá Thòi lòi', -1, 330530, -1),
(54, 'Cua càng to', -1, 360530, -1),
(55, 'độc nhãn', -1, 49878, -1),
(56, 'Cá ma', -1, 680530, -1),
(57, 'Huyết nhãn', -1, 84870, -1),
(58, 'Quỷ hoa', -1, 800530, -1),
(59, 'Skeleton lam', -1, 95378, -1),
(60, 'Tê giác', -1, 240878, -1),
(61, 'Cọp tím', -1, 160078, -1),
(62, 'Cọp xanh', -1, 112590, -1),
(63, 'Cọp đỏ', -1, 61750, -1),
(64, 'Cá sấu đỏ', -1, 183830, -1),
(65, 'Cua đinh', -1, 172230, -1),
(66, 'Cá sấu xanh', -1, 200878, -1),
(67, 'Cóc lam', -1, 209778, -1),
(68, 'Cóc lửa', -1, 70478, -1),
(69, 'Cóc xanh', -1, 228350, -1),
(70, 'Dế lửa', -1, 238030, -1),
(71, 'Dế cam', -1, 66030, -1),
(72, 'Dế lam', -1, 258198, -1),
(73, 'trâu đỏ', -1, 268694, -1),
(74, 'Người đá', -1, 158414, -1),
(75, 'trâu xanh', -1, 290530, -1),
(76, 'ốc sên', -1, 152630, -1),
(77, 'Bọ hung', -1, 268350, -1),
(78, 'Sơn tặc 3', -1, 325454, -1),
(79, 'Hải tặc 1', -1, 337690, -1),
(80, 'Nấm độc', -1, 125078, -1),
(81, 'Cánh cam', -1, 290530, -1),
(82, 'đại bàng', -1, 200000000, -1),
(83, 'cổng thành', -1, 100000000, -1),
(84, 'Cương thi', -1, 5000, -1),
(85, 'Đá', -1, 50, -1),
(86, 'Bông', -1, 50, -1),
(87, 'Gỗ', -1, 50, -1),
(88, 'Da', -1, 50, -1),
(89, 'Sắt', -1, 50, -1),
(90, 'Người tuyết', -1, 150000000, -1),
(91, 'Rắn chị', -1, 150000000, -1),
(92, 'Rắn em', -1, 150000000, -1),
(93, 'Mắt quỷ', -1, 100000000, -1),
(94, 'Bạch cốt tướng quân', -1, 150000000, -1),
(95, 'Ngựa ca nhan trang', -1, 158414, -1),
(96, 'Ngựa ca nhan xanh', -1, 158414, -1),
(97, 'Ngựa ca nhan đỏ', -1, 158414, -1),
(98, 'Ngựa ca nhan xanh la', -1, 158414, -1),
(99, 'Ngựa ca nhan vàng', -1, 158414, -1),
(100, 'Ngựa ca nhan tím', -1, 158414, -1),
(101, 'Ngựa bang trắng', -1, 158414, -1),
(102, 'Ngựa bang xanh', -1, 158414, -1),
(103, 'Ngựa bang đỏ', -1, 158414, -1),
(104, 'Ngựa bang xanh lá', -1, 158414, -1),
(105, 'Ngựa bang vàng', -1, 158414, -1),
(106, 'Ngựa bang tím', -1, 158414, -1),
(107, 'Ngựa quốc gia trắng', -1, 158414, -1),
(108, 'Ngựa quốc gia xanh', -1, 158414, -1),
(109, 'Ngựa quốc gia đỏ', -1, 158414, -1),
(110, 'Ngựa quốc gia xanh lá', -1, 158414, -1),
(111, 'Ngựa quốc gia vàng', -1, 158414, -1),
(112, 'Ngựa quốc gia tím', -1, 158414, -1),
(113, 'Boss thỏ điên', -1, 200000000, -1),
(114, 'Khô lâu', -1, 1, -1),
(115, 'Boss Gấu xám', -1, 400000000, -1),
(116, 'Dracula', -1, 500000000, -1),
(117, 'Bí ngô', -1, 500000000, -1),
(118, 'Nhện ma', -1, 900530, -1),
(119, 'cong thanh', -1, 1, -1),
(120, 'Trụ rồng', -1, 5000000, -1),
(121, 'Rương ma quái', -1, 1, -1),
(122, 'Ngọc 1 sao', -1, 1, -1),
(123, 'Ngọc 2 sao', -1, 1, -1),
(124, 'Ngọc 3 sao', -1, 1, -1),
(125, 'Ngọc 4 sao', -1, 1, -1),
(126, 'Ngọc 5 sao', -1, 1, -1),
(127, 'Ngọc 6 sao', -1, 1, -1),
(128, 'Ngọc 7 sao', -1, 1, -1),
(129, 'Boss thuỷ tinh', -1, 1, -1),
(130, 'Boss sơn tinh', -1, 1, -1),
(131, 'Thùng gỗ', -1, 1, -1),
(132, 'Cọp tím', -1, 950000, -1),
(133, 'Cọp xanh', -1, 1000000, -1),
(134, 'Cọp đỏ', -1, 1050000, -1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `monsters`
--

CREATE TABLE `monsters` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `image` text NOT NULL,
  `Arrimage` text NOT NULL,
  `moveType` int(11) NOT NULL,
  `speed` double NOT NULL,
  `height` double NOT NULL,
  `w` double NOT NULL,
  `h` double NOT NULL,
  `xCenter` double NOT NULL,
  `yCenter` double NOT NULL,
  `xadd` double NOT NULL,
  `yadd` double NOT NULL,
  `isNewMonster` int(11) NOT NULL,
  `he` int(11) NOT NULL,
  `palate` double NOT NULL,
  `spalate` double NOT NULL,
  `maxhp` double NOT NULL,
  `isLoad` varchar(255) NOT NULL,
  `isMaxHight` varchar(255) NOT NULL,
  `timePaint` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `npc`
--

CREATE TABLE `npc` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` int(255) NOT NULL,
  `idImg` varchar(255) NOT NULL,
  `x` varchar(255) NOT NULL,
  `y` varchar(255) NOT NULL,
  `width` varchar(255) NOT NULL,
  `height` varchar(255) NOT NULL,
  `nFrame` text NOT NULL,
  `typeLimit` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `chars` varchar(255) NOT NULL DEFAULT '[]'
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `chars`) VALUES
(1, 'admin', '1', '[]');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xaphu`
--

CREATE TABLE `xaphu` (
  `id` int(11) NOT NULL,
  `x` varchar(255) NOT NULL,
  `y` varchar(255) NOT NULL,
  `price` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `chars`
--
ALTER TABLE `chars`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Chỉ mục cho bảng `gems`
--
ALTER TABLE `gems`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `maps`
--
ALTER TABLE `maps`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Chỉ mục cho bảng `mobs`
--
ALTER TABLE `mobs`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Chỉ mục cho bảng `monsters`
--
ALTER TABLE `monsters`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `npc`
--
ALTER TABLE `npc`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Chỉ mục cho bảng `xaphu`
--
ALTER TABLE `xaphu`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `chars`
--
ALTER TABLE `chars`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `gems`
--
ALTER TABLE `gems`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `items`
--
ALTER TABLE `items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `monsters`
--
ALTER TABLE `monsters`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `npc`
--
ALTER TABLE `npc`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `xaphu`
--
ALTER TABLE `xaphu`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
