package org.hunmr.acejump;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import org.hunmr.acejump.command.CopyRangeAfterJumpCommand;
import org.hunmr.common.selector.Selector;
import org.hunmr.util.EditorUtils;

public class AceJumpCopyRangeAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String actionId = e.getActionManager().getId(this);
        String selectorClassName = "org.hunmr.common.selector."
                + actionId.substring("emacsIDEAs.AceJumpCopy.".length())
                + "Selector";

        try {
            Class<? extends Selector> selectorClass = (Class<? extends Selector>) Class.forName(selectorClassName);
            EditorUtils.copyRange(selectorClass, editor);

            AceJumpAction.getInstance().addCommandAroundJump(new CopyRangeAfterJumpCommand(editor, selectorClass));
            AceJumpAction.getInstance().performAction(e);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}