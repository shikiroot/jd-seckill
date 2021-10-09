package com.jd.seckill;

import com.alibaba.fastjson.JSONObject;
import com.sun.webkit.network.CookieManager;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Start {

    final static String headerAgent = "User-Agent";
    // Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36
    // Mozilla/5.0 (iPhone; CPU iPhone OS 15_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1
    final static String headerAgentArg = "JD4iPhone/10.1.6 CFNetwork/1312 Darwin/21.0.0";
    final static String Referer = "Referer";
    final static String RefererArg = "https://passport.jd.com/new/login.aspx";
    // 单手柄 100019378198 双手柄 100021367452 茅台 100012043978
    static String pid = new Scanner(System.in).nextLine();
    static String eid = "X";
    static String fp = "X";
    volatile static Integer ok = new Scanner(System.in).nextInt();
    static CookieManager manager = new CookieManager();

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ParseException {
        CookieHandler.setDefault(manager);
        Login.login();
        judgePruchase();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 5; i++) {
            threadPoolExecutor.execute(new RushToPurchase());
        }
        new RushToPurchase().run();
    }

    public static void judgePruchase() throws IOException, ParseException, InterruptedException {
        JSONObject headers = new JSONObject();
        headers.put(Start.headerAgent, Start.headerAgentArg);
        headers.put(Start.Referer, Start.RefererArg);
        String str = HttpUrlConnectionUtil.get(headers, "https://item-soa.jd.com/getWareBusiness?skuId=" + pid);
        JSONObject shopDetail = JSONObject.parseObject(str);
        if (shopDetail.get("yuyueInfo") != null) {
            String buyDate = JSONObject.parseObject(shopDetail.get("yuyueInfo").toString()).get("buyTime").toString();
            String startDate = buyDate.split("-202")[0] + ":00";
            System.out.println("抢购时间为：" + startDate);
            Long startTime = HttpUrlConnectionUtil.dateToTime(startDate);
            while (true) {
                JSONObject jdTime = JSONObject.parseObject(HttpUrlConnectionUtil.get(headers, "https://api.m.jd.com/client.action?functionId=queryMaterialProducts&client=wh5"));
                Long serverTime = Long.valueOf(jdTime.get("currentTime2").toString());
                if (startTime >= serverTime) {
                    System.out.println("正在等待抢购时间");
                    Thread.sleep(300);
                } else {
                    break;
                }
            }
        }
    }

}
