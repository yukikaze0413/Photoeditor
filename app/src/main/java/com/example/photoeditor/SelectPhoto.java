package com.example.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

public class SelectPhoto {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;

    private Activity activity;
    private Uri imageUri;
    private OnImageSelectedListener listener;

    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri);
    }

    public SelectPhoto(Activity activity, OnImageSelectedListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, PICK_IMAGE);
    }

    public void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAPTURE_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            if (listener != null) {
                listener.onImageSelected(imageUri);
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "Title", null);
            imageUri = Uri.parse(path);
            if (listener != null) {
                listener.onImageSelected(imageUri);
            }
        }
    }
}