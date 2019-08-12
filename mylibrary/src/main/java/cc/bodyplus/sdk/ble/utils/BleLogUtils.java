package cc.bodyplus.sdk.ble.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cc.bodyplus.sdk.ble.wave.EcgWaveFrameData;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class BleLogUtils {

    public static void handleLogData(long stamp, List<EcgWaveFrameData> data, String result){
        String fileName =  BleConstant.BLE_WAVE_PATH ;
        File file = new File(fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        fileName = fileName  +stamp +"_"+ data.size()+"_"+result+ ".txt";
        File dir = new File(fileName);
        if (!dir.exists()) {
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeLogFile(fileName,data);
    }

    private static synchronized void writeLogFile(String fileName, List<EcgWaveFrameData> data) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(fileName, true);
            bw = new BufferedWriter(fw);
            for (EcgWaveFrameData frameData :data){
                bw.write(Arrays.toString(frameData.ecgData));
                bw.newLine();
                bw.flush();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                bw.close();
                fw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
