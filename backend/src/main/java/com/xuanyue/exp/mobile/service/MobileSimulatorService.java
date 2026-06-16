package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.exp.dto.ExpSimulatorListItem;
import com.xuanyue.exp.exp.dto.ExpSimulatorPageQuery;
import com.xuanyue.exp.exp.service.ExpSimulatorService;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 移动端模拟实验：复用管理端分页/查询，媒体 URL 在 mobile 层 enrich。
 */
@Service
public class MobileSimulatorService {

    private final ExpSimulatorService expSimulatorService;
    private final MinioStorageService minioStorageService;

    public MobileSimulatorService(ExpSimulatorService expSimulatorService,
                                  MinioStorageService minioStorageService) {
        this.expSimulatorService = expSimulatorService;
        this.minioStorageService = minioStorageService;
    }

    public PageResult<ExpSimulatorListItem> page(ExpSimulatorPageQuery query) {
        PageResult<ExpSimulatorListItem> page = expSimulatorService.page(query);
        if (page.getRecords() != null) {
            for (ExpSimulatorListItem item : page.getRecords()) {
                enrichItem(item);
            }
        }
        return page;
    }

    public ExpSimulatorListItem get(String simulatorId) {
        ExpSimulatorListItem item = expSimulatorService.get(simulatorId);
        enrichItem(item);
        return item;
    }

    private void enrichItem(ExpSimulatorListItem item) {
        if (item == null) {
            return;
        }
        if (StringUtils.hasText(item.getCoverImageUrl())) {
            item.setCoverImagePreviewUrl(MobileMediaUrlSupport.resolve(minioStorageService, item.getCoverImageUrl()));
        }
        if (StringUtils.hasText(item.getSimulatorUrl())) {
            item.setSimulatorPreviewUrl(MobileMediaUrlSupport.resolve(minioStorageService, item.getSimulatorUrl()));
        }
    }
}
