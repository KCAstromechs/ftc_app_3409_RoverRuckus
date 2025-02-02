package org.firstinspires.ftc.teamcode.camera;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.vuforia.CameraDevice;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VisionBaseOliver {

    private VuforiaLocalizer vuforia;
    boolean speedControl, debug;
    OpMode callingOpMode;

    public VisionBaseOliver(boolean _speedControl, boolean _debug, OpMode _callingOpMode) {
        speedControl = _speedControl;
        debug = _debug;
        callingOpMode = _callingOpMode;

        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "Ac8xsqH/////AAAAGcG2OeE2NECwo7mM5f9KX1RKmDT79NqkIHc/ATgW2+loN9Fr8fkfb6jE42RZmiRYeei1FvM2M3kUPdl53j" +
                "+oeuhahXi7ApkbRv9cef0kbffj+4EkWKWCgQM39sRegfX+os6PjJh1fwGdxxijW0CYXnp2Rd1vkTjIs/cW2/7TFTtuJTkc17l" +
                "+FNJAeqLEfRnwrQ0FtxvBjO8yQGcLrpeKJKX/+sN+1kJ/cvO345RYfPSoG4Pi+wo/va1wmhuZ/WCLelUeww8w8u0douStuqcuz" +
                "ufrsWmQThsHqQDfDh0oGKZGIckh3jwCV2ABkP0lT6ICBDm4wOZ8REoyiY2kjsDnnFG6cT803cfzuVuPJl+uGTEf";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB888, true);
        vuforia.setFrameQueueCapacity(1);


    }

    public enum MINERALS {
        LEFT,
        CENTER,
        RIGHT
    }

    public MINERALS analyzeSample(int yStart, int yMax, int xStart, int xMax) throws InterruptedException {
        int thisR, thisB, thisG;                    //RGB values of current pixel to translate into HSV
        int idx = 0;                                //Ensures we get correct image type from Vuforia

        System.out.println("timestamp before getting image");
        callingOpMode.telemetry.addData("timestamp ", "before getting image");
        callingOpMode.telemetry.update();
        //Take an image from Vuforia in the correct format
        VuforiaLocalizer.CloseableFrame frame = vuforia.getFrameQueue().take();
        for (int i = 0; i < frame.getNumImages(); i++) {
            if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB888) {
                idx = i;
                break;
            }
        }

        //Create an instance of the image and then of the pixels
        Image image = frame.getImage(idx);
        ByteBuffer px = image.getPixels();

        //Origin: top right of image (current guess)



        //Loop through every pixel column
        int h = image.getHeight();
        int w = image.getWidth();

        int rIDX;
        int gIDX;
        int bIDX;

        System.out.println("timestamp before processing loop");
        callingOpMode.telemetry.addData("timestamp ", "before processing image");
        callingOpMode.telemetry.update();
        long timeStartAnalysis = System.currentTimeMillis();

        int avgGoldPos = 0;
        int sumGoldPos = 0;
        int numOfGold =  0;

        for (int y = yStart; y < yMax; y++) {

//            System.out.println("loop #" + i);
            //If the bot stops you should really stop.
            if(Thread.interrupted()) break;

            //Loop through a certain number of rows to cover a certain area of the image
            for (int x = xStart; x < xMax; x++) { //925, 935

                rIDX = y * w * 3 + (x * 3);
                gIDX = y * w * 3 + (x * 3) + 1;
                bIDX = y * w * 3 + (x * 3) + 2;

                //Take the RGB vals of current pix
                thisR = px.get(rIDX) & 0xFF;
                thisG = px.get(gIDX) & 0xFF;
                thisB = px.get(bIDX) & 0xFF;

                if(thisR>180 && thisG>130 & thisB<100) {
                    px.put(rIDX, (byte) 0);
                    px.put(gIDX, (byte) 255);
                    px.put(bIDX, (byte) 0);
                    numOfGold++;
                    sumGoldPos+=x;

                }
                if((thisR>230 && thisG>230 && thisB>230) || Math.abs(x - 630) < 10) {
                    px.put(rIDX, (byte) 0);
                    px.put(gIDX, (byte) 0);
                    px.put(bIDX, (byte) 0);
                }
                //X AXIS
                if(Math.abs(y - yMax) < 10) {
                    px.put(rIDX, (byte) 0);
                    px.put(gIDX, (byte) 0);
                    px.put(bIDX, (byte) 100);
                }
                //Y AXIS
                if(Math.abs(x - xMax) < 10) {
                    px.put(rIDX, (byte) 100);
                    px.put(gIDX, (byte) 0);
                    px.put(bIDX, (byte) 0);
                }
                //}
            }
        }


        long timeStopAnalysis = System.currentTimeMillis();

        double timeForAnalysis = (timeStopAnalysis - timeStartAnalysis)/1000;

        callingOpMode.telemetry.addData("timestamp ", "after processing loop before save pic/grab picto");
        callingOpMode.telemetry.update();
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
        callingOpMode.telemetry.addData("timestamp ", "after save pic");
        callingOpMode.telemetry.update();



        if(numOfGold!=0) avgGoldPos = (int) (sumGoldPos/numOfGold);
        String goldLocResult = "";


        if (numOfGold > 100) {
            if(avgGoldPos<630) {
                return MINERALS.LEFT;
            }
            else {
                return MINERALS.CENTER;
            }
        }
        else {
            return MINERALS.RIGHT;
        }

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
    }}
