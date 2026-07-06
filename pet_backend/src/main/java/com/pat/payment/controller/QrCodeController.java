package com.pat.payment.controller;

import com.pat.payment.helper.QrCodeHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "二维码生成")
public class QrCodeController {

    @Operation(summary = "生成二维码图片")
    @GetMapping("/qrcode")
    public ResponseEntity<byte[]> qrcode(@RequestParam("url") String url) {
        byte[] png = QrCodeHelper.generatePngBytes(url, 300);
        if (png.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
