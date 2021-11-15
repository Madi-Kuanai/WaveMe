package com.mzmm.wave_me.recognize;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class objectDetectorClass {
    // should start from small letter

    // this is used to load model and predict
    private Interpreter interpreter;
    private Interpreter interpreter2;

    // store all label in array
    private List<String> labelList;
    private int INPUT_SIZE;
    private int PIXEL_SIZE = 3; // for RGB
    private int IMAGE_MEAN = 0;
    private float IMAGE_STD = 255.0f;
    // use to initialize gpu in app
    private GpuDelegate gpuDelegate = new GpuDelegate();
    private int height = 0;
    private int width = 0;
    private int Classification_Input_Size = 0;
    private String text = "Error";

    public objectDetectorClass(AssetManager assetManager, String modelPath, String labelPath, int inputSize, String classification_model, int classification_input_size) throws IOException {
        INPUT_SIZE = inputSize;
        Classification_Input_Size = classification_input_size;
        // use to define gpu or cpu // no. of threads
        Interpreter.Options options = new Interpreter.Options();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(1); // set it according to your phone
        // loading model
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
        // load labelmap
        labelList = loadLabelList(assetManager, labelPath);

        Interpreter.Options options2 = new Interpreter.Options();
        options2.setNumThreads(2);
        interpreter2 = new Interpreter(loadModelFile(assetManager, classification_model), options2);


    }

    public String getText() {
        return text;
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;

        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException  {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public Mat recognizeImage(Mat mat_image) {

        Mat rotated_mat_image = new Mat();

        Mat a = mat_image.t();
        Core.flip(a, rotated_mat_image, 1);
        // Release mat
        a.release();

        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(rotated_mat_image.cols(), rotated_mat_image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotated_mat_image, bitmap);
        // define height and width
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        // scale the bitmap to input size of model
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // convert bitmap to bytebuffer as model input should be in it
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        // defining output
        // 10: top 10 object detected
        // 4: there coordinate in image
        //  float[][][]result=new float[1][10][4];
        Object[] input = new Object[1];
        input[0] = byteBuffer;

        Map<Integer, Object> output_map = new TreeMap<>();
        // we are not going to use this method of output
        // instead we create treemap of three array (boxes,score,classes)

        float[][][] boxes = new float[1][10][4];
        // 10: top 10 object detected
        // 4: there coordinate in image
        float[][] scores = new float[1][10];
        // stores scores of 10 object
        float[][] classes = new float[1][10];
        // stores class of object

        // add it to object_map;
        output_map.put(0, boxes);
        output_map.put(1, classes);
        output_map.put(2, scores);

        // now predict
        interpreter.runForMultipleInputsOutputs(input, output_map);
        // Before watching this video please watch my previous 2 video of
        //      1. Loading tensorflow lite model
        //      2. Predicting object
        // In this video we will draw boxes and label it with it's name

        Object value = output_map.get(0);
        Object Object_class = output_map.get(1);
        Object score = output_map.get(2);

        // loop through each object
        // as output has only 10 boxes
        for (int i = 0; i < 10; i++) {
            float class_value = (float) Array.get(Array.get(Object_class, 0), i);
            float score_value = (float) Array.get(Array.get(score, 0), i);
            // define threshold for score

            // Here you can change threshold according to your model
            // Now we will do some change to improve app
            if (score_value > 0.5) {
                Object box1 = Array.get(Array.get(value, 0), i);
                // we are multiplying it with Original height and width of frame

                float y1 = (float) Array.get(box1, 0) * height;
                float x1 = (float) Array.get(box1, 1) * width;
                float y2 = (float) Array.get(box1, 2) * height;
                float x2 = (float) Array.get(box1, 3) * width;

                if (y1 < 0) y1 = 0;
                if (x1 < 0) x1 = 0;
                if (x2 > width) x2 = width;
                if (y2 > height) y2 = height;

                float w1 = x2 - x1;
                float h1 = y2 - y1;

                Rect cropped_roi = new Rect((int) x1, (int) y1, (int) w1, (int) h1);
                Mat cropped = new Mat(rotated_mat_image, cropped_roi).clone();


                Bitmap bitmap1 = null;
                bitmap1 = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped, bitmap1);

                Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(bitmap1, Classification_Input_Size, Classification_Input_Size, false);
                ByteBuffer byteBuffer1 = convertBitmapToByteBuffer1(scaledBitmap1);

                float[][] output_class_value = new float[1][1];
                interpreter2.run(byteBuffer1, output_class_value);

                String sign_val = get_alphabets(output_class_value[0][0]);

                Imgproc.putText(rotated_mat_image, "" + sign_val, new Point(x1 + 10, y1 + 40), 2, 1.5, new Scalar(255, 172, 139, 249), 2);

                Imgproc.rectangle(rotated_mat_image, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0, 255), 2);
            }

        }
        Mat b = rotated_mat_image.t();
        Core.flip(b, mat_image, 0);
        b.release();
        // Now for second change go to CameraBridgeViewBase
        return mat_image;
    }

    private String get_alphabets(float sign_v) {
        if (sign_v >= -0.5 & sign_v < 0.5) text = "A";
        else if (sign_v >= 0.5 & sign_v < 1.5) text = "B";
        else if (sign_v >= 1.5 & sign_v < 2.5) text = "C";
        else if (sign_v >= 2.5 & sign_v < 3.5) text = "D";
        else if (sign_v >= 3.5 & sign_v < 4.5) text = "E";
        else if (sign_v >= 4.5 & sign_v < 5.5) text = "F";
        else if (sign_v >= 5.5 & sign_v < 6.5) text = "G";
        else if (sign_v >= 6.5 & sign_v < 7.5) text = "H";
        else if (sign_v >= 7.5 & sign_v < 8.5) text = "I";
        else if (sign_v >= 8.5 & sign_v < 9.5) text = "J";
        else if (sign_v >= 9.5 & sign_v < 10.5) text = "K";
        else if (sign_v >= 10.5 & sign_v < 11.5) text = "L";
        else if (sign_v >= 11.5 & sign_v < 12.5) text = "M";
        else if (sign_v >= 12.5 & sign_v < 13.5) text = "N";
        else if (sign_v >= 13.5 & sign_v < 14.5) text = "O";
        else if (sign_v >= 14.5 & sign_v < 15.5) text = "P";
        else if (sign_v >= 15.5 & sign_v < 16.5) text = "Q";
        else if (sign_v >= 16.5 & sign_v < 17.5) text = "R";
        else if (sign_v >= 17.5 & sign_v < 18.5) text = "S";
        else if (sign_v >= 18.5 & sign_v < 19.5) text = "T";
        else if (sign_v >= 19.5 & sign_v < 20.5) text = "U";
        else if (sign_v >= 20.5 & sign_v < 21.5) text = "V";
        else if (sign_v >= 21.5 & sign_v < 22.5) text = "W";
        else if (sign_v >= 22.5 & sign_v < 23.5) text = "X";
        else if (sign_v >= 23.5 & sign_v < 24.5) text = "Y";
        else text = "Z";


        return text;
    }


    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        // some model input should be quant=0  for some quant=1
        // for this quant=0
        // Change quant=1
        // As we are scaling image from 0-255 to 0-1
        int quant = 1;
        int size_images = INPUT_SIZE;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1 * size_images * size_images * 3);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * 1 * size_images * size_images * 3);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[size_images * size_images];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        // some error
        //now run
        for (int i = 0; i < size_images; ++i) {
            for (int j = 0; j < size_images; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    // paste this
                    byteBuffer.putFloat((((val >> 16) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val) & 0xFF)) / 255.0f);
                }
            }
        }
        return byteBuffer;
    }

    private ByteBuffer convertBitmapToByteBuffer1(Bitmap bitmap) {
        ByteBuffer byteBuffer;

        int quant = 1;
        int size_images = Classification_Input_Size;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1 * size_images * size_images * 3);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * size_images * size_images * 3);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[size_images * size_images];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        // some error
        //now run
        for (int i = 0; i < size_images; ++i) {
            for (int j = 0; j < size_images; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    // paste this
                    byteBuffer.putFloat((((val >> 16) & 0xFF)));
                    byteBuffer.putFloat((((val >> 8) & 0xFF)));
                    byteBuffer.putFloat((((val) & 0xFF)));
                }
            }
        }
        return byteBuffer;
    }
}
