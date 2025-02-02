package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.camera.VisionBaseOliver;

@Disabled
@Autonomous(name="Meteor Basic", group = "test")
public class autoOliverMeteor extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        DriveBaseOliver driveBase = new DriveBaseOliver(false, true, this);
        VisionBaseOliver camera = new VisionBaseOliver(true, true, this);
        RobotBaseOliver robotBase = new RobotBaseOliver(true, this);
        waitForStart();

        robotBase.land();

        driveBase.hasBeenZeroed = false;

        sleep(1000);

        driveBase.turn(355, 0.25);

        VisionBaseOliver.MINERALS result = camera.analyzeSample(450, 700, 0, 1200);

        driveBase.driveStraight(14, 0, 0.5);

        if (result == VisionBaseOliver.MINERALS.LEFT) {
            driveBase.strafe(14, 0, 0.5);
        }
        if (result == VisionBaseOliver.MINERALS.RIGHT) {
            driveBase.strafe(14, 0, -0.5);
        }

        driveBase.driveStraight(14, 0);

        driveBase.driveStraight(14, 0, -DriveBaseOliver.driveSpeed);

        if (result == VisionBaseOliver.MINERALS.RIGHT) {
            driveBase.strafe(60, 0, 0.6);
        }
        if (result == VisionBaseOliver.MINERALS.CENTER) {
            driveBase.strafe(46, 0, 0.6);
        }
        if (result == VisionBaseOliver.MINERALS.LEFT) {
            driveBase.strafe(32, 0, 0.6);
        }

        driveBase.turn(303, 0.5);

        driveBase.strafe(40, 310, 0.75);

        robotBase.deployMarker();

        sleep(1000);

        driveBase.turn(228  , 0.5);

        driveBase.driveStraight(88, 220, -0.5);

        robotBase.deployFlap();
    }
}
