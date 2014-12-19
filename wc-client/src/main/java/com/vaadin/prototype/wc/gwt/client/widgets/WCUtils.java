package com.vaadin.prototype.wc.gwt.client.widgets;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.document;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Predicate;
import com.google.gwt.user.client.EventListener;
import com.vaadin.prototype.wc.gwt.client.html.HTMLElement;

public class WCUtils {
    public static int getAttrIntValue(HTMLElement el, String attr, int def) {
        return (int) getAttrFloatValue(el, attr, def);
    }

    public static double getAttrDoubleValue(HTMLElement el, String attr,
            double def) {
        return getAttrFloatValue(el, attr, (float) def);
    }

    public static String getAttrValue(HTMLElement el, String attr, String def) {
        String val = el.getAttribute(attr);
        return val == null || val.isEmpty() ? def : val;
    }

    public static float getAttrFloatValue(HTMLElement el, String attr, float def) {
        try {
            return Float.valueOf(getAttrValue(el, attr, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void loadVaadinGlobalTheme() {
        // Wait until body is ready.
        $(document).on("load", new Function(){
            public void f() {
               GQuery body = $("body");
               String theme = body.attr("vaadin-theme");
               if (!theme.isEmpty()) {
                   GQuery style = $("#__vaadin-style");
                   if (style.isEmpty()) {
                       style = $("<style id='__vaadin-style' language='text/css'></style>").appendTo($("head"));
                   }
                   loadTheme(body, style, theme);
               }
            }
        });
    }

    public static void loadVaadinTheme(HTMLElement container, HTMLElement el, HTMLElement style, String def) {
        String theme = getAttrValue(el, "theme", def);
        if (theme == null) {
            return;
        }
        if ($(theme).text().contains(theme)) {
            return;
        }
        loadTheme($(container), $(style), theme);
    }

    private static void loadTheme(GQuery container, GQuery style, String theme) {
        // This code is very tricky and tries to figure out where vaadin themes are.
        // GWT moduleBaseUrl does not work if we use link imports.
        GQuery l = $("link[href]").filter(new Predicate(){
            public boolean f(Element e, int index) {
                String h = $(e).attr("href");
                return h.matches("^(|.*/)(x-vaadin|vaadin-x|v-[\\w\\-]+|vaadin-[\\w\\-]+)\\.html");
            }
        });
        String base = GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "");

        if (!l.isEmpty()) {
            base = l.attr("href").replaceFirst("[\\w\\-]+\\.html", "");
        } else if (base.contains("VAADIN/widgetsets")) {
            base += "../../";
        }
        base += "VAADIN/themes/" + theme + "/styles.css";
        container.addClass(theme);
        style.text("@import url('" + base + "')");
    }

    public static native void observe(JavaScriptObject jso, EventListener ev) /*-{
      var fnc = function(changes) {
        ev.@com.google.gwt.user.client.EventListener::onBrowserEvent(*)(null);
      };
      jso.__fnc = fnc;
      Object.observe(jso, fnc);
   }-*/;

    public static native void unobserve(JavaScriptObject jso) /*-{
      if (jso && jso.__fnc) {
        Object.unobserve(jso, jso.__fnc);
        jso.__fnc = undefined;
      }
   }-*/;
}