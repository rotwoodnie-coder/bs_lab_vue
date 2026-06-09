package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.system.service.DataDictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/dict")
public class MobileDictController {

    private final DataDictionaryService dataDictionaryService;

    public MobileDictController(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    @GetMapping("/{type}")
    public ApiResponse<?> list(@PathVariable String type) {
        return ApiResponse.success(dataDictionaryService.list(type));
    }
}
