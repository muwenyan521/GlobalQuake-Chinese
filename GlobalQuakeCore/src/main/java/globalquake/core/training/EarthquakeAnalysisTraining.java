package globalquake.core.training;

import globalquake.core.GlobalQuake;
import globalquake.core.HypocsSettings;
import globalquake.core.Settings;
import globalquake.core.earthquake.EarthquakeAnalysis;
import globalquake.core.earthquake.GQHypocs;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Hypocenter;
import globalquake.core.earthquake.data.PickedEvent;
import globalquake.core.geo.taup.TauPTravelTimeCalculator;
import globalquake.ui.ProgressUpdateFunction;
import globalquake.utils.GeoUtils;
import org.tinylog.Logger;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class EarthquakeAnalysisTraining {

    public static final int STATIONS = 30;
    public static final double DIST = 5000;

    public static final double INACCURACY = 5000;
    private static final double MASSIVE_ERR_ODDS = 0.4;


    public static void main(String[] args) throws Exception {
        TauPTravelTimeCalculator.init();
        GQHypocs.load();
        EarthquakeAnalysis.DEPTH_FIX_ALLOWED = false;
        GlobalQuake.prepare(new File("./training/"), null);

        Settings.hypocenterDetectionResolution = 0.0;
        Settings.pWaveInaccuracyThreshold = 4000.0;
        Settings.parallelHypocenterLocations = true;
        long sum = 0;
        long n = 0;
        long a  = System.currentTimeMillis();
        int fails = 0;
        for(int i = 0; i < 100; i++) {
            long err = runTest(888+i, STATIONS, false);
            System.err.printf("错误: %,d ms%n", err);
            if(err != -1) {
                sum += err;
                n++;
            } else{
                 fails++;
                //throw new IllegalStateException();
            }
        }

        System.err.println("============================================");
        if(n == 0){
            System.err.println("不正确!");
        } else {
            System.err.printf("平均时间为 %,d ms%n", sum / n);
        }
        System.err.printf("测试用时 %,d ms%n", System.currentTimeMillis() - a);
        System.err.printf("错误数  %d%n", fails);
        System.err.println("============================================");
        System.exit(0);
    }


    public static void calibrateResolution(ProgressUpdateFunction progressUpdateFunction, JSlider slider, boolean cpu) {
        double resolution = 0.0;
        long lastTime;
        int seed = 6543;
        int failed = 0;

        long targetTime = HypocsSettings.getOrDefaultInt("calibrateTargetTime", 400);

        while(failed < 5 && resolution <= (cpu ? 160 : 1000)) {
            if(cpu){
                Settings.hypocenterDetectionResolution = resolution;
            } else {
                Settings.hypocenterDetectionResolutionGPU = resolution;
            }

            lastTime = measureTest(seed++, 60, cpu);
            if(lastTime > targetTime){
                failed++;
            } else {
                failed = 0;
                resolution += cpu ? 2.0 : 5.0;
            }
            if(progressUpdateFunction !=null){
                progressUpdateFunction.update("Calibrating: Resolution %.2f took %d / %d ms".formatted(
                        resolution / 100.0, lastTime, targetTime),
                        (int) Math.max(0, Math.min(100, ((double)lastTime / targetTime) * 100.0)));
            }
            if(slider != null){
                slider.setValue((int) resolution);
                slider.repaint();
            }
        }

        if(GQHypocs.isCudaLoaded()) {
            GQHypocs.calculateStationLimit();
        }

        Settings.save();
    }

    public static long measureTest(long seed, int stations, boolean cpu){
        long a = System.currentTimeMillis();
        runTest(seed, stations, cpu);
        return System.currentTimeMillis()-a;
    }

    public static long runTest(long seed, int stations, boolean cpu) {
        EarthquakeAnalysis earthquakeAnalysis = new EarthquakeAnalysis();
        earthquakeAnalysis.testing = true;

        List<FakeStation> fakeStations = new ArrayList<>();

        Random r = new Random(seed);

        for(int i = 0; i < stations; i++){
            double ang = r.nextDouble() * 360.0;
            double dist = r.nextDouble() * DIST;
            double[] latLon = GeoUtils.moveOnGlobe(0, 0, dist, ang);
            fakeStations.add(new FakeStation(latLon[0], latLon[1]));
        }

        List<PickedEvent> pickedEvents = new ArrayList<>();
        var cluster = new Cluster();
        cluster.updateCount = 6543541;

        Hypocenter absolutetyCorrect = new Hypocenter(140 + r.nextDouble() * 10, r.nextDouble() * 10, 200, 0, 0,0, null, null);

        for(FakeStation fakeStation : fakeStations){
            double distGC = GeoUtils.greatCircleDistance(absolutetyCorrect.lat,
                    absolutetyCorrect.lon, fakeStation.lat, fakeStation.lon);
            double travelTime = TauPTravelTimeCalculator.getPWaveTravelTime(absolutetyCorrect.depth, TauPTravelTimeCalculator.toAngle(distGC));

            if(travelTime < 0){
                continue;
            }

            long time = absolutetyCorrect.origin + ((long) (travelTime * 1000.0));
            time += (long)((r.nextDouble() - 0.5) * INACCURACY);
            if(r.nextDouble() < MASSIVE_ERR_ODDS){
                time += (long) ((r.nextDouble() * 10.0 - 5.0) * INACCURACY);
            }

            var event = new PickedEvent(time, fakeStation.lat, fakeStation.lon, 0, 100);
            pickedEvents.add(event);
        }

        cluster.calculateRoot(fakeStations);

        earthquakeAnalysis.processCluster(cluster, pickedEvents, !cpu);

        Logger.debug("Shouldve been " + absolutetyCorrect);
        Logger.debug("Got           " + cluster.getPreviousHypocenter());

        if(cluster.getEarthquake()!=null) {
            double dist = GeoUtils.greatCircleDistance(cluster.getEarthquake().getLat(), cluster.getEarthquake().getLon(), absolutetyCorrect.lat, absolutetyCorrect.lon);
            return Math.abs(cluster.getEarthquake().getOrigin());
        } else{
            return -1;
        }
    }

    public record FakeStation(double lat, double lon){

    }

}
