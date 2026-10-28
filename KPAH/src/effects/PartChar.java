package effects;

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
public class PartChar {

    private byte id;
    private byte type;
    private short avxf;
    private short avyf;
    private short avx0;
    private short avy0;
    private short avw0;
    private short avh0;
    private byte[][] x;
    private byte[][] y;
    private byte[][] w;
    private byte[][] h;
    private byte[][] dx;
    private byte[][] dy;
    private byte[] data;

    public void setData() throws IOException {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(bas)) {
            dos.writeByte(avx0);
            dos.writeByte(avy0);
            dos.writeByte(avw0);
            dos.writeByte(avh0);
            dos.writeByte(avxf);
            dos.writeByte(avyf);
            if (type < 4) {
                for (int k = 0; k < 4; k++) {
                    for (int l = 0; l < x[k].length; l++) {
                        dos.write(x[k][l]);
                        dos.write(y[k][l]);
                        dos.write(w[k][l]);
                        dos.write(h[k][l]);
                    }
                    for (int m = 0; m < 6; m++) {
                        dos.writeByte(dx[k][m]);
                        dos.writeByte(dy[k][m]);
                    }
                }
            } else {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < x[k].length; l++) {
                        dos.write(x[k][l]);
                        dos.write(y[k][l]);
                        dos.write(w[k][l]);
                        dos.write(h[k][l]);
                    }
                }
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 6; j++) {
                        dos.writeByte(dx[i][j]);
                        dos.writeByte(dy[i][j]);
                    }
                }
            }
            data = bas.toByteArray();
        }
    }
}
