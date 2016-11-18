/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.tasks;

/**
 *
 * @author JamesFraser
 */
import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.ImageUtils;
import com.groupon.seleniumgridextras.utilities.ScreenshotUtility;
import com.groupon.seleniumgridextras.utilities.TimeStampUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.videorecording.ImageProcessor;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.apache.log4j.Logger;

public class LiveFeed extends ExecuteNonJsonTask {
    
    private static Logger logger = Logger.getLogger(LiveFeed.class);
    private final int delay = 5000;
    private boolean refresh = true;
    
    public LiveFeed() {
        setEndpoint(TaskDescriptions.Endpoints.LIVE_FEED);
        setDescription(TaskDescriptions.Description.LIVE_FEED);
        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.Images.WIDTH, "Desired width of the feed. Will be autoscaled with height");
        params.addProperty(JsonCodec.Images.HEIGHT, "Desired height of the feed. Will be autoscaled with width");
        params.addProperty("refresh", "Indicates whether to automatically refresh the page after a delay");
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        setButtonText("Live Feed");
        setEnabledInGui(true);
        
    }
    
    @Override
    public String execute() {
        
        StringBuilder pageHtml = new StringBuilder();
        pageHtml.append("<html><head></head><body><div>");
        
        pageHtml.append(getScreenshot());
        
        pageHtml.append("</div>");
        pageHtml.append(includeRefresh());
        pageHtml.append("</body></html>");
        
        return pageHtml.toString();
    }
    
    @Override
    public String execute(Map<String, String> parameter) {
        
        int width = parameter.containsKey(JsonCodec.Images.WIDTH) ? Integer.parseInt(parameter.get(
                JsonCodec.Images.WIDTH)) : 0;
        int height = parameter.containsKey(JsonCodec.Images.HEIGHT) ? Integer.parseInt(parameter.get(
                JsonCodec.Images.HEIGHT)) : 0;
        
        refresh = parameter.containsKey("refresh") ? (!parameter.get("refresh").toLowerCase().equals("false")) : true;
        
        if (width > 0 || height > 0) {
            
            StringBuilder pageHtml = new StringBuilder();
            pageHtml.append("<html><head></head><body><div>");
            
            pageHtml.append(getScreenshot(width, height));
            
            pageHtml.append("</div>");
            pageHtml.append(includeRefresh());
            pageHtml.append("</body></html>");
            
            return pageHtml.toString();            
        } else {
            return execute();
        }
        
    }
    
    protected String getScreenshot() {
        String returnString = "<img src='data:image/png;base64,";
        
        try {
            returnString = returnString + ScreenshotUtility
                    .getFullScreenshotAsBase64String();
        } catch (AWTException e) {
            logger.debug(e);
            e.printStackTrace();
            
            BufferedImage errorImage
                    = ImageProcessor
                    .createTitleFrame(new Dimension(800, 600), BufferedImage.TYPE_3BYTE_BGR,
                            "Cannot create screenshot for this node. Is it in Headless mode?",
                            e.getMessage(),
                            TimeStampUtility.getTimestampAsString());
            
            returnString = returnString + ImageUtils.encodeToString(errorImage, "PNG");
        }
        
        return returnString + "'>";
    }
    
    protected String getScreenshot(int width, int height) {
        String returnString = "<img src='data:image/png;base64,";
        
        try {
            returnString = returnString + ScreenshotUtility
                    .getResizedScreenshotAsBase64String(width, height);
        } catch (AWTException e) {
            logger.debug(e);
            e.printStackTrace();
            
            BufferedImage errorImage
                    = ImageProcessor
                    .createTitleFrame(new Dimension(width, height), BufferedImage.TYPE_3BYTE_BGR,
                            "Cannot create screenshot for this node. Is it in Headless mode?",
                            e.getMessage(),
                            TimeStampUtility.getTimestampAsString());
            
            returnString = returnString + ImageUtils.encodeToString(errorImage, "PNG");
        }
        
        return returnString + "'>";
    }
    
    protected String includeRefresh() {
        if(!refresh) return "";
        StringBuilder refreshScript = new StringBuilder();
        refreshScript.append("<script>");
        refreshScript.append("function refresh() {");
        refreshScript.append("window.location.reload(true);");
        refreshScript.append("}");
        refreshScript.append("setTimeout(refresh, " + delay + ");");
        refreshScript.append("</script>");
        
        return refreshScript.toString();
    }
}
