package effects;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import consts.Const;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class EffectData {

    private short id;
    private byte typeEffect;
    private short idSpecial;
    private byte[] arrFrame;
    private ImageInfo[] imageInfo;
    private PartFrame[][] frame;
    private byte[] imageData;
    private Animation[] animation;
    private byte[] data;

    public void setData() throws IOException {
        if (typeEffect == Const.NORMAL_EFFECT) {
            imageData = Util.readFile(String.format("data//image//effect//map//%s.png", id));
        }
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(bas)) {
            dos.writeByte(imageInfo.length);
            for (ImageInfo imageInfo1 : imageInfo) {
                dos.writeByte(imageInfo1.getID());
                dos.writeByte(imageInfo1.getX0());
                dos.writeByte(imageInfo1.getY0());
                dos.writeByte(imageInfo1.getW());
                dos.writeByte(imageInfo1.getH());
            }
            switch (typeEffect) {
                case Const.DYNAMIC_EFFECT -> {
                    dos.writeShort(frame.length);
                    for (PartFrame[] partFrames : frame) {
                        dos.writeByte(partFrames.length);
                        for (PartFrame part : partFrames) {
                            dos.writeShort(part.getDx());
                            dos.writeShort(part.getDy());
                            dos.writeByte(part.getIdSmallImg());
                        }
                    }
                    dos.writeShort(arrFrame.length);
                    for (byte frameChild : arrFrame) {
                        dos.writeShort(frameChild);
                    }
                    dos.writeShort(0);
                    dos.writeShort(0);
                    for (Animation animationChild : animation) {
                        dos.writeByte(animationChild.getFrame().length);
                        dos.write(animationChild.getFrame());
                    }
                }
                case Const.PET_EFFECT -> {
                    dos.writeShort(frame.length);
                    for (PartFrame[] partFrames : frame) {
                        dos.writeByte(partFrames.length);
                        for (PartFrame part : partFrames) {
                            dos.writeShort(part.getDx());
                            dos.writeShort(part.getDy());
                            dos.writeByte(part.getIdSmallImg());
                            dos.writeByte(part.getFlip());
                            dos.writeByte(part.getOnTop());
                        }
                    }
                    dos.writeShort(arrFrame.length);
                    for (byte frameChild : arrFrame) {
                        dos.writeShort(frameChild);
                    }
                    for (int i = 0; i < 8; i++) {
                        Animation animationChild = animation[i];
                        dos.writeByte(animationChild.getFrame().length);
                        dos.write(animationChild.getFrame());
                    }
                    dos.writeByte(idSpecial);
                    for (PartFrame[] frame1 : frame) {
                        dos.writeByte(frame1[0].getXShadow());
                        dos.writeByte(frame1[0].getYShadow());
                    }
                    if (animation.length > 8) {
                        for (int i = 8; i < animation.length; i++) {
                            Animation animationChild = animation[i];
                            dos.writeByte(animationChild.getFrame().length);
                            dos.write(animationChild.getFrame());
                        }
                    }
                }
                case Const.SKILL_EFFECT -> {
                    dos.writeShort(frame.length);
                    for (PartFrame[] partFrames : frame) {
                        dos.writeByte(partFrames.length);
                        for (PartFrame part : partFrames) {
                            dos.writeShort(part.getDx());
                            dos.writeShort(part.getDy());
                            dos.writeByte(part.getIdSmallImg());
                            dos.writeByte(part.getFlip());
                            dos.writeByte(part.getOnTop());
                        }
                    }
                    dos.writeShort(arrFrame.length);
                    for (byte frameChild : arrFrame) {
                        dos.writeShort(frameChild);
                    }
                    dos.writeByte(idSpecial);
                    dos.writeByte(0);
                    dos.writeByte(0);
                    dos.writeByte(0);
                }
                case Const.THAN_THU_EFFECT -> {
                    dos.writeShort(frame.length);
                    for (PartFrame[] partFrames : frame) {
                        dos.writeByte(partFrames.length);
                        for (PartFrame part : partFrames) {
                            dos.writeShort(part.getDx());
                            dos.writeShort(part.getDy());
                            dos.writeByte(part.getIdSmallImg());
                            dos.writeByte(part.getFlip());
                            dos.writeByte(part.getOnTop());
                        }
                    }
                    dos.writeShort(arrFrame.length);
                    for (byte frameChild : arrFrame) {
                        dos.writeShort(frameChild);
                    }
                    for (Animation animationChild : animation) {
                        dos.writeByte(animationChild.getFrame().length);
                        dos.write(animationChild.getFrame());
                    }
                }
            }
            data = bas.toByteArray();
        }
    }
}
