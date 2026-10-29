package vn.indiedev.tutien.game;

public final class KpahConstants {

    private KpahConstants() {}

    // 5 môn phái KPAH
    public static final int KIEM_KHACH = 0;
    public static final int CHIEN_BINH = 1;
    public static final int PHAP_SU = 2;
    public static final int DAU_SI = 3;
    public static final int CUNG_THU = 4;

    // Ngũ hành KPAH
    public static final int THUY = 0;
    public static final int MOC = 1;
    public static final int HOA = 2;
    public static final int THO = 3;
    public static final int KIM = 4;

    // Giới tính
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    // 3 Quốc gia KPAH
    public static final int DONG_SON = 0;
    public static final int NAM_SON = 1;
    public static final int BAC_SON = 2;

    public static String getClassName(int classId) {
        return switch (classId) {
            case KIEM_KHACH -> "Kiếm Khách";
            case CHIEN_BINH -> "Chiến Binh";
            case PHAP_SU -> "Pháp Sư";
            case DAU_SI -> "Đấu Sĩ";
            case CUNG_THU -> "Cung Thủ";
            default -> "Vô Danh";
        };
    }

    public static int getElement(int classId) {
        return switch (classId) {
            case KIEM_KHACH -> THUY;
            case CHIEN_BINH -> KIM;
            case PHAP_SU -> HOA;
            case DAU_SI -> THO;
            case CUNG_THU -> MOC;
            default -> THUY;
        };
    }

    public static String getElementName(int elementId) {
        return switch (elementId) {
            case THUY -> "Thủy";
            case MOC -> "Mộc";
            case HOA -> "Hỏa";
            case THO -> "Thổ";
            case KIM -> "Kim";
            default -> "Thủy";
        };
    }

    public static String getNationName(int nationId) {
        return switch (nationId) {
            case DONG_SON -> "Đông Sơn";
            case NAM_SON -> "Nam Sơn";
            case BAC_SON -> "Bắc Sơn";
            default -> "Đông Sơn";
        };
    }

    public static int getStarterWeaponId(int classId) {
        return switch (classId) {
            case KIEM_KHACH -> 79; // Kiếm tre
            case CHIEN_BINH -> 86; // Đao tre
            case PHAP_SU -> 93;   // Bút tre
            case DAU_SI -> 100;   // Thổ búa
            case CUNG_THU -> 107; // Cung tre
            default -> 79;
        };
    }

    public static int[] getStarterStats(int classId) {
        // [strength, agility, spirit, health, luck]
        return switch (classId) {
            case KIEM_KHACH -> new int[]{25, 20, 10, 25, 10};
            case CHIEN_BINH -> new int[]{30, 20, 10, 20, 10};
            case PHAP_SU -> new int[]{10, 25, 35, 10, 10};
            case DAU_SI -> new int[]{20, 30, 10, 20, 10};
            case CUNG_THU -> new int[]{20, 30, 15, 15, 10};
            default -> new int[]{20, 20, 20, 20, 10};
        };
    }
}
