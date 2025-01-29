package globalquake.ui.action;

import globalquake.core.database.StationDatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class RestoreDatabaseAction extends AbstractAction {

    private final StationDatabaseManager databaseManager;
    private final Window parent;

    public RestoreDatabaseAction(Window parent, StationDatabaseManager databaseManager){
        super("恢复默认");
        this.databaseManager = databaseManager;
        this.parent = parent;

        putValue(SHORT_DESCRIPTION, "将一切恢复到默认状态");

        ImageIcon restoreIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/image_icons/restore.png")));
        Image image = restoreIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        putValue(Action.SMALL_ICON, scaledIcon);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        int option = JOptionPane.showConfirmDialog(parent,
                "你确定要将所有内容恢复到默认状态吗?",
                "确定",
                JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        databaseManager.restore();
    }
}
