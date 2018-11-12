package org.cyberio.tt;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TTController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TTController.class);

    private static final String DIV_USER_STATS_TITLE = "div.userStatsTitle";
    private static final String SPAN_USER_LOCATION = "span.userLocation";
    private static final String DIV_USER_PIC = "div.userPic";
    private static final String IMG = "img";
    private static final String SRC = "src";

    @RequestMapping(value = "/getProfileData", produces="application/json")
    public Map getProfileData(@RequestParam String profileId) {
        return sendGetToResource("https://foursquare.com/", profileId);
    }

    private Map sendGetToResource(String url, String profileId) {
        Map resultMap = new HashMap<>();
        String possibleUrls[] = {url + profileId, url + "user/" + profileId};
        Document doc;
        final int MAX_TRY = 2;
        int tryNumber = 0;
        while (tryNumber < MAX_TRY) {
            LOGGER.info("Trying resource url: " + possibleUrls[tryNumber]);
            try {
                doc = Jsoup.connect(possibleUrls[tryNumber++]).get();
                resultMap.put("userName", doc.select(DIV_USER_STATS_TITLE).first().text());
                resultMap.put("userLocation", doc.select(SPAN_USER_LOCATION).first().text());
                resultMap.put("imageURL", doc.select(DIV_USER_PIC).first().children().select(IMG).attr(SRC));
                resultMap.put("imageBytes", readImageBytesFromUrl(doc.select(DIV_USER_PIC).first().children().select(IMG).attr(SRC)));
                return resultMap;
            } catch (IOException e) {
                LOGGER.error("Invalid resource url", e);
            }
        }

        return Collections.singletonMap("error", "invald user id");
    }

    private byte[] readImageBytesFromUrl(String url) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = new URL(url).openStream();

            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            LOGGER.error("Invalid image url", e);
            return null;
        }

        return outputStream.toByteArray();
    }

}
