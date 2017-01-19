package org.var.play.guestbook.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import org.var.play.guestbook.model.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Portlet implementation class GuestBookPortlet
 */
public class GuestBookPortlet extends MVCPortlet {

    private static final Log LOG = LogFactoryUtil.getLog(GuestBookPortlet.class);

    @Override
    public void init() {
        viewTemplate = getInitParameter("view-jsp");
    }

    @Override
    public void doView(
            RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        include(viewTemplate, renderRequest, renderResponse);
    }

    @Override
    protected void include(
            String path, RenderRequest renderRequest,
            RenderResponse renderResponse)
            throws IOException, PortletException {
        super.init();
        PortletRequestDispatcher portletRequestDispatcher =
                getPortletContext().getRequestDispatcher(path);

        if (portletRequestDispatcher == null) {
            LOG.error(path + " is not a valid include");
        } else {
            portletRequestDispatcher.include(renderRequest, renderResponse);
        }
    }

    public void addEntry(ActionRequest request, ActionResponse response) {
        try {
            PortletPreferences prefs = request.getPreferences();

            String[] guestbookEntries =
                    prefs.getValues("guestbook-entries", new String[0]);
            ArrayList<String> entries = new ArrayList<>(Arrays.asList(guestbookEntries));

            String userName = ParamUtil.getString(request, "name");
            String message = ParamUtil.getString(request, "message");
            String entry = userName + "^" + message;

            entries.add(entry);
            String[] array = entries.toArray(new String[entries.size()]);
            prefs.setValues("guestbook-entries", array);

            prefs.store();

        } catch (ReadOnlyException | IOException | ValidatorException ex) {
            Logger.getLogger(GuestBookPortlet.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    @Override
    public void render(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {
        PortletPreferences prefs = renderRequest.getPreferences();
        String[] guestbookEntries = prefs.getValues("guestbook-entries",
                new String[1]);

        if (guestbookEntries != null) {
            List<Entry> entries = parseEntries(guestbookEntries);
            renderRequest.setAttribute("entries", entries);
        }

        super.render(renderRequest, renderResponse);
    }

    private List<Entry> parseEntries(String[] guestBookEntries) {

        ArrayList<Entry> entries = new ArrayList<>();

        for (String entry : guestBookEntries) {
            String[] parts = entry.split("\\^", 2);
            Entry gbEntry = new Entry(parts[0], parts[1]);
            entries.add(gbEntry);
        }

        return entries;
    }

}
