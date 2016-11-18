/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.utilities;

import com.groupon.seleniumgridextras.tasks.RebootAll;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.apache.log4j.Logger;
/**
 *
 * @author JamesFraser
 */
public class ThisHubInfo {

    public static List<String> getAllConnectedNodeIPs() {
        ArrayList<String> nodes = new ArrayList<String>();
        try {
            JSONObject json = new JSONObject(HttpUtility.getRequestAsString(new URI("http://127.0.0.1:4444/grid/admin/ActiveNodeServlet")));
            org.json.JSONArray nodesList = json.getJSONArray("nodes");

            for (int i = 0; i < nodesList.length(); i++) {
                nodes.add(nodesList.getJSONObject(i).getString("host"));
            }

        } catch (Exception e) {
            Logger.getLogger(RebootAll.class.getName()).error(e);
        }
        return nodes;
    }
}
