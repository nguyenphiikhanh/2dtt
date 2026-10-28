package template;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class TreeInfo {

    private byte id;
    private byte dx;
    private byte dy;
    private byte startX;
    private byte startY;
    private byte endX;
    private byte endY;

    private byte[] data;

    public void setData() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {
            outputStream.writeShort(6);
            outputStream.writeByte(dx);
            outputStream.writeByte(dy);
            outputStream.writeByte(startX);
            outputStream.writeByte(startY);
            outputStream.writeByte(endX);
            outputStream.writeByte(endY);
            data = byteArrayOutputStream.toByteArray();
        }
    }
}
