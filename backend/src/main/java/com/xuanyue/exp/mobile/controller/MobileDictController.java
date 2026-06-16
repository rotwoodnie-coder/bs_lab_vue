package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.data.service.DataDictService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/dict")
public class MobileDictController {

    private final DataDictService dataDictService;

    public MobileDictController(DataDictService dataDictService) {
        this.dataDictService = dataDictService;
    }

    /** 移动端字典：学科、年级等业务字典（data_school_subject 等） */
    @GetMapping("/{type}")
    public ApiResponse<?> list(@PathVariable String type) {
        return ApiResponse.success(dataDictService.list(type));
    }
}
