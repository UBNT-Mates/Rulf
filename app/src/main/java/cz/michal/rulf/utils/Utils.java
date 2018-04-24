package cz.michal.rulf.utils;

import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.michal.rulf.Constants;
import cz.michal.rulf.model.Server;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static double getLatency(String ipAddress) {
        String pingCommand = Constants.PING_COMMAND + ipAddress;
        String inputLine = "";
        double avgRtt = 0;

        try {
            Process process = Runtime.getRuntime().exec(pingCommand);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                if (inputLine.length() > 0 && inputLine.contains(Constants.PING_AVERAGE_VALUE)) {
                    break;
                }
                inputLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            Log.v(TAG, "getLatency: EXCEPTION");
            e.printStackTrace();
        }

        String afterEqual = inputLine.substring(inputLine.indexOf("="), inputLine.length()).trim();
        String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1, afterEqual.length()).trim();
        String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
        avgRtt = Double.valueOf(strAvgRtt);

        return avgRtt;
    }

    public static List<Server> getServerDistance(Location userLocation, List<Server> serverList) {
        for (int i = 0; i < serverList.size(); i++) {
            Server currentServer = serverList.get(i);

            Location serverLocation = new Location("");
            serverLocation.setLatitude(currentServer.getLatitude());
            serverLocation.setLongitude(currentServer.getLongitude());

            currentServer.setDistance(userLocation.distanceTo(serverLocation));
        }

        return sortServersByDistance(serverList);
    }

    private static List<Server> sortServersByDistance(List<Server> serverList) {
        Collections.sort(serverList, new Comparator<Server>() {
            @Override
            public int compare(Server s1, Server s2) {
                if (s1.getDistance() > s2.getDistance()) {
                    return 1;
                }
                if (s1.getDistance() < s2.getDistance()) {
                    return -1;
                }
                return 0;
            }
        });

        return serverList;
    }

    public static Server getServerLatency(List<Server> serverList) {
        List<Server> finalServerList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Server currentServer = serverList.get(i);

            currentServer.setLatency(getLatency(currentServer.getUrl().substring(7)));
            finalServerList.add(currentServer);
        }

        sortServersByLatency(finalServerList);

        return finalServerList.get(0);
    }

    private static List<Server> sortServersByLatency(List<Server> serverList) {
        Collections.sort(serverList, new Comparator<Server>() {
            @Override
            public int compare(Server s1, Server s2) {
                Log.d(TAG, s1.getLatency() + " " + s2.getLatency());
                if (s1.getLatency() > s2.getLatency()) {
                    return 1;
                }
                if (s1.getLatency() < s2.getLatency()) {
                    return -1;
                }
                return 0;
            }
        });

        return serverList;
    }
}
