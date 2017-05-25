package com.maihaoche.cx5;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

/**
 * Created by yang on 17/5/15.
 */
public class Utils {

    private static final NotificationGroup GROUP_DISPLAY_ID_INFO_BALLOON =
            new NotificationGroup("com.maihaoche.cx5",
                    NotificationDisplayType.BALLOON, true);

    /**
     * 当前editor是否打开
     *
     * @param e
     * @return
     */
    public static boolean isEditorInFocuse(AnActionEvent e) {
        if (e == null) {
            return false;
        }
        //Get required data keys
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        //Set visibility only in case of existing project and editor
        return (project != null && editor != null);
    }


    /**
     * 当前editor是否打开,并且选中了某些文字
     *
     * @param e
     * @return
     */
    public static boolean hasTextSelected(AnActionEvent e) {
        if (e == null) {
            return false;
        }
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        //Set visibility only in case of existing project and editor
        return (isEditorInFocuse(e) && editor.getSelectionModel().hasSelection());
    }

    /**
     * 冒气泡显示错误信息
     */
    public static void info(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = GROUP_DISPLAY_ID_INFO_BALLOON.createNotification(errorMsg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notificationX);
    }

    /**
     * 冒气泡显示错误信息
     */
    public static void error(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = GROUP_DISPLAY_ID_INFO_BALLOON.createNotification(errorMsg, NotificationType.ERROR);
        Notifications.Bus.notify(notificationX);
    }
}
