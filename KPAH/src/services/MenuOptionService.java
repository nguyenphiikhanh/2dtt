package services;

import consts.Const;
import java.io.IOException;
import lombok.NonNull;
import network.Message;
import player.Player;
import utils.CommandMessage;
import consts.ItemEquipConst;
import consts.NpcConst;
import item.ItemAnimal;
import item.Attribute;
import item.ItemPotion;
import java.util.ArrayList;
import java.util.List;
import lombok.Cleanup;
import manager.Manager;
import template.HoaTieuTemplate;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class MenuOptionService {

    public static final MenuOptionService instance = new MenuOptionService();

    private static final byte MUA_TRANG_BI = 0;
    private static final byte SHOP_HAC_NGUU = 1;
    private static final byte THAY_NGU_HANH = 2;
    private static final byte THO_HOP_THANH_CAO_CAP = 3;
    private static final byte THO_HOP_THANH_SO_CAP = 4;
    private static final byte TONG_QUAN = 5;
    private static final byte THO_REN_THAN_BI = 5;
    private static final byte TONG_TIEU_DAU = 6;
    private static final byte DAU_TRUONG = 7;
    private static final byte TIEN_NU = 8;
    private static final byte HAO_DUYEN = 9;
    private static final byte TAO_THU = 10;
    private static final byte LUYEN_THU = 11;
    private static final byte LUYEN_THU_SPECIAL = 12;
    private static final byte CONG_DICH_CHUYEN = 13;
    private static final byte XA_PHU_NEW = 14;
    private static final byte HOA_TIEU_NEW = 15;
    private static final byte SELECT_HOA_TIEU_MAP = 16;
    private static final byte BANG_TOP = 17;

    public void onMenuOption(@NonNull Player player, byte idMenu, byte selected) throws IOException {
        if (selected < 0) {
            return;
        }
        if (player.isDie()) {
            return;
        }
        player.getSundry().setIdOpenMenu(idMenu);
        player.getSundry().setSelectedOption(selected);
        switch (idMenu) {
            case THAY_NGU_HANH -> {
                switch (selected) {
                    case 4 ->
                        InventoryService.instance.sendItemSold(player);
                }
            }
            case MUA_TRANG_BI -> {
                switch (player.getSundry().getIdNpcOpen()) {
                    case NpcConst.HAC_NGUU ->
                        ShopService.instance.openNpcShop(player, "WEAPON_ANIMAL", selected);
                    case NpcConst.THIET_BI ->
                        ShopService.instance.openNpcShop(player, "ITEM_EQUIPMENT", selected);
                    case NpcConst.GIAP_SU ->
                        ShopService.instance.openNpcShop(player, "GIAP_SU", selected);
                }
            }
            case SHOP_HAC_NGUU -> {
                switch (selected) {
                    case 0 ->
                        ShopService.instance.openNpcShop(player, "WEAPON", ItemEquipConst.DAMAGE_NONE);
                    case 1 ->
                        sendOptionBuyItem(player);
                }
            }
            case HAO_DUYEN -> {
                switch (selected) {
                    case 0 ->
                        sendMenuTaoThu(player);
                    case 1 ->
                        sendMenuLuyenThu(player);
                    case 2 ->
                        upgradeAnimal(player);
                }
            }
            case TAO_THU -> {
                short idItem = -1;
                switch (selected) {
                    case 0, 1, 2, 3, 4 ->
                        idItem = (short) (64 + selected);
                    case 5 ->
                        idItem = 86;
                    case 6 ->
                        idItem = 70;
                    case 7 ->
                        idItem = 74;
                    case 8 ->
                        idItem = 72;
                    case 9 ->
                        idItem = 115;
                }
                if (idItem == -1) {
                    return;
                }
                ItemPotion trung = InventoryService.instance.findItemPotion(player, idItem);
                if (trung == null) {
                    Service.instance.sendLogOut(player.getSession(), String.format("Không tìm thấy trứng %s", Util.capitalizeFirstLetter(Manager.getPotionTemplate(idItem).getName().replace("Trứng ", ""))));
                    return;
                }
                createAnimal(player, idItem);
                InventoryService.instance.minusQuantityItemPotion(player, trung, (short) 1);
                InventoryService.instance.sendItemPotion(player);
                InventoryService.instance.sendItemAnimal(player);
                Service.instance.sendLogOut(player.getSession(), String.format("Tạo thành công linh thú %s", Util.capitalizeFirstLetter(trung.getTemplate().getName().replace("Trứng ", ""))));
            }
            case LUYEN_THU -> {
                switch (selected) {
                    case 0 ->
                        upgradeNormal(player);
                    case 1 ->
                        upgradeTiemNang(player);
                    case 2 ->
                        sendMenuLuyenThuSpecial(player);
                }
            }
            case LUYEN_THU_SPECIAL -> {
                switch (selected) {
                    case 0 ->
                        changeSpecialAttribute(player);
                    case 1 ->
                        upgradeSpecialAttribute(player);
                }
            }
            case CONG_DICH_CHUYEN -> {
                switch (selected) {
                    case 0 -> {
                        byte countryGo = player.getLocation().getInCountry() == Const.THANH_LONG ? Const.HAC_HO : Const.THANH_LONG;
                        player.getLocation().setInCountry(countryGo);
                        ChangeMapService.instance.changeMap(player, (short) 118);
                        MapService.instance.sendLocationServer(player);
                        Service.instance.sendMainCharInfo(player);
                    }
                    case 1 ->
                        ChangeMapService.instance.changeMap(player, (short) 9, (short) 280, (short) 1464);
                }
            }
            case XA_PHU_NEW -> {
                switch (selected) {
                    case 0 -> {
                        if (player.getLocation().getInCountry() != player.getInfo().getIdNation()) {
                            player.getLocation().setInCountry(player.getInfo().getIdNation());
                            MapService.instance.sendLocationServer(player);
                            Service.instance.sendMainCharInfo(player);
                        }
                        ChangeMapService.instance.changeMap(player, player.getLocation().getMapVillage(), (short) 384, (short) 672);
                    }
                    case 1 -> {
                        if (player.getLocation().getInCountry() != player.getInfo().getIdNation()) {
                            player.getLocation().setInCountry(player.getInfo().getIdNation());
                            MapService.instance.sendLocationServer(player);
                            Service.instance.sendMainCharInfo(player);
                        }
                        ChangeMapService.instance.changeMap(player, (short) (player.getInfo().getIdNation() == Const.THANH_LONG ? 1701 : 301), (short) 384, (short) 672);
                    }
                }
            }
            case HOA_TIEU_NEW -> {
                byte idHoaTieu = player.getSundry().getIdHoaTieu();
                HoaTieuTemplate template = Manager.getHoaTieuTemplate(idHoaTieu);
                if (template == null) {
                    return;
                }
                sendMenuHoaTieuMap(player, template, selected);
            }
            case SELECT_HOA_TIEU_MAP -> {
                byte idHoaTieu = player.getSundry().getIdHoaTieu();
                HoaTieuTemplate template = Manager.getHoaTieuTemplate(idHoaTieu);
                if (template == null) {
                    return;
                }
                short mapId = template.getMapId()[player.getSundry().getIndexHoaTieu()][selected];
                short x = template.getX()[player.getSundry().getIndexHoaTieu()];
                short y = template.getY()[player.getSundry().getIndexHoaTieu()];
                ChangeMapService.instance.changeMap(player, mapId, x, y);
            }
        }
    }

    public void sendMenuLuyenThuSpecial(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        sendOptionMenu(player, LUYEN_THU_SPECIAL, String.format("Thay đổi (%s lượng)", Manager.ANIMAL_CHANGE_SPECIAL_ATTRIBUTE_PRICE), String.format("Nâng cấp (%s lượng)", Manager.getAnimalTrainPrice(animal.getLevel())));
    }

    public void sendMenuLuyenThu(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        sendOptionMenu(player, LUYEN_THU, String.format("Cơ bản (%s lượng)", Manager.getAnimalTrainPrice(animal.getLevel())), String.format("Tiềm năng (%s lượng)", Manager.getAnimalTrainPrice(animal.getLevel())), "Đặc biệt");
    }

    public void sendMenuBangTop(@NonNull Player player) throws IOException {
        sendOptionMenu(player, BANG_TOP, "Top cao thủ Thanh long", "Top cao thủ Hắc hổ", "Top công trạng Thanh long", "Top công trạng Hắc hổ", "Top liên trảm Thanh long", "Top liên trảm Hắc hổ");
    }

    public void sendMenuTaoThu(@NonNull Player player) throws IOException {
        sendOptionMenu(player, TAO_THU, "Hắc ngưu", "Mãnh hỗ", "Sói xám", "Tiên hạc", "Bạch mã", "Phượng hoàng", "Phượng hoàng băng", "Bạch cốt", "Đương khang", "Lân sư tử");
    }

    public void sendMenuHaoDuyen(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null || animal.getLevel() >= ItemEquipConst.MAX_LEVEL_ANIMAL) {
            sendOptionMenu(player, HAO_DUYEN, "Tạo thú", "Luyện thú");
            return;
        }
        sendOptionMenu(player, HAO_DUYEN, "Tạo thú", "Luyện thú", String.format("Nâng cấp thú (%s lượng)", Manager.getPriceUpgradeAnimal(animal.getLevel())));
    }

    public void sendMenuHoaTieuMap(@NonNull Player player, @NonNull HoaTieuTemplate template, byte index) throws IOException {
        player.getSundry().setIndexHoaTieu(index);
        sendOptionMenu(player, SELECT_HOA_TIEU_MAP, template.getNameMapChild()[index]);
    }

    public void sendMenuHoaTieu2(@NonNull Player player) throws IOException {
        byte idHoaTieu = player.getLocation().getZone().getMap().getMapData().getIdXaPhu();
        HoaTieuTemplate template = Manager.getHoaTieuTemplate(idHoaTieu);
        if (template == null) {
            return;
        }
        player.getSundry().setIdHoaTieu(idHoaTieu);
        sendOptionMenu(player, HOA_TIEU_NEW, template.getNameMap());
    }

    public void sendMenuHoaTieu(@NonNull Player player) throws IOException {
        byte idHoaTieu = player.getLocation().getZone().getMap().getMapData().getIdHoaTieu();
        HoaTieuTemplate template = Manager.getHoaTieuTemplate(idHoaTieu);
        if (template == null) {
            return;
        }
        player.getSundry().setIdHoaTieu(idHoaTieu);
        sendOptionMenu(player, HOA_TIEU_NEW, template.getNameMap());
    }

    public void sendMenuXaPhuNew(@NonNull Player player) throws IOException {
        if (player.getInfo().getIdNation() == Const.THANH_LONG) {
            sendOptionMenu(player, XA_PHU_NEW, "Dương đông", "Đông Dương đông");
        } else {
            sendOptionMenu(player, XA_PHU_NEW, "Sơn nam", "Đông Sơn nam");
        }
    }

    public void sendMenuCongDichChuyen(@NonNull Player player) throws IOException {
        sendOptionMenu(player, CONG_DICH_CHUYEN, String.format("Đến biên giới %s", player.getLocation().getInCountry() == Const.THANH_LONG ? "Hắc hổ" : "Thanh long"), "Trường giang");
    }

    public void sendMenuDauTruong(@NonNull Player player) throws IOException {
        sendOptionMenu(player, DAU_TRUONG, "Thách đấu cá nhân", "Trấn yêu trận", "Đăng kí chiến trường", "Nhận quà khu liên đấu", "Nhận quà liên trảm", "Nhận quà top trụ", "Núi châu báu", "Hủy đăng ký liên đấu");
    }

    public void sendMenuTongTieuDau(@NonNull Player player) throws IOException {
        sendOptionMenu(player, TONG_TIEU_DAU, "Xuống ngựa", "Đổi danh hiệu", "Đăng ký lôi đài", "Vào sảnh chờ", "Xem lôi đài");
    }

    public void sendMenuThoRenThanBi(@NonNull Player player) throws IOException {
        sendOptionMenu(player, THO_REN_THAN_BI, "Vũ khí", "Trang bị", "Trang bị thú", "Gia hạn vktt(150l 7n)", "Gia hạn vktt dùng thẻ", "Tắt phi phong vip", "Tắt mặt nạ");
    }

    public void sendMenuTongQuan(@NonNull Player player) throws IOException {
        sendOptionMenu(player, TONG_QUAN, "Hợp đục", "Mở rộng hành trang(150L)", "Thử vận may", "Chuyển lãnh thổ(300L)", "Nhận quà Giftcode", "Đăng ký liên đấu");
    }

    public void sendMenuThoHopThanhSoCap(@NonNull Player player) throws IOException {
        sendOptionMenu(player, THO_HOP_THANH_SO_CAP, "Nguyên liệu thường", "Nguyên liệu khóa", "Ngọc huyền minh", "Ngọc huyền minh khóa", "Bột thường", "Bột khóa");
    }

    public void sendMenuThoHopThanhCaoCap(@NonNull Player player) throws IOException {
        sendOptionMenu(player, THO_HOP_THANH_CAO_CAP, "Nguyên liệu thường", "Nguyên liệu khóa", "Xương không khóa", "Xương khóa");
    }

    public void sendMenuThayNguHanh(@NonNull Player player) throws IOException {
        sendOptionMenu(player, THAY_NGU_HANH, "Đổi hệ ngũ hành", "Tháo ngọc khảm", "Đặt mật khẩu rương", "Vòng quay", "Đồ đã bán");
    }

    public void sendOptionShopHacNguu(@NonNull Player player) throws IOException {
        sendOptionMenu(player, SHOP_HAC_NGUU, "Mua vũ khí", "Mua trang bị thú");
    }

    public void sendOptionBuyItem(@NonNull Player player) throws IOException {
        sendOptionMenu(player, MUA_TRANG_BI, "Trang bị ma pháp", "Trang bị vật lý");
    }

    private void sendOptionMenu(@NonNull Player player, byte idMenu, String... text) throws IOException {
        Message msg = new Message(CommandMessage.MENU_OPTION);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(idMenu);
        msg.writer().writeByte(text.length);
        for (String s : text) {
            msg.writer().writeUTF(s);
        }
        player.getSession().sendMessage(msg);
    }

    // <editor-fold defaultstate="collapsed" desc="Functions Animal">
    private void createAnimal(@NonNull Player player, short idItem) {
        ItemAnimal animal = ItemService.instance.createNewItemAnimal(idItem);
        if (animal == null) {
            return;
        }
        if (idItem >= 64 && idItem <= 68) {
            animal.setItemBody(new ArrayList<>());
            animal.setMinutes(-1);
            animal.setTimeStart(System.currentTimeMillis());
            byte level = 1;
            for (int i = 0; i < ItemEquipConst.ATTRIBUTE_DEFAULT_ANIMAL.length; i++) {
                short id = ItemEquipConst.ATTRIBUTE_DEFAULT_ANIMAL[i];
                short value = (short) (Util.nextInt(1, Manager.getMaxValueAttributeAnimal((byte) 56, level)) >= 2 ? 2 : 1);
                animal.getAttributes().add(new Attribute(Manager.getAttributeTemplate(id), value));
            }
            animal.getAttributes().add(new Attribute(Manager.getAttributeTemplate((short) 33), (short) Manager.getMaxValueAttributeAnimal((byte) 33, level)));
            animal.getAttributes().add(new Attribute(Manager.getAttributeTemplate((short) 34), (short) Manager.getMaxValueAttributeAnimal((byte) 34, level)));
            short idRandom = ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL[Util.nextInt(0, ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL.length - 1)];
            animal.getAttributes().add(new Attribute(Manager.getAttributeTemplate(idRandom), (short) (Util.nextInt(1, Manager.getMaxValueAttributeAnimal(idRandom, level)) >= 2 ? 2 : 1)));
            idRandom = ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL_SPECIAL[Util.nextInt(0, ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL_SPECIAL.length - 1)];
            animal.getAttributes().add(new Attribute(Manager.getAttributeTemplate(idRandom), (short) (Util.nextInt(1, Manager.getMaxValueAttributeAnimal(idRandom, level)) >= 2 ? 2 : 1)));
        }
        InventoryService.instance.addItemAnimal(player, animal);
    }

    private void upgradeAnimal(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        if (animal.getLevel() >= ItemEquipConst.MAX_LEVEL_ANIMAL) {
            Service.instance.sendLogOut(player.getSession(), "Linh thú đạt cấp tối đa");
            return;
        }
        if (!animal.getAttributes().stream().allMatch(att -> att != null && att.getValue() >= Manager.getMaxValueAttributeAnimal(att.getTemplate().getId(), animal.getLevel()))) {
            Service.instance.sendLogOut(player.getSession(), "Linh thú chưa đủ điều kiện để nâng cấp");
            return;
        }
        int price = Manager.getPriceUpgradeAnimal(animal.getLevel());
        if (!player.getInventory().minusLuong(price)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(price)));
            return;
        }
        animal.plusLevel((byte) 1);
        for (short i = 3; i < 5; i++) {
            Attribute att = animal.getAttributes().get(i);
            if (att != null && att.getValue() < Manager.getMaxValueAttributeAnimal(att.getTemplate().getId(), animal.getLevel())) {
                att.setValue(Manager.getMaxValueAttributeAnimal(att.getTemplate().getId(), animal.getLevel()));
            }
        }
        @Cleanup("clear")
        List<Short> existingAttributeIds = new ArrayList<>();
        for (Attribute att : animal.getAttributes()) {
            if (att != null) {
                existingAttributeIds.add(att.getTemplate().getId());
            }
        }
        short idRandom;
        do {
            idRandom = ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL[Util.nextInt(0, ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL.length - 1)];
        } while (existingAttributeIds.contains(idRandom));
        animal.getAttributes().add(animal.getAttributes().size() - 1, new Attribute(Manager.getAttributeTemplate(idRandom), (short) (Util.nextInt(1, Manager.getMaxValueAttributeAnimal(idRandom, animal.getLevel())) >= 2 ? 2 : 1)));
        player.getPoint().initPoint();
        InventoryService.instance.sendItemBody(player);
        InventoryService.instance.sendItemAnimal(player);
        Service.instance.sendMainCharInfo(player);
    }

    private void changeSpecialAttribute(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        int price = Manager.ANIMAL_CHANGE_SPECIAL_ATTRIBUTE_PRICE;
        if (!player.getInventory().minusLuong(price)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(price)));
            return;
        }
        Attribute attributeToChange = animal.getAttributes().get(animal.getAttributes().size() - 1);
        if (attributeToChange == null) {
            return;
        }
        animal.getAttributes().remove(attributeToChange);
        short idRandom = ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL_SPECIAL[Util.nextInt(0, ItemEquipConst.ATTRIBUTE_RANDOM_ANIMAL_SPECIAL.length - 1)];
        animal.getAttributes().add(new Attribute(Manager.getAttributeTemplate(idRandom), (short) (Util.nextInt(1, Manager.getMaxValueAttributeAnimal(idRandom, animal.getLevel())) >= 2 ? 2 : 1)));
        player.getPoint().initPoint();
        InventoryService.instance.sendItemBody(player);
        InventoryService.instance.sendItemAnimal(player);
        Service.instance.sendMainCharInfo(player);
    }

    private void upgradeSpecialAttribute(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        short price = Manager.getAnimalTrainPrice(animal.getLevel());
        if (!player.getInventory().minusLuong(price)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(price)));
            return;
        }
        Attribute attributeToUpgrade = animal.getAttributes().get(animal.getAttributes().size() - 1);
        if (attributeToUpgrade == null) {
            return;
        }
        if (attributeToUpgrade.getValue() < Manager.getMaxValueAttributeAnimal(attributeToUpgrade.getTemplate().getId(), animal.getLevel())) {
            attributeToUpgrade.plusValue((short) 1);
            player.getPoint().initPoint();
            InventoryService.instance.sendItemBody(player);
            InventoryService.instance.sendItemAnimal(player);
            Service.instance.sendMainCharInfo(player);
        } else {
            Service.instance.sendLogOut(player.getSession(), "Đã nâng cấp tối đa");
        }
    }

    private void upgradeTiemNang(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        short price = Manager.getAnimalTrainPrice(animal.getLevel());
        if (!player.getInventory().minusLuong(price)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(price)));
            return;
        }
        Attribute attributeToUpgrade = null;
        for (short i = 5; i < animal.getAttributes().size() - 1; i++) {
            Attribute att = animal.getAttributes().get(i);
            if (att != null) {
                short valueSpecial = animal.getTemplate().getMaxValueSpecial(att.getTemplate().getId());
                short maxValue = Manager.getMaxValueAttributeAnimal(att.getTemplate().getId(), animal.getLevel());
                if (shouldUpgradeAttribute(animal.getLevel(), valueSpecial, att.getValue(), maxValue)) {
                    attributeToUpgrade = att;
                    break;
                }
            }
        }
        if (attributeToUpgrade == null) {
            Service.instance.sendLogOut(player.getSession(), "Đã nâng cấp tối đa");
            return;
        }
        attributeToUpgrade.plusValue((short) 1);
        player.getPoint().initPoint();
        InventoryService.instance.sendItemBody(player);
        InventoryService.instance.sendItemAnimal(player);
        Service.instance.sendMainCharInfo(player);
    }

    private boolean shouldUpgradeAttribute(byte level, short valueSpecial, short attributeValue, short maxValue) {
        if (level >= ItemEquipConst.MAX_LEVEL_ANIMAL) {
            if (valueSpecial == -1 && attributeValue < maxValue) {
                return true;
            }
            return valueSpecial != -1 && attributeValue < valueSpecial;
        } else {
            return attributeValue < maxValue;
        }
    }

    private void upgradeNormal(@NonNull Player player) throws IOException {
        ItemAnimal animal = player.getHorse().getAnimalUse();
        if (animal == null) {
            Service.instance.sendLogOut(player.getSession(), "Bạn phải cưỡi linh thú mà bạn muốn luyện");
            return;
        }
        short price = Manager.getAnimalTrainPrice(animal.getLevel());
        if (!player.getInventory().minusLuong(price)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(price)));
            return;
        }
        Attribute attributeToUpgrade = null;
        for (short i = 0; i < 3; i++) {
            Attribute att = animal.getAttributes().get(i);
            if (att != null && att.getValue() < Manager.getMaxValueAttributeAnimal(att.getTemplate().getId(), animal.getLevel())) {
                attributeToUpgrade = att;
                break;
            }
        }
        if (attributeToUpgrade == null) {
            Service.instance.sendLogOut(player.getSession(), "Đã nâng cấp tối đa");
            return;
        }
        attributeToUpgrade.plusValue((short) 1);
        player.getPoint().initPoint();
        InventoryService.instance.sendItemBody(player);
        InventoryService.instance.sendItemAnimal(player);
        Service.instance.sendMainCharInfo(player);
    }
    // </editor-fold>
}
