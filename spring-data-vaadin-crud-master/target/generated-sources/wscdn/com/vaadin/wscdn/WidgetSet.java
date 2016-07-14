package com.vaadin.wscdn;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/* Addons in this ws:
 * 7.6.2
 * OBF
 * switch:org.vaadin.teemu:2.0.2

 */
@WebListener
public class WidgetSet implements javax.servlet.http.HttpSessionListener, 
    java.io.Serializable {
    private boolean inited = false;

    private String wsUrl = "//wscdn.vaadin.com/ws/vwscdn7c87ce1831b3238a4be59c5f180e7c99/vwscdn7c87ce1831b3238a4be59c5f180e7c99.nocache.js";
    private String wsName = "vwscdn7c87ce1831b3238a4be59c5f180e7c99";
    private boolean wsReady = true;


    public WidgetSet() { 
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        if(!inited) {
            VaadinServletService.getCurrent().addSessionInitListener(new SessionInitListener() {

                @Override
                public void sessionInit(SessionInitEvent event) throws ServiceException {
            VaadinSession.getCurrent().addBootstrapListener(new BootstrapListener() {

                @Override
                public void modifyBootstrapFragment(
                        BootstrapFragmentResponse response) {
                }

                @Override
                public void modifyBootstrapPage(BootstrapPageResponse response) {
                // Update the bootstrap page
                    Document document = response.getDocument();
                    Element scriptTag = document.getElementsByTag("script").last();
                    String script = scriptTag.html();
                    scriptTag.html("");
                    script = script.replaceAll("\"widgetset\": *\"[^\"]*\"", "\"widgetset\": \"" + wsName + "\"");
                    if(!wsUrl.equals("local")) {
                        script = script.replace("});", ",\"widgetsetUrl\":\"" + wsUrl + "\"});");
                    }
                    if(!wsReady) {
                        script += "\nvar wsname = \"vwscdn7c87ce1831b3238a4be59c5f180e7c99\";\nsetTimeout(function() {if(!window[wsname]) {if (window.confirm('The widgetset cloud still hadn\\'t compiled your widgetset. Depending on usage, this may take 0.5 - n minutes. Would you like to try again?')) { window[wsname] = 'foo'; window.location.reload(false);}}}, 14000);";
                    }
                    scriptTag.appendChild(new DataNode(script, scriptTag.baseUri()));
                }
            });
            inited = true;
                }
            });
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }
}
