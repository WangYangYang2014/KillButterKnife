package com.maihaoche.cx5.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.TextRange;
import com.maihaoche.cx5.CX5Handler;
import com.maihaoche.cx5.Utils;

import java.util.ArrayList;

/**
 * Created by yang on 17/5/15.
 */
public abstract class BaseAction extends AnAction {

    abstract String getTarget();


    @Override
    final public void actionPerformed(AnActionEvent anActionEvent) {
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        if (!editor.getSelectionModel().hasSelection()) {
            Utils.info("请选择需要处理的代码");
            return;
        } else {
            //Access document, caret, and selection
            final Document document = editor.getDocument();
            final CaretModel caretModel = editor.getCaretModel();
            final SelectionModel selectionModel = editor.getSelectionModel();
            final int start = selectionModel.getSelectionStart();
            final int end = selectionModel.getSelectionEnd();
            Runnable runnable = () -> {
                String target = getTarget();
                if (target == null) {
                    target = "";
                }
                //处理findView
                ArrayList<CX5Handler.FindViewInfo> findViewInfos = CX5Handler.getFindView(document.getText(new TextRange(start, end)));
                CX5Handler.TextHandleResult findViewResult = CX5Handler.handleFindView(document, findViewInfos, getTarget(), start);
                //处理onClick
                ArrayList<CX5Handler.OnClickInfo> onClickInfos = CX5Handler.getOnClick(document.getText(new TextRange(start, end + findViewResult.mDelta)));
                CX5Handler.TextHandleResult onClickResult = CX5Handler.handleOnClick(document, onClickInfos, target, start);
                if (!findViewResult.mHandled && !onClickResult.mHandled) {
                    return;
                }
                String delear = "\n";
                int replaceStart = 0;
                if (findViewResult.mHandled) {
                    delear += "findView(" + target + ");\n";
                    replaceStart = findViewResult.mReplaceStart;
                }
                if (onClickResult.mHandled) {
                    delear += "setOnClick(" + target + ");\n";
                    replaceStart = onClickResult.mReplaceStart;
                }
                if (findViewResult.mHandled && onClickResult.mHandled) {
                    delear = "\nbindView(" + target + ");\nprivate void bindView(" + (target.equals("") ? target : "View " + target) + "){" + delear + "}\n";
                    replaceStart = Math.min(findViewResult.mReplaceStart, onClickResult.mReplaceStart);
                }
                document.replaceString(replaceStart, replaceStart, delear);
                caretModel.moveToLogicalPosition(new LogicalPosition(document.getLineNumber(replaceStart) + 1, 0));
                selectionModel.selectLineAtCaret();

            };
            //Making the replacement
            WriteCommandAction.runWriteCommandAction(anActionEvent.getRequiredData(CommonDataKeys.PROJECT), runnable);
            selectionModel.removeSelection();
        }
    }

    @Override
    final public void update(AnActionEvent e) {
        e.getPresentation().setVisible(Utils.isEditorInFocuse(e));
    }


}
