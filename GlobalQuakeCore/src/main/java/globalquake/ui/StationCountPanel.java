package globalquake.ui;

import globalquake.core.database.Channel;
import globalquake.core.database.Network;
import globalquake.core.database.Station;
import globalquake.core.database.StationDatabaseManager;
import globalquake.ui.stationselect.StationColor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class StationCountPanel extends JPanel {
    private final StationDatabaseManager manager;
    private final CounterPanel total;
    private final CounterPanel available;
    private final CounterPanel selected;
    private final CounterPanel unavailable;

    public StationCountPanel(StationDatabaseManager manager, LayoutManager layoutManager) {
        this.manager = manager;
        setLayout(layoutManager);

        add(total = new CounterPanel("总测站信道数", StationColor.ALL));
        add(available = new CounterPanel("可用测站信道数", StationColor.AVAILABLE));
        add(selected = new CounterPanel("已选测站信道数", StationColor.SELECTED));
        add(unavailable = new CounterPanel("不可用测站信道数", StationColor.UNAVAILABLE));

        manager.addUpdateListener(this::recalculate);

        recalculate();
    }

    public void recalculate() {
        int tot = 0;
        int ava = 0;
        int sel = 0;
        int unb = 0;
        manager.getStationDatabase().getDatabaseReadLock().lock();
        try{
            for(Network network : manager.getStationDatabase().getNetworks()){
                for(Station station: network.getStations()){
                    for(Channel channel:station.getChannels()){
                        tot++;
                        if(channel.isAvailable()){
                            ava++;
                        }
                        if(channel.equals(station.getSelectedChannel())){
                            if(channel.isAvailable()){
                                sel++;
                            } else {
                                unb++;
                            }
                        }
                    }
                }
            }
        } finally {
            manager.getStationDatabase().getDatabaseReadLock().unlock();
        }
        total.setCount(tot);
        available.setCount(ava);
        selected.setCount(sel);
        unavailable.setCount(unb);
    }

    static class CounterPanel extends JPanel
    {
        private final String name;
        private final JLabel label;
        private int count;
        public CounterPanel(String name, Color color){
            this.name = name;
            add(new StationIcon(color));
            add(label = new JLabel());
            setBorder(BorderFactory.createRaisedBevelBorder());
            updateLabel();
        }

        private void updateLabel() {
            label.setText("%d %s".formatted(count, name));
        }

        @SuppressWarnings("unused")
        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
            updateLabel();
            repaint();
        }

        private static class StationIcon extends JPanel {
            private final Color color;

            public StationIcon(Color color) {
                this.color = color;
                setPreferredSize(new Dimension(22,22));
            }

            @Override
            public void paint(Graphics gr) {
                super.paint(gr);
                Graphics2D g = (Graphics2D) gr;
                int size = Math.min(getWidth(), getHeight()) - 1;

                Path2D path = new Path2D.Double();
                path.moveTo(0, size * (Math.sqrt(3) / 2.0));
                path.lineTo(size, size * (Math.sqrt(3) / 2.0));
                path.lineTo(size/ 2.0, 0);
                path.closePath();

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.fill(path);
                g.setColor(Color.black);
                g.draw(path);
            }
        }
    }
}
