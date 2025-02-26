package com.example.photoeditor;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageProcessor {

    // 将图片转换为灰度图
    public static Mat toGrayscale(Mat image) {
        Mat grayImage = new Mat(image.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    /**
     * 应用半调网屏效果到输入图像。
     * @param input 输入的灰度图像 (Mat 对象)
     * @return 处理后的半调网屏图像 (Mat 对象)
     */
    public static Mat applyHalftoneEffect(Mat input) {
        // 检查输入是否为灰度图像
        if (input.channels() != 1) {
            System.err.println("输入图像必须是灰度图像！");
            return null;
        }

        // 创建输出画布，初始化为白色背景
        Mat output = new Mat(input.size(), CvType.CV_8UC1, new Scalar(255));

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Runnable> tasks = new ArrayList<>();

        // 创建每个线程的任务
        for (int y = 0; y < input.rows(); y++) {
            int finalY = y;
            tasks.add(() -> processRow(input, output, finalY));
        }

        // 提交任务并等待完成
        tasks.forEach(executor::submit);
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return output;
    }

    private static void processRow(Mat input, Mat output, int y) {
        for (int x = 0; x < input.cols(); x++) {
            // 获取当前像素的灰度值
            double intensity = input.get(y, x)[0];

            // 如果像素较暗，则绘制一个点
            if (intensity < 128) {
                Imgproc.circle(output, new Point(x, y), 2, new Scalar(0), -1); // 绘制一个小圆
            }
        }
    }
}