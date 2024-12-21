package globalquake.core.database;

import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import gqserver.api.packets.station.InputType;
import org.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class SeedlinkCommunicator {

    public static final long UNKNOWN_DELAY = Long.MIN_VALUE;
    private static final ThreadLocal<SimpleDateFormat> FORMAT_UTC_SHORT = new ThreadLocal<>();
    private static final ThreadLocal<SimpleDateFormat> FORMAT_UTC_LONG = new ThreadLocal<>();
    private static final long MAX_DELAY_MS = 1000 * 60 * 60 * 24L;

    public static void runAvailabilityCheck(SeedlinkNetwork seedlinkNetwork, StationDatabase stationDatabase, int attempt) throws Exception {
        if(attempt > 1){
            Logger.warn("尝试第%d/3次从%s获取可用站点".formatted(attempt, seedlinkNetwork.getName()));
        }

        seedlinkNetwork.setStatus(0, attempt == 1 ? "正在连接..." : "正在连接...（尝试第%d/3次）".formatted(attempt));
        SeedlinkReader reader = new SeedlinkReader(seedlinkNetwork.getHost(), seedlinkNetwork.getPort(), seedlinkNetwork.getTimeout(), false, seedlinkNetwork.getTimeout());

        seedlinkNetwork.setStatus(33, "正在下载...");
        String infoString = reader.getInfoString(SeedlinkReader.INFO_STREAMS).trim().replaceAll("[^\\u0009\\u000a\\u000d\\u0020-\\uD7FF\\uE000-\\uFFFD]", " ");

        seedlinkNetwork.setStatus(66, "正在解析...");
        parseAvailability(infoString, stationDatabase, seedlinkNetwork);

        seedlinkNetwork.setStatus(80, "完成中...");
        reader.close();

        seedlinkNetwork.setStatus(99, "完成");

    }

    private static void parseAvailability(String infoString, StationDatabase stationDatabase, SeedlinkNetwork seedlinkNetwork) throws Exception {
        seedlinkNetwork.availableStations = 0;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(infoString)));
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("station");
        Logger.info("在Seedlink节点 %s中发现了%d个可用站点".formatted(nodeList.getLength(), seedlinkNetwork.getName()));
        for (int itr = 0; itr < nodeList.getLength(); itr++) {
            Node node = nodeList.item(itr);
            String stationCode = node.getAttributes().getNamedItem("name").getTextContent();
            String networkCode = node.getAttributes().getNamedItem("network").getTextContent();
            NodeList channelList = ((Element) node).getElementsByTagName("stream");
            for (int k = 0; k < channelList.getLength(); k++) {
                Node channel = channelList.item(k);
                String locationCode = channel.getAttributes().getNamedItem("location").getTextContent();
                String channelName = channel.getAttributes().getNamedItem("seedname").getTextContent();
                String endDate = channel.getAttributes().getNamedItem("end_time").getTextContent();

                long delay = UNKNOWN_DELAY;

                try {
                    if(FORMAT_UTC_LONG.get() == null || FORMAT_UTC_SHORT.get() == null){
                        FORMAT_UTC_SHORT.set(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                        FORMAT_UTC_SHORT.get().setTimeZone(TimeZone.getTimeZone("UTC"));

                        FORMAT_UTC_LONG.set(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS"));
                        FORMAT_UTC_LONG.get().setTimeZone(TimeZone.getTimeZone("UTC"));
                    }

                    Calendar end = Calendar.getInstance();
                    end.setTime(endDate.contains("-") ? FORMAT_UTC_SHORT.get().parse(endDate) : FORMAT_UTC_LONG.get().parse(endDate));

                    delay = System.currentTimeMillis() - end.getTimeInMillis();

                    if (delay > MAX_DELAY_MS) {
                        continue;
                    }

                } catch(NumberFormatException e){
                    Logger.warn(new RuntimeException("无法从%s获取延迟, %s: %s".formatted(stationCode, seedlinkNetwork.getName(), e.getMessage())));
                }

                addAvailableChannel(networkCode, stationCode, channelName, locationCode, delay, seedlinkNetwork, stationDatabase);
            }
        }
    }

    private static void addAvailableChannel(String networkCode, String stationCode, String channelName, String locationCode, long delay, SeedlinkNetwork seedlinkNetwork, StationDatabase stationDatabase) {
        locationCode = locationCode.trim();
        stationDatabase.getDatabaseWriteLock().lock();
        try {
            Station station = StationDatabase.getStation(stationDatabase.getNetworks(), networkCode, stationCode);
            if(station == null){
                return; // :(
            }

            Channel channel = StationDatabase.getChannel(stationDatabase.getNetworks(), networkCode, stationCode, channelName, locationCode);

            if(channel == null){
                channel = findChannelButDontUseLocationCode(station, channelName);

                if(channel != null){
                    var any = channel.getStationSources().stream().findAny();
                    Channel newChannel = StationDatabase.getOrCreateChannel(station, channelName, locationCode, channel.getLatitude(), channel.getLongitude(), channel.getElevation(), channel.getSampleRate(), any.orElse(null), -1, InputType.UNKNOWN);
                    Logger.warn("未找到[%s %s %s `%s`]的精确匹配,假设位置代码为`%s`".formatted(networkCode, stationCode, channelName, locationCode, channel.getLocationCode()));
                    channel = newChannel;
                }
            }

            if (channel == null) {
                return;
            }

            seedlinkNetwork.availableStations++;
            channel.getSeedlinkNetworks().put(seedlinkNetwork, delay);
        }finally {
            stationDatabase.getDatabaseWriteLock().unlock();
        }
    }

    private static Channel findChannelButDontUseLocationCode(Station station, String channelName) {
        List<Channel> channels = new ArrayList<>(station.getChannels()).stream().filter(channel -> channel.getCode().equals(channelName)).sorted(Comparator.comparing(Channel::getLocationCode)).toList();
        if(channels.isEmpty()){
            return null;
        }

        return channels.get(0);
    }

}
