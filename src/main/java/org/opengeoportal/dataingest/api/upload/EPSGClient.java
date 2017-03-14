/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.api.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengeoportal.dataingest.exception.EPSGClientException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * Created by joana on 14/03/17.
 */
public class EPSGClient {


    /**
     * Gets the EPS gfrom WKT.
     *
     * @param srt the srt
     * @return the EPS gfrom WKT
     * @throws EPSGClientException the EPSG client exception
     */
    public String getEPSGfromWKT(String srt) throws EPSGClientException {

        try {

            String url = "http://prj2epsg.org/search.json?mode=wkt&terms=" + URLEncoder.encode(srt, "UTF-8");

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            if (responseCode != 200) throw new EPSGClientException("return code: " + responseCode + ", from " + url);

            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            if (rootNode.isNull()) throw new EPSGClientException("Could not parse epsgclient response");
            JsonNode exactNode = rootNode.path("exact");
            if (exactNode.isNull()) throw new EPSGClientException("Could not parse 'exact' in epsgclient response");

            if (!exactNode.asBoolean()) return null;

            // A code was found; let's retrieve it
            JsonNode codeNode = rootNode.path("codes").findPath("code");
            if (codeNode.isNull()) throw new EPSGClientException("Could not parse 'code' in epsgclient response");

            return codeNode.toString();

        } catch (MalformedURLException ex) {
            throw new EPSGClientException(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new EPSGClientException(ex.getMessage());
        } catch (java.io.IOException ex) {
            throw new EPSGClientException(ex.getMessage());
        }

    }

}
