package com.example.photoeditor;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GCode {
    private static final int MAX_POWER = 255; // 激光最大功率

    //生成 G 代码
    public static String generateGCode(Mat image) {
        int maxPower = 1000; // 激光最大功率
        StringBuilder gcode = new StringBuilder();
        gcode.append("G0 X0 Y0 F" + 1000 + "\n"); // 快速移动到起点
        gcode.append("M4 S0\n"); // 打开激光但功率为 0

        Mat grayImage = ImageProcessor.toGrayscale(image); // 转换为灰度图
        int rows = grayImage.rows();
        int cols = grayImage.cols();

        for (int y = 0; y < rows; y++) {
            boolean rowStarted = false;
            for (int x = 0; x < cols; x++) {
                double grayValue = grayImage.get(y, x)[0];
                int power = (int) ((255 - grayValue) / 255.0 * maxPower);

                if (power > 0) {
                    if (!rowStarted) {
                        gcode.append("G0 X" + x + " Y" + y + " S0\n"); // 快速移动到起始点
                        rowStarted = true;
                    }
                    gcode.append("G1 X" + x + " Y" + y + " S" + power + "\n"); // 开始雕刻
                } else {
                    if (rowStarted) {
                        gcode.append("S0\n"); // 关闭激光
                        rowStarted = false;
                    }
                }
            }
        }

        gcode.append("G0 X0 Y0\n"); // 返回起点
        gcode.append("M5\n"); // 关闭激光
        grayImage.release();
        return gcode.toString();
    }

    //保存 G 代码到文件
    public static void saveGCodeToFile(String gcode, Context context) {
        try {
            // 获取当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDate = sdf.format(new Date());

            // 创建文件对象，文件名包含当前时间
            File file = new File(context.getExternalFilesDir(null), "output_" + currentDate + ".nc");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(gcode.getBytes());
            fos.close();
            System.out.println("G 代码已保存: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}