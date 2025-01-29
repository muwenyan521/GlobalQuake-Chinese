package globalquake.core.database;

import globalquake.core.GlobalQuake;
import globalquake.core.exception.FatalIOException;
import globalquake.core.exception.FdnwsDownloadException;
import org.tinylog.Logger;

import java.io.*;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class StationDatabaseManager {

    private static final int ATTEMPTS = 3;
    private StationDatabase stationDatabase;

    private final List<Runnable> updateListeners = new CopyOnWriteArrayList<>();

    private final List<Runnable> statusListeners = new CopyOnWriteArrayList<>();
    private boolean updating = false;

    public StationDatabaseManager() {
    }

    public StationDatabaseManager(StationDatabase stationDatabase) {
        this.stationDatabase = stationDatabase;
    }

    public void load() throws FatalIOException {
        File file = getDatabaseFile();
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new FatalIOException("无法创建数据库文件目录!", null);
            }
        }

        if (file.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                stationDatabase = (StationDatabase) in.readObject();
                in.close();

                Logger.info("数据库加载成功");
            } catch (ClassNotFoundException | IOException e) {
                GlobalQuake.getErrorHandler().handleException(
                        new FatalIOException("无法加载台站数据库,它可能已损坏!", e));
            }
        }

        if (stationDatabase == null) {
            Logger.info("创建了一个新的数据库");
            stationDatabase = new StationDatabase();
        }

    }

    public void save() throws FatalIOException {
        File file = getDatabaseFile();
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new FatalIOException("无法创建数据库文件目录!", null);
            }
        }

        if (stationDatabase == null) {
            return;
        }

        stationDatabase.getDatabaseReadLock().lock();
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(stationDatabase);
            out.close();
            Logger.info("台站数据库成功保存");
        } catch (IOException e) {
            throw new FatalIOException("无法保存台站数据库!", e);
        } finally {
            stationDatabase.getDatabaseReadLock().unlock();
        }
    }

    public void addUpdateListener(Runnable runnable) {
        this.updateListeners.add(runnable);
    }

    public void addStatusListener(Runnable runnable) {
        this.statusListeners.add(runnable);
    }

    public void fireUpdateEvent() {
        for (Runnable runnable : updateListeners) {
            runnable.run();
        }
    }

    private void fireStatusChangeEvent() {
        for (Runnable runnable : statusListeners) {
            runnable.run();
        }
    }

    public void runUpdate(List<StationSource> toBeUpdated, Runnable onFinish) {
        this.updating = true;
        fireStatusChangeEvent();

        final Object statusSync = new Object();

        new Thread(() -> {
            toBeUpdated.forEach(stationSource -> {
                stationSource.getStatus().setString("排队...");
                stationSource.getStatus().setValue(0);
            });
            toBeUpdated.parallelStream().forEach(stationSource -> {
                try {
                    synchronized (statusSync) {
                        stationSource.getStatus().setString("正在更新...");
                    }
                    List<Network> networkList = FDSNWSDownloader.downloadFDSNWS(stationSource, "");

                    synchronized (statusSync) {
                        stationSource.getStatus().setString("正在更新数据库...");
                    }

                    StationDatabaseManager.this.acceptNetworks(networkList);

                    synchronized (statusSync) {
                        stationSource.getStatus().setString(networkList.size() + " 个节点已下载");
                        stationSource.getStatus().setValue(100);
                        stationSource.setLastUpdate(LocalDateTime.now());
                    }
                } catch (SocketTimeoutException e) {
                    Logger.error(e);
                    synchronized (statusSync) {
                        stationSource.getStatus().setString("超时!");
                        stationSource.getStatus().setValue(0);
                    }
                } catch (FdnwsDownloadException e) {
                    Logger.error(e);
                    synchronized (statusSync) {
                        stationSource.getStatus().setString(e.getUserMessage());
                        stationSource.getStatus().setValue(0);
                    }
                } catch (Exception e) {
                    Logger.error(e);
                    synchronized (statusSync) {
                        stationSource.getStatus().setString("错误!");
                        stationSource.getStatus().setValue(0);
                    }
                } finally {
                    fireUpdateEvent();
                }
            });

            this.updating = false;
            fireStatusChangeEvent();
            if (onFinish != null) {
                onFinish.run();
            }
        }).start();
    }

    protected void acceptNetworks(List<Network> networkList) {
        stationDatabase.getDatabaseWriteLock().lock();
        try {
            for (Network network : networkList) {
                for (Station station : network.getStations()) {
                    for (Channel channel : station.getChannels()) {
                        stationDatabase.acceptChannel(network, station, channel);
                    }
                }
            }
        } finally {
            stationDatabase.getDatabaseWriteLock().unlock();
        }
    }

    public static File getDatabaseFile() {
        return new File(getStationsFolder(), "database.dat");
    }

    private static File getStationsFolder() {
        return new File(GlobalQuake.mainFolder, "/stationDatabase/");
    }

    public StationDatabase getStationDatabase() {
        return stationDatabase;
    }

    private final Object statusSync = new Object();

    public void runAvailabilityCheck(List<SeedlinkNetwork> toBeUpdated, Runnable onFinish) {
        this.updating = true;
        toBeUpdated.forEach(seedlinkNetwork -> seedlinkNetwork.setStatus(0, "排队..."));
        fireStatusChangeEvent();

        new Thread(() -> {
            toBeUpdated.parallelStream().forEach(seedlinkNetwork -> {
                        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
                            if(runSeedlinkUpdate(seedlinkNetwork, attempt + 1)){
                                break;
                            }
                        }
                    }
            );
            this.updating = false;
            fireStatusChangeEvent();
            if (onFinish != null) {
                onFinish.run();
            }
        }).start();
    }

    private boolean runSeedlinkUpdate(SeedlinkNetwork seedlinkNetwork, int attempt) {
        synchronized (statusSync) {
            seedlinkNetwork.setStatus(0, "正在更新...");
        }

        try {
            SeedlinkCommunicator.runAvailabilityCheck(seedlinkNetwork, stationDatabase, attempt);
        } catch (Exception ce) {
            Logger.warn("无法从Seedlink节点服务器获取台站数据 `%s`: %s".formatted(seedlinkNetwork.getName(), ce.getMessage()));
            synchronized (statusSync) {
                seedlinkNetwork.setStatus(0, "网络错误: " + ce.getMessage());
            }
            return false;
        }

        synchronized (statusSync) {
            seedlinkNetwork.setStatus(100, "完成");
        }

        fireUpdateEvent();
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUpdating() {
        return updating;
    }

    public void restore() {
        getStationDatabase().getDatabaseWriteLock().lock();
        try {
            removeAllSeedlinks(getStationDatabase().getSeedlinkNetworks());
            removeAllStationSources(getStationDatabase().getStationSources());
            getStationDatabase().addDefaults();
            fireUpdateEvent();
        } finally {
            getStationDatabase().getDatabaseWriteLock().unlock();
        }
    }

    public void removeAllSeedlinks(List<SeedlinkNetwork> toBeRemoved) {
        for (Network network : getStationDatabase().getNetworks()) {
            for (Station station : network.getStations()) {
                for (Channel channel : station.getChannels()) {
                    toBeRemoved.forEach(channel.getSeedlinkNetworks()::remove);
                }
                if (station.getSelectedChannel() != null && !station.getSelectedChannel().isAvailable()) {
                    station.selectBestAvailableChannel();
                }
            }
        }

        getStationDatabase().getSeedlinkNetworks().removeAll(toBeRemoved);
        fireUpdateEvent();
    }

    public void removeAllStationSources(List<StationSource> toBeRemoved) {
        for (Iterator<Network> networkIterator = getStationDatabase().getNetworks().iterator(); networkIterator.hasNext(); ) {
            Network network = networkIterator.next();
            for (Iterator<Station> stationIterator = network.getStations().iterator(); stationIterator.hasNext(); ) {
                Station station = stationIterator.next();
                for (Iterator<Channel> channelIterator = station.getChannels().iterator(); channelIterator.hasNext(); ) {
                    Channel channel = channelIterator.next();
                    toBeRemoved.forEach(channel.getStationSources()::remove);
                    if (channel.getStationSources().isEmpty()) {
                        channelIterator.remove();
                    }
                }
                if (station.getChannels().isEmpty()) {
                    stationIterator.remove();
                } else if (station.getSelectedChannel() != null) {
                    if (!station.getChannels().contains(station.getSelectedChannel())) {
                        station.selectBestAvailableChannel();
                    }
                }
            }
            if (network.getStations().isEmpty()) {
                networkIterator.remove();
            }
        }

        getStationDatabase().getStationSources().removeAll(toBeRemoved);

        fireUpdateEvent();
    }

    /**
     * @return {totalStations, connectedStations, runningSeedlinks, totalSeedlinks}
     */
    public int[] getSummary() {
        int totalStations = 0;
        int connectedStations = 0;
        int runningSeedlinks = 0;
        int totalSeedlinks = 0;
        for (SeedlinkNetwork seedlinkNetwork : getStationDatabase().getSeedlinkNetworks()) {
            totalStations += seedlinkNetwork.selectedStations;
            connectedStations += seedlinkNetwork.connectedStations;
            if (seedlinkNetwork.selectedStations > 0) {
                totalSeedlinks++;
            }
            if (seedlinkNetwork.status == SeedlinkStatus.RUNNING) {
                runningSeedlinks++;
            }
        }

        return new int[]{totalStations, connectedStations, runningSeedlinks, totalSeedlinks};
    }
}
