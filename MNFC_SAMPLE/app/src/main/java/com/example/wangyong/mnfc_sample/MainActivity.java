package com.example.wangyong.mnfc_sample;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.nfc.NdefMessage;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import java.util.Date;

import java.util.List;
import java.util.Locale;


import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;

import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;

import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangyong.mnfc_sample.read.ParsedNdefRecord;

/**
 * @author  chason
 * @emaile 7641436@qq.com
 */
public class MainActivity extends AppCompatActivity {


        private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
        private NfcAdapter mAdapter;
        private PendingIntent mPendingIntent;
        private NdefMessage mNdefPushMessage;
        private TextView promt;
        private AlertDialog mDialog;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            promt = (TextView) findViewById(R.id.promt);
            // 获取默认的NFC控制器
            mAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mAdapter == null) {
                promt.setText("设备不支持NFC！");
                Toast.makeText(this, "设备不支持NFC", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!mAdapter.isEnabled()) {
                Toast.makeText(this, "请在系统设置中先启用NFC功能", Toast.LENGTH_SHORT).show();
                promt.setText("请在系统设置中先启用NFC功能！");
                finish();
                return;
            }

        }

    @Override
    protected void onResume() {
        super.onResume();
        //得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            //处理该intent
            processIntent(getIntent());
        }else{
            Toast.makeText(this, getIntent().getAction(), Toast.LENGTH_SHORT).show();
        }
    }
    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private void processIntent(Intent intent) {
        
        if(intent==null){

            Toast.makeText(this, "intent == null", Toast.LENGTH_SHORT).show();
            return;
        }
        
        //取出封装在intent中的TAG,有可能会取出为null，这个需要跟卡片进行协商
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tagFromIntent.getTechList()) {
            Log.e("chason",tech);
        }
        boolean auth = false;
        //读取TAG
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            Log.e("chason","type"+type+", sectorCount"+sectorCount);
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            promt.setText(metaInfo);
        } catch (Exception e) {
            Toast.makeText(this, "Exception :"+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}