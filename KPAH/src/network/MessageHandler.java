package network;

import services.Service;
import player.Player;
import daos.PlayerDAO;
import interfaces.ISession;
import item.ItemBuyNpc;
import lombok.NonNull;
import services.BuffService;
import services.ChatService;
import services.ClanService;
import services.FriendService;
import services.InventoryService;
import services.ItemService;
import services.LoginService;
import services.MapService;
import services.MenuOptionService;
import services.MonsterService;
import services.NpcService;
import services.PartyService;
import services.PopupService;
import services.ShopService;
import services.SkillService;
import services.TopService;
import services.TradeService;
import services.UseItemService;
import consts.ClanConst;
import consts.CombineConst;
import utils.CommandMessage;
import consts.Const;
import consts.ItemEquipConst;
import consts.NpcConst;
import item.ItemEquip;
import services.ChangeMapService;
import services.CombineService;
import services.DepositeService;
import services.TextBoxService;
import utils.Logger;
import utils.Printer;
import utils.Util;

public class MessageHandler {

    public void onMessage(@NonNull ISession session, @NonNull Message msg) {
        try {
            Player player = session.getPlayer();
            switch (msg.command) {
                case CommandMessage.REQUEST_SELL_ITEM -> {
                    if (player != null) {
                        boolean isSell = msg.reader().readBoolean();
                        byte typeNpc = msg.reader().readByte();
                        byte indexShop = msg.reader().readByte();
                        short idItem = msg.reader().readShort();
                        int price = msg.reader().readInt();
                        byte typeItem = msg.reader().readByte();
                        DepositeService.instance.requestSellItem(player, isSell, typeNpc, indexShop, idItem, price, typeItem);
                    }
                }
                case CommandMessage.GET_DEPOSITE_ITEM -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        byte npcType = msg.reader().readByte();
                        byte indexShop = msg.reader().readByte();
                        short idChar = -1;
                        if (type == 1) {
                            idChar = msg.reader().readShort();
                        }
                        DepositeService.instance.onDepositeItem(player, type, idChar, npcType, indexShop);
                    }
                }
                case CommandMessage.BUY_DEPOSITE_ITEM -> {
                    if (player != null) {
                        byte npcId = msg.reader().readByte();
                        byte shopId = msg.reader().readByte();
                        short charId = msg.reader().readShort();
                        short idItem = msg.reader().readShort();
                        byte type = msg.reader().readByte();
                        ShopService.instance.buyItemDeposite(player, npcId, shopId, charId, idItem, type);
                    }
                }
                case CommandMessage.RIDE_ANIMAL -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        short id = msg.reader().readShort();
                        UseItemService.instance.riderAnimal(player, id, type);
                    }
                }
                case CommandMessage.TEXT_BOX -> {
                    if (player != null) {
                        msg.reader().readShort();
                        TextBoxService.instance.onTextBox(player, msg.reader().readByte(), msg.reader().readUTF());
                    }
                }
                case CommandMessage.KILLER -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        MapService.instance.onChangeKiller(player);
                    }
                }
                case CommandMessage.DOWN_HORSE -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        byte type = msg.reader().readByte();
                        switch (type) {
                            case 0 ->
                                MapService.instance.onDownHorse(player);
                            case 1 -> {
                            }
                            case 2 -> {
                                if (player.getInventory().isFullInventory()) {
                                    Service.instance.sendLogOut(session, "Hành trang không đủ ô trống");
                                    return;
                                }
                                ItemEquip cuoc = InventoryService.instance.findItemBodyByType(player, (byte) 13);
                                if (cuoc != null) {
                                    InventoryService.instance.removeItemBodyEquipment(player, cuoc);
                                    InventoryService.instance.addItemBagEquipment(player, cuoc);
                                    InventoryService.instance.sendItemBag(player);
                                    InventoryService.instance.sendItemBody(player);
                                }
                            }
                        }
                    }
                }
                case CommandMessage.TACH_NGUYEN_LIEU -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        byte type = msg.reader().readByte();
                        short indexItem = msg.reader().readShort();
                        short idMaterial = msg.reader().readShort();
                        byte iLock = msg.reader().readByte();
                        if (type == CombineConst.THEM_THUOC_TINH) {

                        } else {
                            CombineService.instance.doTachNguyenLieu(player, type, indexItem, idMaterial, iLock);
                        }
                    }
                }
                case CommandMessage.TRADE -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        TradeService.instance.doTrade(player, msg);
                    }
                }
                case CommandMessage.CREATE_PARTY -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        msg.reader().readInt();
                        PartyService.instance.doActionInvite(player, PartyService.CREATE_PARTY, (short) -1);
                    }
                }
                case CommandMessage.REMOVE_FRIEND -> {
                    if (player != null) {
                        FriendService.instance.removeFriend(player, msg.reader().readUTF());
                    }
                }
                case CommandMessage.MESSAGE_PRIVATE -> {
                    if (player != null) {
                        ChatService.instance.sendChatPrivate(player, msg.reader().readUTF(), msg.reader().readUTF());
                    }
                }
                case CommandMessage.CHOOSE_ICON_CLAN -> {
                    if (player != null) {
                        ClanService.instance.confirmRegisterClan(player, msg.reader().readShort(), msg.reader().readUTF());
                    }
                }
                case CommandMessage.TOP_STRONGER_RICHER -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        byte page = msg.reader().readByte();
                        TopService.instance.doShowTop(player, type, page);
                    }
                }
                case CommandMessage.TRANS_MONEY_CLAN -> {
                    if (player != null) {
                        ClanService.instance.transMoney(player, msg.reader().readInt());
                    }
                }
                case CommandMessage.CHAT_CLAN -> {
                    if (player != null) {
                        ClanService.instance.chatAllClan(player, msg.reader().readUTF());
                    }
                }
                case CommandMessage.EVICTION_CLAN -> {
                    if (player != null) {
                        ClanService.instance.kickMember(player, msg.reader().readUTF());
                    }
                }
                case CommandMessage.MESSAGE_CLAN -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        String text = "";
                        int idMsg = 0;
                        if (type == ClanConst.CHAT) {
                            text = msg.reader().readUTF();
                        } else if (type == ClanConst.REMOVE_CHAT) {
                            idMsg = msg.reader().readInt();
                        }
                        ClanService.instance.onChat(player, type, text, idMsg);
                    }
                }
                case CommandMessage.DISSOLVE_CLAN -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        ClanService.instance.dissolveClan(player);
                    }
                }
                case CommandMessage.OUT_CLAN -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        ClanService.instance.leaveClan(player);
                    }
                }
                case CommandMessage.CLAN_INFO -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short idClan = msg.reader().readShort();
                        ClanService.instance.sendInfoClan(player, idClan);
                    }
                }
                case CommandMessage.ADD_CLAN -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        short idPlayer = msg.reader().readShort();
                        boolean confirm = false;
                        if (type == 1) {
                            confirm = msg.reader().readBoolean();
                        }
                        ClanService.instance.onActionInvite(player, type, idPlayer, confirm);
                    }
                }
                case CommandMessage.CLAN_LIST -> {
                    if (player != null) {
                        ClanService.instance.sendMembers(player);
                    }
                }
                case CommandMessage.REG_CLAN -> {
                    if (player != null) {
                        ClanService.instance.doRegisterClan(player);
                    }
                }
                case CommandMessage.ADD_TO_PARTY -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        byte type = msg.reader().readByte();
                        short idPlayerInvited = msg.reader().readShort();
                        PartyService.instance.doActionInvite(player, type, idPlayerInvited);
                    }
                }
                case CommandMessage.KICK_PARTY -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        byte type = msg.reader().readByte();
                        short idPlayerInvited = msg.reader().readShort();
                        PartyService.instance.doActionKick(player, type, idPlayerInvited);
                    }
                }
                case CommandMessage.ADD_FRIEND -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short idPlayerAdd = msg.reader().readShort();
                        byte ok = msg.reader().readByte();
                        FriendService.instance.doAction(player, ok, idPlayerAdd);
                    }
                }
                case CommandMessage.VIEW_INFO -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short idPlayerView = msg.reader().readShort();
                        byte type = msg.reader().readByte();
                        MapService.instance.sendViewInfo(player, type, idPlayerView);
                    }
                }
                case CommandMessage.GET_WEAPONE -> {
                    byte type = msg.reader().readByte();
                    byte weaponType = msg.reader().readByte();
                    byte weaponStyle = msg.reader().readByte();
                    byte index = msg.reader().readByte();
                    if (type == 0) {
                        Service.instance.sendWeaponData(session, weaponType, weaponStyle, index);
                    }
                }
                case CommandMessage.MENU_OPTION -> {
                    if (player != null) {
                        msg.reader().readShort();
                        byte idMenu = msg.reader().readByte();
                        byte selected = msg.reader().readByte();
                        MenuOptionService.instance.onMenuOption(player, idMenu, selected);
                    }
                }
                case CommandMessage.BUY_ITEM_FROM_NPC -> {
                    if (player != null) {
                        byte sizeItems = msg.reader().readByte();
                        for (int i = 0; i < sizeItems; i++) {
                            byte category = msg.reader().readByte();
                            short id = msg.reader().readShort();
                            short quantity = msg.reader().readShort();
                            byte classChar = -1;
                            if (category == Const.CATEGORY_ITEM) {
                                classChar = msg.reader().readByte();
                            }
                            if (quantity > 0 && id > 0) {
                                player.getSundry().getItemNpcShop().add(new ItemBuyNpc(category, id, quantity, classChar));
                            }
                        }
                        ShopService.instance.buyItemNpcShop(player, player.getSundry().getSelectedOption());
                    }
                }
                case CommandMessage.BUY_ITEM_SPECIAL -> {
                    if (player != null) {
                        int idItem = msg.reader().readUnsignedByte();
                        ShopService.instance.buyItemSpecial(player, (short) idItem);
                    }
                }
                case CommandMessage.DELL_GEM_ITEM -> {
                    if (player != null) {
                        byte typeGem = msg.reader().readByte();
                        short idGem = msg.reader().readShort();
                        if (typeGem == Const.DROP_GEM_ITEM) {
                            boolean isLockItem = msg.reader().readByte() == 1;
                            if (isLockItem) {
                                InventoryService.instance.removeItemGemLock(player, InventoryService.instance.findItemGemLock(player, idGem));
                                InventoryService.instance.sendItemGemLock(player);
                                return;
                            }
                            InventoryService.instance.removeItemGem(player, InventoryService.instance.findItemGem(player, idGem));
                            InventoryService.instance.sendItemGem(player);
                        }
                    }
                }
                case CommandMessage.UP_TO_BOARD -> {
                    if (player != null) {
                        MapService.instance.sendUpToBoard(player);
                    }
                }
                case CommandMessage.BUY_TICKET -> {
                    if (player != null) {
                        ShopService.instance.buyTicket(player);
                    }
                }
                case CommandMessage.SELL_ITEM -> {
                    if (player != null) {
                        short indexItem = msg.reader().readShort();
                        ShopService.instance.onSellItem(player, indexItem);
                    }
                }
                case CommandMessage.MOVE_TO_MAP -> {
                    if (player != null) {
                        byte index = msg.reader().readByte();
                        short mapId = msg.reader().readShort();
                        ChangeMapService.instance.changeMapByXaPhu(player, index, mapId);
                    }
                }
                case CommandMessage.REPAIR_ITEM -> {
                    if (player != null) {
                        InventoryService.instance.repairItem(player, msg.reader().readByte());
                    }
                }
                case CommandMessage.GIVE_ITEM_TO_GROUND -> {
                    if (player != null) {
                        short idItem = msg.reader().readShort();
                        ItemEquip item = InventoryService.instance.findItemBag(player, idItem);
                        if (item == null) {
                            return;
                        }
                        if (item.getTemplate().getNdayLoan() != 0) {
                            Service.instance.sendLogOut(session, "Không thể vứt đồ thuê");
                            InventoryService.instance.sendItemBag(player);
                            return;
                        }
                        InventoryService.instance.removeItemBagEquipment(player, item);
                        item.dispose();
                        InventoryService.instance.sendItemBag(player);
                    }
                }
                case CommandMessage.DELL_POTION -> {
                    if (player != null) {
                        short potionType = msg.reader().readShort();
                        InventoryService.instance.removeItemPotion(player, InventoryService.instance.findItemPotion(player, (byte) potionType));
                        InventoryService.instance.sendItemPotion(player);
                    }
                }
                case CommandMessage.BUY_GEM_ITEM_FROM_NPC -> {
                    if (player != null) {
                        short num = msg.reader().readShort();
                        for (int i = 0; i < num; i++) {
                            short gemId = msg.reader().readShort();
                            player.getSundry().getItemNpcShop().add(new ItemBuyNpc(Const.CATEGORY_GEM_ITEM, gemId, (short) 1, (byte) -1));
                        }
                        ShopService.instance.buyItemNpcShop(player, ItemEquipConst.DAMAGE_NONE);
                    }
                }
                case CommandMessage.LEAR_SKILL -> {
                    if (player != null) {
                        msg.reader().readByte();
                        byte indexSkill = msg.reader().readByte();
                        SkillService.instance.learnNewSkill(player, indexSkill);
                    }
                }
                case CommandMessage.GET_ITEM_OUT_BAG -> {
                    if (player != null) {
                        short index = msg.reader().readShort();
                        InventoryService.instance.getItemEquipmentFromBox(player, index);
                    }
                }
                case CommandMessage.PUT_ITEM_2_BAG -> {
                    if (player != null) {
                        short index = msg.reader().readShort();
                        InventoryService.instance.addItemBoxEquipment(player, index);
                    }
                }
                case CommandMessage.NPC_INFO -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        byte idType = 0;
                        if (type == NpcConst.THIET_BI || type == NpcConst.GIAP_SU || (type > NpcConst.HOA_TIEU && type != 30)) {
                            idType = msg.reader().readByte();
                        }
                        NpcService.instance.onNpcInfo(player, type, idType);
                    }
                }
                case CommandMessage.MONSTER_INFO -> {
                    if (player != null) {
                        short id = msg.reader().readShort();
                        MonsterService.instance.sendMonsterInfo(player, id);
                    }
                }
                case CommandMessage.CHAT -> {
                    if (player != null) {
                        String chat = msg.reader().readUTF();
                        if (Util.isNullOrEmpty(chat)) {
                            return;
                        }
                        ChatService.instance.sendChat(session.getPlayer(), chat);
                    }
                }
                case CommandMessage.GET_INFO_TEMPLATE -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        short id = msg.reader().readShort();
                        MonsterService.instance.sendMonsterTemplate(player, type, id);
                    }
                }
                case CommandMessage.CHANGE_MAP -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        MapService.instance.doChangeMap(player, msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort());
                    }
                }
                case CommandMessage.LOAD_IMAGE_MONSTER -> {
                    if (player != null) {
                        msg.reader().readByte();
                        msg.reader().readByte();
                        MonsterService.instance.sendMonsterImage(player, msg.reader().readShort());
                    }
                }
                case CommandMessage.CHAR_INFO -> {
                    if (player != null) {
                        short id = msg.reader().readShort();
                        MapService.instance.sendInfoPlayer(player, id);
                    }
                }
                case CommandMessage.ATTACK_MULTI_MONSTER -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        byte skillType = msg.reader().readByte();
                        byte sizeTarget = msg.reader().readByte();
                        short[] idMobs = new short[sizeTarget];
                        for (int i = 0; i < sizeTarget; i++) {
                            idMobs[i] = msg.reader().readShort();
                        }
                        SkillService.instance.useSkillToMob(player, skillType, idMobs);
                    }
                }
                case CommandMessage.PLAYER_ATTACK_MONSTER -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short id = msg.reader().readShort();
                        byte skillType = msg.reader().readByte();
                        SkillService.instance.useSkillToMob(player, skillType, id);
                    }
                }
                case CommandMessage.USE_BUFF -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        msg.reader().readByte();
                        short id = msg.reader().readShort();
                        msg.reader().readByte();
                        byte skillType = msg.reader().readByte();
                        msg.reader().readShort();
                        BuffService.instance.useSkillBuff(player, skillType, id);
                    }
                }
                case CommandMessage.PLAYER_ATTACK_PLAYER -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short id = msg.reader().readShort();
                        byte skillType = msg.reader().readByte();
                        SkillService.instance.useSkillToPlayer(player, skillType, id);
                    }
                }
                case CommandMessage.GET_IMAGE -> {
                    if (player != null) {
                        byte type = msg.reader().readByte();
                        byte ver = msg.reader().readByte();
                        if (ver == 0) {
                            Service.instance.sendImage(player, type);
                        }
                    }
                }
                case CommandMessage.RQ_MAINCHAR_INFO -> {
                    if (player != null) {
                        Service.instance.sendMainCharInfo(player);
                    }
                }
                case CommandMessage.MOVE_CHAR -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        MapService.instance.checkMove(player, msg.reader().readShort(), msg.reader().readShort());
                    }
                }
                case CommandMessage.EFFECT_OBJ -> {
                    if (player != null) {
                        Service.instance.sendEffectObject(player, msg.reader().readByte());
                    }
                }
                case CommandMessage.IMAGE_SERVER -> {
                    short id = msg.reader().readShort();
                    Service.instance.sendIcon(session, id);
                }
                case CommandMessage.CREATE_CHAR -> {
                    String name = msg.reader().readUTF();
                    byte clazz = msg.reader().readByte();
                    byte head = msg.reader().readByte();
                    byte gender = msg.reader().readByte();
                    byte idNation = msg.reader().readByte();
                    if (session.getListChar().size() >= 3) {
                        return;
                    }
                    PlayerDAO.createPlayer(session, name, clazz, head, gender, idNation);
                }
                case CommandMessage.REQUEST_REGISTER -> {
                    Service.instance.sendLogOut(session, "Hiện tại không hỗ trợ chơi mới!");
                }
                case CommandMessage.CHARLIST -> {
                    LoginService.instance.selectChar(session, msg.reader().readByte(), msg.reader().readInt());
                }
                case CommandMessage.LOAD_IMAGE_TREE -> {
                    byte idTree = msg.reader().readByte();
                    if (idTree == -1) {
                        Service.instance.sendImageCloth(session, msg.reader().readByte(), msg.reader().readByte());
                    } else {
                        Service.instance.sendImageTree(session, idTree);
                    }
                }
                case CommandMessage.LOGIN -> {
                    session.loginAccount(msg);
                }
                case CommandMessage.SET_CLIENT_TYPE -> {
                    session.setZoomLevel(msg);
                }
                case CommandMessage.USE_POTION -> {
                    if (player != null) {
                        byte idPotion = msg.reader().readByte();
                        UseItemService.instance.useItemPotion(player, idPotion);
                    }
                }
                case CommandMessage.USE_ITEM -> {
                    if (player != null) {
                        short indexItem = msg.reader().readShort();
                        UseItemService.instance.useItemEquipment(player, indexItem);
                    }
                }
                case CommandMessage.ITEM_INFO -> {
                    if (player != null) {
                        short index = msg.reader().readShort();
                        short idTemplate = msg.reader().readShort();
                        ItemService.instance.sendItemInfo(player, index, idTemplate);
                    }
                }
                case CommandMessage.GET_GEM_FROM_GROUND -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        byte type = msg.reader().readByte();
                        short id = msg.reader().readShort();
                        switch (type) {
                            case Const.CATEGORY_SPECIAL_ITEM -> {
                            }
                            case Const.CATEGORY_GEM_ITEM ->
                                MapService.instance.getGemFromGround(player, id);
                        }
                    }
                }
                case CommandMessage.GET_POTION_FROM_GROUND -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short id = msg.reader().readShort();
                        MapService.instance.getPotionFromGround(player, id);
                    }
                }
                case CommandMessage.GET_ITEM_FROM_GROUND -> {
                    if (player != null && player.getLocation().getZone() != null) {
                        short id = msg.reader().readShort();
                        MapService.instance.getItemEquipmentFromGround(player, id);
                    }
                }
                case CommandMessage.ADD_BASE_POINT -> {
                    if (player != null) {
                        player.getPoint().increaseBasePoint(msg.reader().readByte(), msg.reader().readShort());
                    }
                }
                case CommandMessage.ADD_SKILL_POINT -> {
                    if (player != null) {
                        player.getPoint().increaseSkillPoint(msg.reader().readByte());
                    }
                }
                case CommandMessage.COME_HOME -> {
                    if (player != null) {
                        MapService.instance.comeHome(player);
                    }
                }
                case CommandMessage.CUSTOM_POPUP -> {
                    if (player != null) {
                        short id = msg.reader().readShort();
                        byte idPop = msg.reader().readByte();
                        msg.reader().readUTF();
                        byte button = msg.reader().readByte();
                        if (id == player.getIdPlayer()) {
                            PopupService.instance.doAction(player, idPop, button);
                        }
                    }
                }
                case CommandMessage.FINISH_PUT_ITEM_2_BAG, CommandMessage.CONFIG, CommandMessage.QUEST_CLAN -> {
                }
                default -> {
                    Printer.printRed("CMD Function Not Found: " + msg.command);
                }
            }
        } catch (Exception e) {
            Logger.logError("Lỗi Message Handler", e);
            session.disconnect();
        }
    }
}
