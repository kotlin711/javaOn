import com.aparapi.Kernel;
import com.aparapi.Range;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PPMImagegray {


    public static PPM readPPM3(String path) throws IOException {
        PPM ppm = new PPM();
        List<String> list = Files.readAllLines(Paths.get(path));
        String[] s = list.get(1).split(" ");
        ppm.setW(Integer.parseInt(s[0]));
        ppm.setH(Integer.parseInt(s[1]));
        list.remove(0);
        list.remove(0);
        list.remove(0);
        int[] R = new int[ppm.w * ppm.h];
        int[] G = new int[ppm.w * ppm.h];
        int[] B = new int[ppm.w * ppm.h];
        List<Integer> rgb = new ArrayList<>();
        for (String s1 : list) {
            String[] s2 = s1.split(" ");
            for (String s3 : s2) {
                rgb.add(Integer.valueOf(s3));
            }
        }
        int count = 0;
        for (int i = 0; i < ppm.w * ppm.h; i++) {
            R[i] = rgb.get(count);
            G[i] = rgb.get(count + 1);
            B[i] = rgb.get(count + 2);
            count += 3;
        }
        ppm.setR(R);
        ppm.setG(G);
        ppm.setB(B);
        return ppm;
    }

    public static void writePPM3(PPM ppm, String path) throws IOException {

        BufferedWriter out = new BufferedWriter(new FileWriter(path));
        out.append(String.format("P3\n%d %d\n255\n", ppm.w, ppm.h));

        for (int i = 0; i < ppm.w * ppm.h; i++) {
            out.append(String.format("%d %d %d ", ppm.r[i], ppm.g[i], ppm.b[i]));
        }

        out.close();


    }

    static class PPM {
        private int w;
        private int h;
        private int[] r;
        private int[] g;
        private int[] b;

        public PPM(int w, int h, int[] r, int[] g, int[] b) {
            this.w = w;
            this.h = h;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public PPM() {

        }

        public int getW() {
            return w;
        }

        public void setW(int w) {
            this.w = w;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }

        public int[] getR() {
            return r;
        }

        public void setR(int[] r) {
            this.r = r;
        }

        public int[] getG() {
            return g;
        }

        public void setG(int[] g) {
            this.g = g;
        }

        public int[] getB() {
            return b;
        }

        public void setB(int[] b) {
            this.b = b;
        }
    }


    public static PPM grayAvg(final int[] R, final int[] G, final int[] B, final int W, final int H) {
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int idx = getGroupId(0) * getLocalSize(0) + getLocalId(0);
                int idy = getGroupId(1) * getLocalSize(1) + getLocalId(1);
                if (idx < W && idy < H) {
                    int tid = idy * W + idx;
                    int gray = (R[tid] + G[tid] + B[tid]) / 3;
                    R[tid] = gray;
                    G[tid] = gray;
                    B[tid] = gray;
                }

            }
        };
        kernel.execute(Range.create2D(W, H));
        kernel.dispose();
        return new PPM(W, H, R, G, B);
    }

    public static PPM grayMax(final int[] R, final int[] G, final int[] B, final int W, final int H) {
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int idx = getGroupId(0) * getLocalSize(0) + getLocalId(0);
                int idy = getGroupId(1) * getLocalSize(1) + getLocalId(1);
                if (idx < W && idy < H) {
                    int tid = idy * W + idx;
                    int i = (R[tid] >= G[tid]) ? R[tid] : G[tid];
                    int gray = (i >= B[tid]) ? i : B[tid];
                    R[tid] = gray;
                    G[tid] = gray;
                    B[tid] = gray;
                }

            }
        };
        kernel.execute(Range.create2D(W, H));
        kernel.dispose();
        return new PPM(W, H, R, G, B);
    }

    public static PPM grayWAvg(final int[] R, final int[] G, final int[] B, final int W, final int H) {
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int idx = getGroupId(0) * getLocalSize(0) + getLocalId(0);
                int idy = getGroupId(1) * getLocalSize(1) + getLocalId(1);
                if (idx < W && idy < H) {
                    int tid = idy * W + idx;
                    int gray = (int) ((0.3 * R[tid]) + (0.59 * G[tid]) + (0.11 * B[tid]));
                    R[tid] = gray;
                    G[tid] = gray;
                    B[tid] = gray;
                }

            }
        };
        kernel.execute(Range.create2D(W, H));
        kernel.dispose();
        return new PPM(W, H, R, G, B);
    }

    public static void main(String[] args) throws IOException {

        PPM ppm = readPPM3("data/im.ppm");
        PPM gray = grayAvg(ppm.r, ppm.g, ppm.b, ppm.w, ppm.h);
        writePPM3(gray, "data/out.ppm");
        PPM gray1 = grayMax(ppm.r, ppm.g, ppm.b, ppm.w, ppm.h);
        writePPM3(gray1, "data/out1.ppm");
        PPM gray2 = grayWAvg(ppm.r, ppm.g, ppm.b, ppm.w, ppm.h);
        writePPM3(gray2, "data/out2.ppm");
        //
    }

}
