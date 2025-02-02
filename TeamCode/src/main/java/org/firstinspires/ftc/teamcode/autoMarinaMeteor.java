package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.camera.VisionBaseMarina;

@Autonomous(name="Marina Meteor", group = "marina")
public class autoMarinaMeteor extends LinearOpMode {

    Servo led;

    @Override
    public void runOpMode() throws InterruptedException {
        DriveBaseMarinaAdvanced driveBase = new DriveBaseMarinaAdvanced(false, true, this);
        VisionBaseMarina camera = new VisionBaseMarina(false, this,100, 350, 0, 1280);
        RobotBaseMarina robotBase = new RobotBaseMarina(true, this);

        led = hardwareMap.servo.get("elleedee");

        driveBase.setSpeedLimit(0.5);

        waitForStart();

        robotBase.land();

        driveBase.hasBeenZeroed = false;

        sleep(1000);

        led.setPosition(0.7745);

        driveBase.driveStraight(1.5, 0, -0.4);

        VisionBaseMarina.MINERALS result = camera.analyzeSampleNoSave();

        led.setPosition(0.6935);

        if (result == VisionBaseMarina.MINERALS.RIGHT) {
            driveBase.turn(120, 0.6);
            driveBase.driveStraight(32, 120, false);
            driveBase.driveStraight(14, 120, true);
            driveBase.turn(8);
            driveBase.driveStraight(48, 0, false);
        } else if (result == VisionBaseMarina.MINERALS.CENTER) {
            driveBase.turn(95, 0.6);
            driveBase.driveStraight(24, 95, false);
            driveBase.driveStraight(14, 95, true);
            driveBase.turn(8);
            driveBase.driveStraight(38, 0, false);
        } else if (result == VisionBaseMarina.MINERALS.LEFT) {
            driveBase.turn(60, 0.6);
            driveBase.driveStraight(28, 60, false);
            driveBase.driveStraight(14, 60, true);
            driveBase.turn(8);
            driveBase.driveStraight(28, 0, false);
        }

        driveBase.driveStraightStall(1.8, 0, -0.4);
        driveBase.turn(320);

        driveBase.driveStraight(32, 318, false);

        driveBase.turn(225);

        robotBase.deployMarker();

        sleep(1111);

        driveBase.turn(310);

        driveBase.strafe(3, 315, 0.8, 2);

        driveBase.driveStraight(45, 315,318, true, -1600, 4);
    }
}
