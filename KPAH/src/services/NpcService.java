package services;

import java.io.IOException;
import lombok.NonNull;
import player.Player;
import consts.ItemEquipConst;
import utils.Printer;
import consts.NpcConst;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class NpcService {

    public static final NpcService instance = new NpcService();

    public void onNpcInfo(@NonNull Player player, byte type, byte idType) throws IOException {
        if (idType < 0) {
            return;
        }
        player.getSundry().setIdNpcOpen(type);
        player.getSundry().setSelected(idType);
        switch (type) {
            case NpcConst.NHAT_GIAP, NpcConst.NHI_GIAP, NpcConst.TAM_GIAP, NpcConst.TU_GIAP, NpcConst.NGU_GIAP, NpcConst.NHAT_NGUU ->
                DepositeService.instance.sendGianHang(player, idType);
            case NpcConst.HOA_TIEU_NEW_2 ->
                MenuOptionService.instance.sendMenuHoaTieu2(player);
            case NpcConst.HOA_TIEU_NEW ->
                MenuOptionService.instance.sendMenuHoaTieu(player);
            case NpcConst.XA_PHU_NEW ->
                MenuOptionService.instance.sendMenuXaPhuNew(player);
            case NpcConst.CONG_DICH_CHUYEN ->
                MenuOptionService.instance.sendMenuCongDichChuyen(player);
            case NpcConst.HAO_DUYEN ->
                MenuOptionService.instance.sendMenuHaoDuyen(player);
            case NpcConst.GIAP_SU ->
                MenuOptionService.instance.sendOptionBuyItem(player);
            case NpcConst.KIEM_SU ->
                ShopService.instance.openNpcShop(player, "KIEM_SU", ItemEquipConst.DAMAGE_NONE);
            case NpcConst.BAO_NGOC ->
                ShopService.instance.openNpcShop(player, "BAO_NGOC", ItemEquipConst.DAMAGE_NONE);
            case NpcConst.PHU_ONG, NpcConst.BOI_CHAU ->
                ShopService.instance.openNpcShop(player, "GEM_SHOP", ItemEquipConst.DAMAGE_NONE);
            case NpcConst.AN_TAM, NpcConst.ANH_BAY ->
                InventoryService.instance.sendOpenBox(player);
            case NpcConst.HAC_NGUU ->
                MenuOptionService.instance.sendOptionShopHacNguu(player);
            case NpcConst.KIM_HOA, NpcConst.BA_TAM_TAP_HOA ->
                ShopService.instance.openNpcShop(player, "POTION", ItemEquipConst.DAMAGE_NONE);
            case NpcConst.THIET_BI ->
                MenuOptionService.instance.sendOptionBuyItem(player);
            case NpcConst.THAY_NGU_HANH ->
                MenuOptionService.instance.sendMenuThayNguHanh(player);
            case NpcConst.THO_HOP_THANH_SO_CAP ->
                MenuOptionService.instance.sendMenuThoHopThanhSoCap(player);
            case NpcConst.THO_HOP_THANH_CAO_CAP ->
                MenuOptionService.instance.sendMenuThoHopThanhCaoCap(player);
            case NpcConst.DAU_TRUONG ->
                MenuOptionService.instance.sendMenuDauTruong(player);
            case NpcConst.TONG_QUAN ->
                MenuOptionService.instance.sendMenuTongQuan(player);
            case NpcConst.THO_REN_THAN_BI ->
                MenuOptionService.instance.sendMenuThoRenThanBi(player);
            case NpcConst.TONG_TIEU_DAU ->
                MenuOptionService.instance.sendMenuTongTieuDau(player);
            default ->
                Printer.printRed("Npc Function Not Found: " + type);
        }
    }
}
