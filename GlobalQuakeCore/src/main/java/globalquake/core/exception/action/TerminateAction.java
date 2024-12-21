package globalquake.core.exception.action;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Action responsible for terminating the application.
 */
public final class TerminateAction extends AbstractAction {

    public TerminateAction() {
        super("终止");
        putValue(SHORT_DESCRIPTION, "终止应用程序");
        putValue(MNEMONIC_KEY, KeyEvent.VK_T);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
