package com.example.photoeditor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.Utils;
import java.io.IOException;
import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity implements SelectPhoto.OnImageSelectedListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private ImageView imageView;
    private Mat processedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            System.err.println("Unable to load OpenCV library");
        }

        Button captureImage = findViewById(R.id.selectImageButton);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        imageView = findViewById(R.id.imageView);
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择图片");
        builder.setItems(new CharSequence[]{"拍摄新照片", "从相册中选择"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                dispatchTakePictureIntent();
                                break;
                            case 1:
                                dispatchPickFromGalleryIntent();
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickFromGalleryIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            processImage(imageBitmap);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                processImage(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processImage(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        processedImage = ImageProcessor.applyHalftoneEffect(ImageProcessor.toGrayscale(mat));
        Bitmap resultBitmap = Bitmap.createBitmap(processedImage.width(), processedImage.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(processedImage, resultBitmap);
        imageView.setImageBitmap(resultBitmap);
        String gcode = GCode.generateGCode(processedImage);
        GCode.saveGCodeToFile(gcode, this);
    }

    @Override
    public void onImageSelected(Uri imageUri) {
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            processImage(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}