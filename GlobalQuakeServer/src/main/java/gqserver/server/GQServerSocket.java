package gqserver.server;

import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.exception.RuntimeApplicationException;
import globalquake.utils.monitorable.MonitorableCopyOnWriteArrayList;
import gqserver.api.GQApi;
import gqserver.api.Packet;
import gqserver.api.ServerClient;
import gqserver.api.exception.PacketLimitException;
import gqserver.api.packets.system.HandshakePacket;
import gqserver.api.packets.system.HandshakeSuccessfulPacket;
import gqserver.api.packets.system.TerminationPacket;
import gqserver.events.specific.ClientJoinedEvent;
import gqserver.events.specific.ClientLeftEvent;
import gqserver.events.specific.ServerStatusChangedEvent;
import gqserver.api.exception.UnknownPacketException;
import gqserver.main.Main;
import gqserver.ui.server.tabs.StatusTab;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GQServerSocket {

    private static final int HANDSHAKE_TIMEOUT = 10 * 1000;
    private static final int WATCHDOG_TIMEOUT = 60 * 1000;

    public static final int READ_TIMEOUT = WATCHDOG_TIMEOUT + 10 * 1000;
    private static final int CONNECTIONS_LIMIT = 3;
    private final DataService dataService;
    private SocketStatus status;
    private ExecutorService handshakeService;
    private ExecutorService readerService;
    private ScheduledExecutorService clientsWatchdog;
    private ScheduledExecutorService clientsLimitWatchdog;
    private ScheduledExecutorService statusReportingService;
    private final List<ServerClient> clients;

    private GQServerStats stats;

    private volatile ServerSocket lastSocket;
    private final Object joinMutex = new Object();
    private final Object connectionsMapLock = new Object();

    private final Map<String, Integer> connectionsMap = new HashMap<>();

    public GQServerSocket() {
        status = SocketStatus.IDLE;
        clients = new MonitorableCopyOnWriteArrayList<>();
        dataService = new DataService();
    }

    public void run(String ip, int port) {
        Logger.tag("Server").info("创建服务器中...");
        ExecutorService acceptService = Executors.newSingleThreadExecutor();
        handshakeService = Executors.newCachedThreadPool();
        readerService = Executors.newCachedThreadPool();
        clientsWatchdog = Executors.newSingleThreadScheduledExecutor();
        clientsLimitWatchdog = Executors.newSingleThreadScheduledExecutor();
        statusReportingService = Executors.newSingleThreadScheduledExecutor();
        stats = new GQServerStats();

        setStatus(SocketStatus.OPENING);
        try {
            lastSocket = new ServerSocket();
            Logger.tag("Server").info("绑定端口 %d 中...".formatted(port));
            lastSocket.bind(new InetSocketAddress(ip, port));
            clientsWatchdog.scheduleAtFixedRate(this::checkClients, 0, 10, TimeUnit.SECONDS);
            clientsLimitWatchdog.scheduleAtFixedRate(this::updateLimits, 0, 60, TimeUnit.SECONDS);
            acceptService.submit(this::runAccept);

            if(Main.isHeadless()){
                statusReportingService.scheduleAtFixedRate(this::printStatus, 0, 30, TimeUnit.SECONDS);
            }

            dataService.run();
            setStatus(SocketStatus.RUNNING);
            Logger.tag("Server").info("服务器启动成功");
        } catch (IOException e) {
            setStatus(SocketStatus.IDLE);
            throw new RuntimeApplicationException("无法打开服务器", e);
        }
    }

    private void updateLimits() {
        clients.forEach(ServerClient::updateLimits);
    }

    private void printStatus() {
        long maxMem = Runtime.getRuntime().maxMemory();
        long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        int[] summary = GlobalQuakeServer.instance.getStationDatabaseManager().getSummary();

        Logger.tag("ServerStatus").info("服务器状态:客户端:%d / %d,内存:%.2f / %.2f GB,种子链接:%d / %d,台站:%d / %d"
                .formatted(clients.size(), Settings.maxClients, usedMem / StatusTab.GB, maxMem / StatusTab.GB,
                        summary[2], summary[3], summary[1], summary[0]));

        if (stats != null) {
            Logger.tag("ServerStatus").info(
                    "已接受:%d,版本错误:%d,数据包错误:%d,服务器已满:%d,成功:%d,错误:%d,IP拒绝:%d"
                    .formatted(stats.accepted, stats.wrongVersion, stats.wrongPacket, stats.serverFull, stats.successfull, stats.errors, stats.ipRejects));
        }
    }

    private void checkClients() {
        try {
            List<ServerClient> toRemove = new LinkedList<>();
            for (ServerClient client : clients) {
                if (!client.isConnected() || System.currentTimeMillis() - client.getLastHeartbeat() > WATCHDOG_TIMEOUT) {
                    try {
                        client.destroy();
                        toRemove.add(client);
                        clientLeft(client.getSocket());
                        GlobalQuakeServer.instance.getServerEventHandler().fireEvent(new ClientLeftEvent(client));
                        Logger.tag("Server").info("客户端 #%d 因超时而断开连接".formatted(client.getID()));
                    } catch (Exception e) {
                        Logger.tag("Server").error(e);
                    }
                }
            }
            clients.removeAll(toRemove);
        }catch(Exception e) {
            Logger.tag("Server").error(e);
        }
    }

    private boolean handshake(ServerClient client) throws IOException {
        Packet packet;
        try {
            packet = client.readPacket();
        } catch (UnknownPacketException | PacketLimitException e) {
            client.destroy();
            Logger.tag("Server").error(e);
            return false;
        }

        if (packet instanceof HandshakePacket handshakePacket) {
            if (handshakePacket.compatVersion() != GQApi.COMPATIBILITY_VERSION) {
                stats.wrongVersion++;
                client.destroy(("您的客户端版本与服务器不兼容!" +
                        " 服务器正在运行版本 %s").formatted(GlobalQuake.version));
                return false;
            }

            client.setClientConfig(handshakePacket.clientConfig());
        } else {
            stats.wrongPacket++;
            Logger.tag("Server").warn("客户端发送了无效的初始数据包!");
            client.destroy();
            return false;
        }

        synchronized (joinMutex) {
            if (clients.size() >= Settings.maxClients) {
                client.destroy("服务器已满!");
                stats.serverFull++;
                return false;
            } else {
                Logger.tag("Server").info("客户端 #%d 握手成功".formatted(client.getID()));
                stats.successfull++;
                client.sendPacket(new HandshakeSuccessfulPacket());
                readerService.submit(new ClientReader(client));
                clients.add(client);
                GlobalQuakeServer.instance.getServerEventHandler().fireEvent(new ClientJoinedEvent(client));
            }
        }

        return true;
    }

    private void onClose() {
        clients.clear();

        GlobalQuake.instance.stopService(clientsLimitWatchdog);
        GlobalQuake.instance.stopService(clientsWatchdog);
        GlobalQuake.instance.stopService(readerService);
        GlobalQuake.instance.stopService(handshakeService);
        GlobalQuake.instance.stopService(statusReportingService);

        dataService.stop();
        // we are the acceptservice
        setStatus(SocketStatus.IDLE);
    }

    public void setStatus(SocketStatus status) {
        this.status = status;
        if (GlobalQuakeServer.instance != null) {
            GlobalQuakeServer.instance.getServerEventHandler().fireEvent(new ServerStatusChangedEvent(status));
        }
    }

    public SocketStatus getStatus() {
        return status;
    }

    public void stop() throws IOException {
        for (ServerClient client : clients) {
            try {
                client.sendPacket(new TerminationPacket("服务器已由操作员关闭"));
                client.flush();
                client.destroy();
            } catch (Exception e) {
                Logger.tag("Server").error(e);
            }
        }

        clients.clear();

        if (lastSocket != null) {
            lastSocket.close();
        }
    }

    private void runAccept() {
        while (lastSocket.isBound() && !lastSocket.isClosed()) {
            try {
                lastSocket.setSoTimeout(0); // we can wait for clients forever
                Socket socket = lastSocket.accept();

                if(!checkAddress(socket)){
                    socket.close();
                    Logger.tag("Server").warn("客户端因达到最大连接数而被拒绝!");
                    stats.ipRejects++;
                    continue;
                }

                stats.accepted++;

                Logger.tag("Server").info("新客户端加入...");
                socket.setSoTimeout(HANDSHAKE_TIMEOUT);

                handshakeService.submit(() -> {
                    ServerClient client;
                    try {
                        client = new ServerClient(socket);
                        Logger.tag("Server").info("为客户端 #%d 执行握手".formatted(client.getID()));
                        if(!handshake(client)){
                            clientLeft(socket);
                        }
                    } catch (IOException e) {
                        stats.errors++;
                        Logger.tag("Server").error("接受客户端时失败!");
                        Logger.tag("Server").trace(e);
                        clientLeft(socket);
                    }
                });
            } catch (IOException e) {
                break;
            }
        }

        onClose();
    }

    private void clientLeft(Socket socket) {
        String address = getRemoteAddress(socket);
        synchronized (connectionsMapLock) {
            connectionsMap.put(address, connectionsMap.get(address) - 1);
        }
    }

    private boolean checkAddress(Socket socket) {
        String address = getRemoteAddress(socket);
        synchronized (connectionsMapLock) {
            int connections = connectionsMap.getOrDefault(address, 1);

            if (connections > CONNECTIONS_LIMIT) {
                return false;
            }

            connectionsMap.put(address, connections + 1);

            return true;
        }
    }

    private String getRemoteAddress(Socket socket) {
        return (((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString();
    }

    public int getClientCount() {
        return clients.size();
    }

    public List<ServerClient> getClients() {
        return clients;
    }

    public DataService getDataService() {
        return dataService;
    }
}
