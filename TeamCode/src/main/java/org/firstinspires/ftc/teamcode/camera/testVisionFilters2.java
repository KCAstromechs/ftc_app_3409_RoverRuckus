package org.firstinspires.ftc.teamcode.camera;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.vuforia.CameraDevice;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Class.*;

/**
 * Created by me on 23/12/2018.
 */

@Disabled
@Autonomous(name="Graphical image saver 2")
public class testVisionFilters2 extends LinearOpMode {

    private VuforiaLocalizer vuforia;

    @Override
    public void runOpMode() throws InterruptedException {

        Class<?> ID = null;
        Class<?> PF = null;

        try {
            ID = forName("com.qualcomm.ftcrobotcontroller.R.idx");
            PF = forName("com.vuforia.PIXEL_FORMAT");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        VuforiaLocalizer.Parameters parameters = null;
        Field cameraMonitorViewID = null;
        Field RGB888 = null;
        try {
            cameraMonitorViewID = ID.getClass().getDeclaredField("cameraMonitorViewId");
            parameters = new VuforiaLocalizer.Parameters((int)cameraMonitorViewID.get(ID));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        parameters.vuforiaLicenseKey = "Ac8xsqH/////AAAAGcG2OeE2NECwo7mM5f9KX1RKmDT79NqkIHc/ATgW2+loN9Fr8fkfb6jE42RZmiRYeei1FvM2M3kUPdl53j" +
                "+oeuhahXi7ApkbRv9cef0kbffj+4EkWKWCgQM39sRegfX+os6PjJh1fwGdxxijW0CYXnp2Rd1vkTjIs/cW2/7TFTtuJTkc17l" +
                "+FNJAeqLEfRnwrQ0FtxvBjO8yQGcLrpeKJKX/+sN+1kJ/cvO345RYfPSoG4Pi+wo/va1wmhuZ/WCLelUeww8w8u0douStuqcuz" +
                "ufrsWmQThsHqQDfDh0oGKZGIckh3jwCV2ABkP0lT6ICBDm4wOZ8REoyiY2kjsDnnFG6cT803cfzuVuPJl+uGTEf";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);
        try {
            RGB888 = PF.getClass().getDeclaredField("RGB888");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            Vuforia.setFrameFormat((int) RGB888.get(PF), true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        vuforia.setFrameQueueCapacity(1);

        waitForStart();

        int thisR, thisB, thisG;                    //RGB values of current pixel to translate into HSV
        int totalBlue = 1;                          //Total number of blue pixels to help find blue side location
        int totalRed = 1;                           //Total number of red pixels to help find red side location
        int idx = 0;                                //Ensures we get correct image type from Vuforia
        float minRGB, maxRGB;

        System.out.println("timestamp before getting image");
        telemetry.addData("timestamp ", "before getting image");
        telemetry.update();
        //Take an image from Vuforia in the correct format
        VuforiaLocalizer.CloseableFrame frame = vuforia.getFrameQueue().take();
        for (int i = 0; i < frame.getNumImages(); i++) {
            try {
                if (frame.getImage(i).getFormat() == (int) RGB888.get(PF)) {
                    idx = i;
                    break;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //Create an instance of the image and then of the pixels
        Image image = frame.getImage(idx);
        ByteBuffer px = image.getPixels();
        ByteBuffer img2 = cloneByteBuffer(px);

        //Origin: top right of image (current guess)

        

        //Loop through every pixel column
        int h = image.getHeight();
        int w = image.getWidth();

        int rIDX;
        int gIDX;
        int bIDX;

        System.out.println("timestamp before processing loop");
        telemetry.addData("timestamp ", "before processing image");
        telemetry.update();
        long timeStartAnalysis = System.currentTimeMillis();

        int avgGoldPos = 0;
        int sumGoldPos = 0;
        int numOfGold =  0;

        for (int i = 0; i < h; i++) {

//            System.out.println("loop #" + i);
            //If the bot stops you should really stop.
            if(Thread.interrupted()) break;

            //Loop through a certain number of rows to cover a certain area of the image
            for (int j = 0; j < w; j++) { //925, 935

                rIDX = i * w * 3 + (j * 3);
                gIDX = i * w * 3 + (j * 3) + 1;
                bIDX = i * w * 3 + (j * 3) + 2;

                //Take the RGB vals of current pix
                thisR = px.get(rIDX) & 0xFF;
                thisG = px.get(gIDX) & 0xFF;
                thisB = px.get(bIDX) & 0xFF;


                //Convert the RGB vals into S
                minRGB = Math.min(thisR, Math.min(thisB, thisG)) + 1;
                maxRGB = Math.max(thisR, Math.max(thisB, thisG)) + 1;
                //System.out.println("Saturation: " + thisS);

                //We now have the colors (one byte each) for any pixel, (j, i) so we can add to the totals
                //if (thisS >= 0.85) {
                //                  System.out.println("Jewel pixel found");
                if(thisR>180 && thisG>130 & thisB<100) {
                    px.put(rIDX, (byte) 0);
                    px.put(gIDX, (byte) 255);
                    px.put(bIDX, (byte) 0);
                    numOfGold++;
                    sumGoldPos+=j;

                }
                if(thisR>230 && thisG>230 && thisB>230) {
                    px.put(rIDX, (byte) 0);
                    px.put(gIDX, (byte) 0);
                    px.put(bIDX, (byte) 0);
                }
                if(i < 30) {
                    px.put(rIDX, (byte) 0);
                    px.put(gIDX, (byte) 0);
                    px.put(bIDX, (byte) 100);
                }
                if(j < 30) {
                    px.put(rIDX, (byte) 100);
                    px.put(gIDX, (byte) 0);
                    px.put(bIDX, (byte) 0);
                }
                //}
            }
        }
        avgGoldPos = (int) (sumGoldPos/numOfGold);
        String goldLocResult = "";

        if(avgGoldPos<420) {
            goldLocResult = "Gold is on the left side";
        }
        else if(avgGoldPos<840) {
            goldLocResult = "Gold is middle";
        }
        else {
            goldLocResult = "Gold is on the right side";
        }

        long timeStopAnalysis = System.currentTimeMillis();

        double timeForAnalysis = (timeStopAnalysis - timeStartAnalysis) /1000;

        telemetry.addData("timestamp ", "after processing loop before save pic/grab picto");
        telemetry.update();
        System.out.println("timestamp after processing loop, before save pic/grab picto");

        CameraDevice.getInstance().setFlashTorchMode(false);

        //save picture block
        boolean graphicalDiagnostic = true;
        boolean bSavePicture = true;
        if (bSavePicture) {
            // Reset the pixel pointer to the start of the image
            //  px = image.getPixels();
            // Create a buffer to hold 32-bit image dataa and fill it
            int bmpData[] = new int[w * h];
            int pixel;
            int index = 0;
            int x,y;
            if(graphicalDiagnostic) {
                for (y = 0; y < h; y++) {
                    for (x = 0; x < w; x++) {
                        thisR = px.get() & 0xFF;
                        thisG = px.get() & 0xFF;
                        thisB = px.get() & 0xFF;
                        bmpData[index] = Color.rgb(thisR, thisG, thisB);
                        index++;
                    }
                }
            }
            else {
                for (y = 0; y < h; y++) {
                    for (x = 0; x < w; x++) {
                        thisR = px.get() & 0xFF;
                        thisG = px.get() & 0xFF;
                        thisB = px.get() & 0xFF;
                        bmpData[index] = Color.rgb(thisR, thisG, thisB);
                        index++;
                    }
                }
            }
            // Now create a bitmap object from the buffer
            Bitmap bmp = Bitmap.createBitmap(bmpData, w, h, Bitmap.Config.ARGB_8888);
            // And save the bitmap to the file system
            // NOTE:  AndroidManifest.xml needs <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
            try {
                //to convert Date to String, use format method of SimpleDateFormat class.
                DateFormat dateFormat = new SimpleDateFormat("mm-dd__hh-mm-ss");
                String strDate = dateFormat.format(new Date());
                String path = Environment.getExternalStorageDirectory() + "/Snapshot__" + strDate + ".png";
                System.out.println("Snapshot filename" + path);
                File file = new File(path);
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                System.out.println("Snapshot exception" + e.getStackTrace().toString());
            }
        }

        System.out.println("timestamp after save pic");
        telemetry.addData("timestamp ", "after save pic");
        telemetry.update();

        //telemetry.addData("totalBlue: ", totalBlue);
        //telemetry.addData("totalRed: ", totalRed);
        telemetry.addLine(goldLocResult);
        telemetry.update();
        sleep(12000);
    }
    public static ByteBuffer cloneByteBuffer(final ByteBuffer original) {
        // Create clone with same capacity as original.
        final ByteBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity()) :
                ByteBuffer.allocate(original.capacity());

        // Create a read-only copy of the original.
        // This allows reading from the original without modifying it.
        final ByteBuffer readOnlyCopy = original.asReadOnlyBuffer();

        // Flip and read from the original.
        readOnlyCopy.flip();
        clone.put(readOnlyCopy);

        return clone;
    }
}
