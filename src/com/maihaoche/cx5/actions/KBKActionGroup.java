package com.maihaoche.cx5.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.maihaoche.cx5.Utils;

/**
 * Created by yang on 17/5/15.
 */
public class KBKActionGroup extends DefaultActionGroup {


    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setVisible(Utils.isEditorInFocuse(e));
    }
}
