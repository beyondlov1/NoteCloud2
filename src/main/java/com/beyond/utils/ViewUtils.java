package com.beyond.utils;

import com.beyond.entity.FxDocument;
import com.beyond.entity.Todo;
import javafx.scene.web.WebView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author beyondlov1
 * @date 2019/01/05
 */
public class ViewUtils {

    /**
     * 加载webview
     * @param fxDocument
     * @param webView
     */
    public static void loadContentForWebView(FxDocument fxDocument, WebView webView){
        if (fxDocument==null) return;
        if (fxDocument.toNormalDocument() instanceof Todo
                && ((Todo)fxDocument.toNormalDocument()).getReminder().getRemindTime()!=null){
            String timeStamp = "";
            Todo todo = (Todo)fxDocument.toNormalDocument();
            Date remoteRemindTime = todo.getReminder().getRemoteRemindTime();
            if (remoteRemindTime !=null){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeStamp = "  \n\n\n***\n提醒时间:"+ simpleDateFormat.format(remoteRemindTime);
            }
            //webview加载内容
            String content = TimeUtils.getContentBaseOnNow(fxDocument.getContent(),todo.getReminder().getRemindTime());
            webView.getEngine().loadContent(MarkDownUtils.convertMarkDownToHtml(content+timeStamp));
        }else {
            webView.getEngine().loadContent(MarkDownUtils.convertMarkDownToHtml(fxDocument.getContent()));
        }
    }
}
