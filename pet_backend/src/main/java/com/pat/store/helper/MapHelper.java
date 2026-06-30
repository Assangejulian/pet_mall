package com.pat.store.helper;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * 高德地图地理编码 Helper — 地址转 GCJ-02 坐标
 *
 * <p>文档：https://lbs.amap.com/api/webservice/guide/api/georegeo</p>
 */
@Slf4j
@Component
public class MapHelper {

    private static final String GEO_URL =
            "https://restapi.amap.com/v3/geocode/geo?key={key}&address={address}&city={city}&output=JSON";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public MapHelper(RestTemplate restTemplate,
                     @Value("\u0024{amap.api-key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    /**
     * 将地址解析为 GCJ-02 坐标
     *
     * @param province 省
     * @param city     市
     * @param district 区
     * @param address  详细地址
     * @return [longitude, latitude]，解析失败返回 null
     */
    public BigDecimal[] geocode(String province, String city, String district, String address) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("高德 API Key 未配置，跳过地理编码");
            return null;
        }

        String fullAddress = buildAddress(province, city, district, address);
        String cityName = city != null ? city : "";

        try {
            String resp = restTemplate.getForObject(GEO_URL, String.class, apiKey, fullAddress, cityName);
            if (resp == null) {
                log.warn("地理编码响应为空，address={}", fullAddress);
                return null;
            }

            JSONObject json = JSONUtil.parseObj(resp);
            String status = json.getStr("status");
            if (!"1".equals(status)) {
                log.warn("地理编码失败，status={}, info={}, address={}", status, json.getStr("info"), fullAddress);
                return null;
            }

            JSONArray geocodes = json.getJSONArray("geocodes");
            if (geocodes == null || geocodes.isEmpty()) {
                log.warn("地理编码无结果，address={}", fullAddress);
                return null;
            }

            String location = geocodes.getJSONObject(0).getStr("location"); // "lng,lat"
            if (location == null || !location.contains(",")) {
                log.warn("地理编码返回格式异常，location={}, address={}", location, fullAddress);
                return null;
            }

            String[] parts = location.split(",");
            BigDecimal lng = new BigDecimal(parts[0].trim());
            BigDecimal lat = new BigDecimal(parts[1].trim());
            log.info("地理编码成功: {} → {},{}", fullAddress, lng, lat);
            return new BigDecimal[]{lng, lat};
        } catch (Exception e) {
            log.error("地理编码请求异常，address={}", fullAddress, e);
            return null;
        }
    }

    /** 拼接完整地址 */
    private String buildAddress(String province, String city, String district, String address) {
        StringBuilder sb = new StringBuilder();
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (district != null) sb.append(district);
        if (address != null) sb.append(address);
        return sb.toString();
    }
}