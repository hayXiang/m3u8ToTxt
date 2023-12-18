package com.iaskme;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class App 
{
    public static void main( String[] args ) {

        class ChannelInfo {
            public String name;
            public String url;
        }

        if (args.length < 2){
            System.err.println("java -java m3u8Totxt.java");
            return;
        }


        File m3u8File = new File(args[0]);
        if (!m3u8File.exists()){
            System.err.println("m3u8 file is not exist");
            return;
        }

        Map<String, List<ChannelInfo>> channelGroups = new LinkedHashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(m3u8File))){
            int count = 0;
            String line = null;
            String currentGroupName = "默认";
            while ((line =bufferedReader.readLine()) != null){
                if (line.isEmpty())
                    continue;

                if (count++ == 0) {
                    if (line.startsWith("#EXTM3U"))
                        continue;

                    if (count == 0 && !line.startsWith("#EXTM3U")) {
                        System.err.println("invalid m3u8 file");
                        return;
                    }
                }

                if (line.startsWith("#EXTINF:")){
                    try {
                        String[] channelInfos = line.split(",");
                        String channelName = channelInfos[channelInfos.length - 1];
                        String channelOtherInfo = channelInfos[channelInfos.length - 2];
                        if (channelOtherInfo.contains("group-title=")) {
                            currentGroupName = channelOtherInfo.split("group-title=")[1].replaceAll("\"", "");
                        }

                        if (!channelGroups.containsKey(currentGroupName)) {
                            channelGroups.put(currentGroupName, new LinkedList<ChannelInfo>());
                        }
                        ChannelInfo channelInfo = new ChannelInfo();
                        channelInfo.name = channelName;
                        channelGroups.get(currentGroupName).add(channelInfo);
                    } catch (Exception e){
                        System.err.println(e);
                    }
                }else if (!line.startsWith("#")){
                    List<ChannelInfo> channelInfos = channelGroups.get(currentGroupName);
                    try {
                        channelInfos.get(channelInfos.size() - 1).url = line.trim();
                    }catch (Exception e){
                        System.err.println(e);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        try(BufferedWriter buffWriter = new BufferedWriter(new FileWriter(args[1]))) {
            int groupCount = 0;
            for (String channelGroupName : channelGroups.keySet()) {
                groupCount++;
                if (channelGroupName.contains("售后"))
                    continue;

                buffWriter.write("\uD83D\uDCE1" + channelGroupName + ",#genre#");
                buffWriter.newLine();
                List<ChannelInfo> channelInfos = channelGroups.get(channelGroupName);
                for (int i = 0; i < channelInfos.size(); i++) {
                    ChannelInfo channelInfo = channelInfos.get(i);
                    buffWriter.write(channelInfo.name + "," + channelInfo.url);
                    if (i != channelInfos.size() -1)
                        buffWriter.newLine();
                }

                if (groupCount != channelGroups.size()) {
                    buffWriter.newLine();
                    buffWriter.newLine();
                    buffWriter.newLine();
                    buffWriter.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
