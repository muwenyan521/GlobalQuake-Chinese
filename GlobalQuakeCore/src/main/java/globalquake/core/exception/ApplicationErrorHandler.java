package globalquake.core.exception;

import globalquake.core.action.OpenURLAction;
import globalquake.core.exception.action.IgnoreAction;
import globalquake.core.exception.action.TerminateAction;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ApplicationErrorHandler implements Thread.UncaughtExceptionHandler {

    private final boolean headless;
    private Window parent;

    private int errorCount = 0;


    public ApplicationErrorHandler(Window parent, boolean headless) {
        this.parent = parent;
        this.headless = headless;
    }

    public void setParent(Window parent) {
        this.parent = parent;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.error("线程 {} 中发生了未捕获的异常: {}", t.getName(), e.getMessage());
        Logger.error(e);
        handleException(e);
    }

    public synchronized void handleException(Throwable e) {
        Logger.error(e);

        if (headless) {
            return;
        }

        if (e instanceof OutOfMemoryError) {
            showOOMError(e);
            return;
        }

        if (!(e instanceof RuntimeApplicationException)) {
            showDetailedError(e);
            return;
        }

        if (e instanceof FatalError ex) {
            showGeneralError(ex.getUserMessage(), true);
        } else {
            ApplicationException ex = (ApplicationException) e;
            showGeneralError(ex.getUserMessage(), false);
        }
    }

    private void showOOMError(Throwable e) {
        Logger.error(e);
        final Object[] options = getOptionsForDialog(true, false);
        JOptionPane.showOptionDialog(parent, createOOMPanel(), "内存不足!", JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, null);
    }

    public synchronized void handleWarning(Throwable e) {
        Logger.warn(e);

        if (headless) {
            return;
        }

        showWarning(e.getMessage());
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(parent, message, "警告", JOptionPane.WARNING_MESSAGE);
    }


    public void info(String s) {
        if (headless) {
            Logger.info(s);
        } else {
            JOptionPane.showMessageDialog(parent, s, "信息", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDetailedError(Throwable e) {
        errorCount++;
        if (errorCount == 2) {
            System.exit(0);
        }
        final Object[] options = getOptionsForDialog(true, true);
        JOptionPane.showOptionDialog(parent, createDetailedPane(e), "严重错误", JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, null);
        errorCount = 0;
    }

    private Component createOOMPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel labelsPanel = new JPanel(new GridLayout(2, 1));

        labelsPanel.add(new JLabel("GlobalQuake内存不足!"));
        labelsPanel.add(new JLabel("请选择较少的台站或连接到服务器."));

        panel.add(labelsPanel, BorderLayout.NORTH);

        return panel;
    }

    private Component createDetailedPane(Throwable e) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel labelsPanel = new JPanel(new GridLayout(2, 1));

        labelsPanel.add(new JLabel("哎呀!GlobalQuake内部出现了严重错误."));
        labelsPanel.add(new JLabel("请将以下文本发送给开发者,以便他们尽快修复:"));

        panel.add(labelsPanel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(16, 60);
        textArea.setEditable(false);
        StringWriter stackTraceWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTraceWriter));
        textArea.append(stackTraceWriter.toString());

        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private void showGeneralError(String message, boolean isFatal) {
        final String title = isFatal ? "严重错误" : "应用程序错误";
        final Object[] options = getOptionsForDialog(isFatal, true);

        JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                options, null);
    }

    private Component[] getOptionsForDialog(boolean isFatal, boolean github) {
        if (!isFatal) {
            return null; // use default
        }

        if (github) {

            return new Component[]{new JButton(new TerminateAction()), new JButton(new OpenURLAction("https://github.com/xspanger3770/GlobalQuake/issues", "在GitHub上打开Issues")),
                    new JButton(new IgnoreAction())};
        } else {
            return new Component[]{new JButton(new TerminateAction()), new JButton(new IgnoreAction())};
        }
    }

}