package com.pat.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 中国坐标系统转换工具
 * WGS-84 (GPS原始) ↔ GCJ-02 (火星坐标系, 微信/高德)
 * GCJ-02 ↔ BD-09 (百度坐标系)
 *
 * 参考: https://on4wp7.codeplex.com/SourceControl/changeset/view/21483#353936
 */
public class CoordTransformer {

    private static final double PI = Math.PI;
    private static final double A = 6378245.0;          // 长半轴
    private static final double EE = 0.00669342162296594323; // 扁率

    /**
     * WGS-84 → GCJ-02
     */
    public static double[] wgs84ToGcj02(double wgsLat, double wgsLng) {
        if (outOfChina(wgsLat, wgsLng)) {
            return new double[]{wgsLat, wgsLng};
        }
        double[] d = delta(wgsLat, wgsLng);
        return new double[]{wgsLat + d[0], wgsLng + d[1]};
    }

    /**
     * GCJ-02 → WGS-84 (迭代法, 精度约0.5m)
     */
    public static double[] gcj02ToWgs84(double gcjLat, double gcjLng) {
        if (outOfChina(gcjLat, gcjLng)) {
            return new double[]{gcjLat, gcjLng};
        }
        double[] d = delta(gcjLat, gcjLng);
        return new double[]{gcjLat - d[0], gcjLng - d[1]};
    }

    /**
     * GCJ-02 → BD-09
     */
    public static double[] gcj02ToBd09(double gcjLat, double gcjLng) {
        double x = gcjLng;
        double y = gcjLat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI);
        return new double[]{
            z * Math.sin(theta) + 0.006,
            z * Math.cos(theta) + 0.0065
        };
    }

    /**
     * BD-09 → GCJ-02
     */
    public static double[] bd09ToGcj02(double bdLat, double bdLng) {
        double x = bdLng - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI);
        return new double[]{
            z * Math.sin(theta),
            z * Math.cos(theta)
        };
    }

    /**
     * BigDecimal 版本: GCJ-02 → WGS-84, 返回 BigDecimal
     */
    public static BigDecimal[] gcj02ToWgs84(BigDecimal lat, BigDecimal lng) {
        double[] result = gcj02ToWgs84(lat.doubleValue(), lng.doubleValue());
        return new BigDecimal[]{
            BigDecimal.valueOf(result[0]).setScale(6, RoundingMode.HALF_UP),
            BigDecimal.valueOf(result[1]).setScale(6, RoundingMode.HALF_UP)
        };
    }

    /**
     * BigDecimal 版本: WGS-84 → GCJ-02
     */
    public static BigDecimal[] wgs84ToGcj02(BigDecimal lat, BigDecimal lng) {
        double[] result = wgs84ToGcj02(lat.doubleValue(), lng.doubleValue());
        return new BigDecimal[]{
            BigDecimal.valueOf(result[0]).setScale(6, RoundingMode.HALF_UP),
            BigDecimal.valueOf(result[1]).setScale(6, RoundingMode.HALF_UP)
        };
    }

    private static boolean outOfChina(double lat, double lng) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    private static double[] delta(double lat, double lng) {
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        return new double[]{dLat, dLng};
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320.0 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
