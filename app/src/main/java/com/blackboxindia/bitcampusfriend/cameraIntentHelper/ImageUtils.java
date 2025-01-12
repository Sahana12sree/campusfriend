package com.blackboxindia.bitcampusfriend.cameraIntentHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import com.blackboxindia.bitcampusfriend.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings({"WeakerAccess", "JavaDoc", "unused"})
@SuppressLint("SdCardPath")
public class ImageUtils {

    private static String TAG = ImageUtils.class.getSimpleName()+" YOYO";
    private static int[] MAX_ICON_RES = {420,420};
    private static int[] MAX_GALLERY_RES = {900, 900};

    Context context;
    private Activity current_activity;
    private Fragment current_fragment;

    private ImageAttachmentListener imageAttachment_callBack;

    private Uri imageUri;

    private int from = 0;
    private boolean isFragment = false;

    public ImageUtils(Activity act) {

        this.context = act;
        this.current_activity = act;
        imageAttachment_callBack = (ImageAttachmentListener) context;
    }

    public ImageUtils(Activity act, ImageAttachmentListener listener) {
        this.context = act;
        this.current_activity = act;
        imageAttachment_callBack = listener;
    }

    public ImageUtils(Activity act, Fragment fragment, boolean isFragment) {

        this.context = act;
        this.current_activity = act;
        imageAttachment_callBack = (ImageAttachmentListener) fragment;
        if (isFragment) {
            this.isFragment = true;
            current_fragment = fragment;
        }
    }

    public ImageUtils(Activity act, Fragment fragment, boolean isFragment, ImageAttachmentListener listener) {

        this.context = act;
        this.current_activity = act;
        if (isFragment) {
            this.isFragment = true;
            current_fragment = fragment;
        }
        imageAttachment_callBack = listener;
    }

    /**
     * Get Image from the given path
     *
     * @param file_name
     * @param file_path
     * @return
     */

    public static Bitmap getImage(String file_name, String file_path) {

        File path;
        path = new File(file_path);
        File file = new File(path, file_name);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inTempStorage = new byte[16 * 1024];

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return bitmap;
    }

    /**
     * Get Image URI from Bitmap
     *
     * @param context
     * @param photo
     * @return
     */

    public static Uri getImageUri(Context context, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 80, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Get Path from Image URI
     *
     * @param uri
     * @return
     */

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.context.getContentResolver().query(uri, projection, null, null, null);
        int column_index;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else
            return uri.getPath();
    }

    /**
     * Bitmap from String
     *
     * @param encodedString
     * @return
     */
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            //Log.e(TAG,"StringToBitMap error: ",e);
            return null;
        }
    }


    /**
     * Get String from Bitmap
     *
     * @param bitmap
     * @return
     */

    public static String BitMapToString(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }


    /**
     * Check Camera Availability
     *
     * @return
     */

    public boolean isDeviceSupportCamera() {
        // this device has a camera
// no camera on this device
        return this.context.getPackageManager().hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY);
    }


    /**
     * Compress Imgae
     *
     * @param imageUri
     * @param height
     * @param width
     * @return
     */


    public Bitmap compressImage(String imageUri, float height, float width) {
        return compressImage(imageUri, height, width,this.context);
    }

    /**
     * Compress Imgae
     *
     * @param imageUri
     * @param maxHeight
     * @param maxWidth
     * @return
     */

    @SuppressWarnings("deprecation")
    public static Bitmap compressImage(String imageUri, float maxHeight, float maxWidth, Context context ) {

        String filePath = getRealPathFromURI(imageUri, context);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.

        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // max Height and width values of the compressed image is taken as 816x612

        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        //  setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //  inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //  load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        assert scaledBitmap != null;
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // check the rotation of the image and display it properly

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            //Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                //Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                //Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                //Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);

            return scaledBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get RealPath from Content URI
     *
     * @param contentURI
     * @param context
     * @return
     */
    public static String getRealPathFromURI(String contentURI, Context context) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String res = cursor.getString(index);
            cursor.close();
            return res;
        }
    }


    public static Drawable tintMyDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }


    /**
     * ImageSize Calculation
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * Launch Camera
     *
     * @param from
     */

    public void launchCamera(int from) {
        this.from = from;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission_check(1);
        } else {
            camera_call();
        }
    }

    /**
     * Launch Gallery
     *
     * @param from
     */

    public void launchGallery(int from) {

        this.from = from;

        if (Build.VERSION.SDK_INT >= 23) {
            permission_check(2);
        } else {
            gallery_call();
        }
    }

    /**
     * Show AlertDialog with the following options
     * <p>
     * Camera
     * Gallery
     *
     * @param from
     */

    public void imagepicker(final int from) {
        this.from = from;

        final CharSequence[] items;

        if (isDeviceSupportCamera()) {
            items = new CharSequence[2];
            items[0] = "Camera";
            items[1] = "Gallery";
        } else {
            items = new CharSequence[1];
            items[0] = "Gallery";
        }

        android.app.AlertDialog.Builder alertdialog = new android.app.AlertDialog.Builder(current_activity);
        alertdialog.setTitle("Add Image");
        alertdialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {
                    launchCamera(from);
                } else if (items[item].equals("Gallery")) {
                    launchGallery(from);
                }
            }
        });
        alertdialog.show();
    }

    /**
     * Check permission
     *
     * @param code
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void permission_check(final int code) {

        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(current_activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(current_activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                showMessageOKCancel("For adding images , You need to provide permission to access your files",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(isFragment){
                                    current_fragment.requestPermissions(
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            code);
                                }else {
                                    ActivityCompat.requestPermissions(current_activity,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            code);
                                }
                            }
                        });
            } else {

                if(isFragment){
                    current_fragment.requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            code);
                }else {
                    ActivityCompat.requestPermissions(current_activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            code);
                }

            }
        }
        else {
            if (code == 1)
                camera_call();
            else if (code == 2)
                gallery_call();
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(current_activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



    protected File getPhotoDirectory() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AdPhotos");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                //Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    public void camera_call() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_.jpg";

        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = new File(getPhotoDirectory(),timeStamp);
        imageUri = Uri.fromFile(file);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent1.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file));
        }else {
            intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }

        intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent1.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (isFragment)
            current_fragment.startActivityForResult(intent1, 0);
        else
            current_activity.startActivityForResult(intent1, 0);
    }

    /**
     * pick image from Gallery
     */

    public void gallery_call() {
        //Log.d(TAG, "gallery_call: ");

        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent2.setType("image/*");

        if (isFragment)
            current_fragment.startActivityForResult(intent2, 1);
        else
            current_activity.startActivityForResult(intent2, 1);

    }


    /**
     * Activity PermissionResult
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void request_permission_result(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Log.i(TAG,"request_permission_result "+ requestCode);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    camera_call();
                } else {
                    Toast.makeText(current_activity, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            case 2:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gallery_call();
                } else {

                    Toast.makeText(current_activity, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    /**
     * Intent ActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data, null);
    }

    /**
     * Intent ActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param fileName    optional
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data, @Nullable String fileName) {
        String file_name;
        if (fileName != null) {
            file_name = fileName;
        } else {
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            currentDateTimeString = currentDateTimeString.replaceAll(" ", "_");
            currentDateTimeString = currentDateTimeString.replaceAll(",", "");
            currentDateTimeString = currentDateTimeString.replaceAll(":", ".");
            currentDateTimeString = "IMG_" + currentDateTimeString + ".jpg";
            file_name = currentDateTimeString;
        }
        Bitmap bitmap;

        switch (requestCode) {
            case 0:
                String selected_path;
                if (resultCode == Activity.RESULT_OK) {

                    try {
                        bitmap = resize(imageUri,MAX_ICON_RES[0],context);
//                        bitmap = compressImage(imageUri.toString(), MAX_ICON_RES[0], MAX_ICON_RES[1]);

                        //Log.i(TAG, "now height: "+bitmap.getHeight());
                        //Log.i(TAG, "now Width: "+bitmap.getWidth());

                        imageAttachment_callBack.image_attachment(from, file_name, bitmap, imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {

                    Uri selectedImage = data.getData();

                    try {
                        selected_path = getPath(selectedImage);
                        file_name = selected_path.substring(selected_path.lastIndexOf("/") + 1);

                        bitmap = resize(selectedImage,MAX_GALLERY_RES[0],context);
//                        bitmap = compressImage(selectedImage.toString(), MAX_GALLERY_RES[0], MAX_GALLERY_RES[1]);

                        //Log.i(TAG, "gallery now height: "+bitmap.getHeight());
                        //Log.i(TAG, "gallery now Width: "+bitmap.getWidth());

                        imageAttachment_callBack.image_attachment(from, file_name, bitmap, selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
        }


    }

    /**
     * Get image from Uri
     *
     * @param uri
     * @param height
     * @param width
     * @return
     */
    public Bitmap getImage_FromUri(Uri uri, float height, float width) {
        Bitmap bitmap = null;

        try {
            bitmap = compressImage(uri.toString(), height, width);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Get filename from URI
     *
     * @param uri
     * @return
     */
    public String getFileName_from_Uri(Uri uri) {
        String path, file_name;

        try {
            path = getRealPathFromURI(uri.getPath(), context);
            file_name = path.substring(path.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return file_name;
    }


    /**
     * Check Image Exist (or) Not
     *
     * @param file_name
     * @param file_path
     * @return
     */

    public static boolean checkimage(String file_name, String file_path) {
        boolean flag;
        File path = new File(file_path);

        File file = new File(path, file_name);
        //Log.i("file", "exists");
//Log.i("file", "not exist");
        flag = file.exists();

        return flag;
    }

    /**
     * Get file name from path
     *
     * @param path
     * @return
     */

    public String getfilename_from_path(String path) {
        return path.substring(path.lastIndexOf('/') + 1);

    }

    /**
     * Create an image
     *
     * @param bitmap
     * @param file_name
     * @param filepath
     * @param file_replace
     */


    public static void createImage(Bitmap bitmap, String file_name, String filepath, boolean file_replace) {

        File path;
        path = new File(filepath);

        if (!path.exists()) {
            if (!path.mkdirs()) {
                //could not make dir
                return;
            }
        }

        File file = new File(path, file_name);

        if (file.exists()) {
            if (file_replace) {
                boolean isDeleted = file.delete();
                if(isDeleted) {
                    file = new File(path, file_name);
                    store_image(file, bitmap);
                    //Log.i(TAG, "file replaced");
                }
//                else
                    //Log.i(TAG, "could not delete file");
            }
        } else {
            store_image(file, bitmap);
        }

    }


    /**
     * @param file
     * @param bmp
     */
    public static void store_image(File file, Bitmap bmp) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap resize(Uri uri,  int maxDim, Context context) {

        String filePath = ImageUtils.getRealPathFromURI(uri.toString(), context);
        Bitmap image;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int orgHeight = options.outHeight;
        int orgWidth = options.outWidth;

        int inSampleSize =1;
        if(orgHeight > orgWidth){
            if(orgHeight >maxDim){
                while(orgHeight/inSampleSize>maxDim){
                    inSampleSize++;
                }
                inSampleSize--;
            }
        }else{
            if(orgWidth>maxDim){
                while(orgWidth/inSampleSize>maxDim){
                    inSampleSize++;
                }
                inSampleSize--;
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        image = BitmapFactory.decodeFile(filePath, options);

        if (maxDim> 0) {

            int width = image.getWidth();
            int height = image.getHeight();

//            float ratioMax = (float) maxWidth / (float) maxHeight;
//
//            int finalWidth = maxWidth;
//            int finalHeight = maxHeight;
//
//            if (ratioMax > 1) {
//                finalWidth = (int) ((float)maxHeight * ratioBitmap);
//            } else {
//                finalHeight = (int) ((float)maxWidth / ratioBitmap);
//            }

            float ratioBitmap = (float) width / (float) height;
            int finalWidth = width;
            int finalHeight = height;

            if(width>height){
                if(width>maxDim){
                    finalWidth = maxDim;
                    finalHeight = (int)((float)maxDim/ratioBitmap);
                }
            }
            else {
                if(height>maxDim){
                    finalHeight = maxDim;
                    finalWidth = (int)((float)maxDim*ratioBitmap);
                }
            }

            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    // Image Attachment Callback

    public interface ImageAttachmentListener {
        void image_attachment(int from, String filename, Bitmap file, Uri uri);
    }


}
