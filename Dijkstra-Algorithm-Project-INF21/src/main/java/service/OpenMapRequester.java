package service;

import model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
/**
 * This class provides an API-Call of the free OpenMapApi that returns a reference value of the effectivness of the Dijsktra Algorithm.
 * @author i21015
 */
public class OpenMapRequester {
    public static void main(String[] args) throws IOException {
        Vertex v = new Vertex(0, 49.41461, 8.681495 );
        Vertex b = new Vertex(1,49.420318, 8.687872);
        OpenMapRequester o = new OpenMapRequester();

        System.out.println(o.getDistance(v,b)+ "\n");
        System.out.println(o.getDuration(v,b) + "\n");
        System.out.println(o.getCompleteReturn(v,b)+ "\n");

    }
    /**
     * Returns the duration of the path in seconds.
     * @param start start of the starting coordinates in pattern: east coordinates, North/South coordinates.
     * @param end end of the ending coordinates in Pattern: East/West Coordinates, North/South Coordinates.
     * @return a double with the duration in seconds.
     */
    public double getDuration(Vertex start, Vertex end) throws IOException {
        String result = getCompleteReturn(start, end);

        String[] resultsplit = result.split("\"summary\":");
        String[] returnsplit = resultsplit[1].split("},\"way_points\"");
        String[] returnsplited = returnsplit[0].split(",\"duration\":");
        return Double.parseDouble(returnsplited[1]);
    }

    /**
     * Returns the distance of the path in seconds.
     * @param start start of the starting coordinates in pattern: east coordinates, North/South coordinates.
     * @param end end of the ending coordinates in Pattern: East/West Coordinates, North/South Coordinates.
     * @return a double with the distance in meters.
     */
    public double getDistance(Vertex start, Vertex end) throws IOException {
        String result = getCompleteReturn(start, end);

        String[] resultsplit = result.split("\"summary\":");
        String[] returnsplit = resultsplit[1].split(",");
        String[] returner =  returnsplit[0].split(":");
        return Double.parseDouble(returner[1]);
    }

    /**
     * Returns the complete request.
     * @param start start of the starting coordinates in pattern: east coordinates, North/South coordinates.
     * @param end end of the ending coordinates in Pattern: East/West Coordinates, North/South Coordinates.
     * @return a double with the distance in meters.
     */
    public String getCompleteReturn(Vertex start, Vertex end) throws IOException {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248595d0baf4b204b90ad00de85ee212c7f&start=" + start.getLon() + "," + start.getLat() + "&end=" + end.getLon() + "," + end.getLat();
        URL apicall = new URL(url);
        URLConnection connection = apicall.openConnection();

        InputStreamReader stream = new InputStreamReader(connection.getInputStream());
        BufferedReader in = new BufferedReader(stream);

        String inputLine;
        String returner = "";

        while ((inputLine = in.readLine()) != null){
            returner += inputLine;
        }


        in.close();
        return returner;
    }

}
