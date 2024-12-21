package globalquake.core.seedlink;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkPacket;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import globalquake.core.GlobalQuake;
import globalquake.core.database.SeedlinkNetwork;
import globalquake.core.database.SeedlinkStatus;
import globalquake.core.station.AbstractStation;
import globalquake.core.station.GlobalStation;
import org.tinylog.Logger;

import java.net.SocketException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeedlinkNetworksReader {

    protected static final int RECONNECT_DELAY = 10;
    private static final int SEEDLINK_TIMEOUT = 90;
    private Instant lastData;

    private ExecutorService seedlinkReaderService;

    private final Queue<SeedlinkReader> activeReaders = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws Exception {
        SeedlinkReader reader = new SeedlinkReader("rtserve.iris.washington.edu", 18000);
        reader.selectData("AK", "D25K", List.of("BHZ"));
        reader.endHandshake();

        SortedSet<DataRecord> set = new TreeSet<>(Comparator.comparing(dataRecord -> dataRecord.getStartBtime().toInstant().toEpochMilli()));

        while (reader.hasNext() && set.size() < 10) {
            SeedlinkPacket pack = reader.readPacket();
            DataRecord dataRecord = pack.getMiniSeed();
            System.out.println(pack.getMiniSeed().getStartTime() + " - " + pack.getMiniSeed().getLastSampleTime() + " x " + pack.getMiniSeed().getEndTime() + " @ " + pack.getMiniSeed().getSampleRate());
            System.out.println(pack.getMiniSeed().getControlHeader().getSequenceNum());
            if (!set.add(dataRecord)) {
                System.out.println("错误，已包含");
            }
        }

        reader.close();
        for (DataRecord dataRecord : set) {
            System.err.println(dataRecord.getStartTime() + " - " + dataRecord.getLastSampleTime() + " x " + dataRecord.getEndTime() + " @ " + dataRecord.getSampleRate());
            System.err.println(dataRecord.oneLineSummary());
        }
    }

    public void run() {
        createCache();
        seedlinkReaderService = Executors.newCachedThreadPool();
        GlobalQuake.instance.getStationDatabaseManager().getStationDatabase().getDatabaseReadLock().lock();

        try {
            GlobalQuake.instance.getStationDatabaseManager().getStationDatabase().getSeedlinkNetworks().forEach(
                    seedlinkServer -> seedlinkReaderService.submit(() -> runSeedlinkThread(seedlinkServer, RECONNECT_DELAY)));
        } finally {
            GlobalQuake.instance.getStationDatabaseManager().getStationDatabase().getDatabaseReadLock().unlock();
        }
    }

    private final Map<String, GlobalStation> stationCache = new HashMap<>();

    private void createCache() {
        for (AbstractStation s : GlobalQuake.instance.getStationManager().getStations()) {
            if (s instanceof GlobalStation) {
                stationCache.put("%s %s".formatted(s.getNetworkCode(), s.getStationCode()), (GlobalStation) s);
            }
        }
    }

    private void runSeedlinkThread(SeedlinkNetwork seedlinkNetwork, int reconnectDelay) {
        seedlinkNetwork.status = SeedlinkStatus.CONNECTING;
        seedlinkNetwork.connectedStations = 0;

        SeedlinkReader reader = null;
        try {
            Logger.info("连接到seedlink服务器 \"" + seedlinkNetwork.getName() + "\"");
            reader = new SeedlinkReader(seedlinkNetwork.getHost(), seedlinkNetwork.getPort(), SEEDLINK_TIMEOUT, false, SEEDLINK_TIMEOUT);
            activeReaders.add(reader);

            reader.sendHello();

            reconnectDelay = RECONNECT_DELAY; // 如果连接成功，则重置延迟

            int errors = 0;

            for (AbstractStation station : GlobalQuake.instance.getStationManager().getStations()) {
                if (station.getSeedlinkNetwork() != null && station.getSeedlinkNetwork().equals(seedlinkNetwork)) {
                    Logger.trace("连接到 %s %s %s %s [%s]".formatted(station.getStationCode(), station.getNetworkCode(), station.getChannelName(), station.getLocationCode(), seedlinkNetwork.getName()));
                    try {
                        reader.selectData(station.getNetworkCode(), station.getStationCode(), List.of("%s%s".formatted(station.getLocationCode(),
                                station.getChannelName())));
                        seedlinkNetwork.connectedStations++;
                    } catch (SeedlinkException seedlinkException) {
                        Logger.warn("无法连接到 %s %s %s %s [%s]!".formatted(station.getStationCode(), station.getNetworkCode(), station.getChannelName(), station.getLocationCode(), seedlinkNetwork.getName()));
                        errors++;
                        if (errors > seedlinkNetwork.selectedStations * 0.1) {
                            Logger.warn("seedlink网络 %s 中错误太多，正在重置!".formatted(seedlinkNetwork.getName()));
                            throw seedlinkException;
                        }
                    }
                }
            }

            if (seedlinkNetwork.connectedStations == 0) {
                Logger.info("没有站点连接到 " + seedlinkNetwork.getName());
                seedlinkNetwork.status = SeedlinkStatus.DISCONNECTED;
                return;
            }

            reader.endHandshake();
            seedlinkNetwork.status = SeedlinkStatus.RUNNING;

            while (reader.hasNext()) {
                SeedlinkPacket slp = reader.readPacket();
                try {
                    newPacket(slp.getMiniSeed());
                } catch (SocketException | SeedFormatException se) {
                    Logger.trace(se);
                } catch (Exception e) {
                    Logger.error(e);
                }
            }

            reader.close();
        } catch (Exception e) {
            Logger.warn("seedlink读取器失败于seedlink `%s`: %s".formatted(seedlinkNetwork.getName(), e.getMessage()));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    Logger.error(ex);
                }
                activeReaders.remove(reader);
            }
        }

        seedlinkNetwork.status = SeedlinkStatus.DISCONNECTED;
        seedlinkNetwork.connectedStations = 0;
        Logger.warn("%s 断开连接，%d秒后重新连接...".formatted(seedlinkNetwork.getName(), reconnectDelay));

        try {
            Thread.sleep(reconnectDelay * 1000L);
            if (reconnectDelay < 60 * 5) {
                reconnectDelay *= 2;
            }
        } catch (InterruptedException ignored) {
            Logger.warn("seedlink读取器线程对于 %s 中断".formatted(seedlinkNetwork.getName()));
            return;
        }

        int finalReconnectDelay = reconnectDelay;
        seedlinkReaderService.submit(() -> runSeedlinkThread(seedlinkNetwork, finalReconnectDelay));
    }

    private void newPacket(DataRecord dr) {
        if (lastData == null || dr.getLastSampleBtime().toInstant().isAfter(lastData)) {
            lastData = dr.getLastSampleBtime().toInstant();
        }

        String network = dr.getHeader().getNetworkCode().replaceAll(" ", "");
        String station = dr.getHeader().getStationIdentifier().replaceAll(" ", "");
        var globalStation = stationCache.get("%s %s".formatted(network, station));
        if (globalStation == null) {
            Logger.trace("Seedlink发送了数据给 %s %s，但那从未被选择过！".formatted(network, station));
        } else {
            globalStation.addRecord(dr);
        }
    }

    public void stop() {
        if (seedlinkReaderService != null) {
            seedlinkReaderService.shutdownNow();
            for (Iterator<SeedlinkReader> iterator = activeReaders.iterator(); iterator.hasNext(); ) {
                SeedlinkReader reader = iterator.next();
                reader.close();
                iterator.remove();
            }
            try {
                if (!seedlinkReaderService.awaitTermination(10, TimeUnit.SECONDS)) {
                    Logger.error("无法终止seedlinkReaderService!");
                }
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
        stationCache.clear();
    }

}