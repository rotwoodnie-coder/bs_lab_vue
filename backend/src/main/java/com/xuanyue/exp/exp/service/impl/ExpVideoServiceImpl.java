package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.exp.service.ExpVideoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpVideoServiceImpl implements ExpVideoService {

    private final ExpVideoRepository repository;
    private final MinioStorageService minioStorageService;

    public ExpVideoServiceImpl(ExpVideoRepository repository, MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
        this.repository = repository;
    }

    @Override
    public List<?> listByExpId(String expId) {
        List lst = repository.findByExpIdOrderBySortOrderAsc(expId);
        for (Object item : lst) {
            ExpVideo video = (ExpVideo) item;
            video.setPreviewUrl(minioStorageService.buildPreviewUrl(video.getVideoUrl()));
        }
        return lst;
    }

    @Override
    @Transactional
    public void saveBatch(String expId, List<Map<String, Object>> videos) {
        repository.deleteByExpId(expId);
        List<ExpVideo> items = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> video : videos) {
            ExpVideo item = new ExpVideo();
            item.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            item.setExpId(expId);
            item.setVideoUrl(asString(video.get("videoUrl")));
            item.setFileId(asString(video.get("fileId")));
            item.setSortOrder(parseOrder(video.get("sortOrder"), index++));
            items.add(item);
        }
        repository.saveAll(items);
    }

    @Override
    @Transactional
    public void saveOne(String expId, Map<String, Object> video) {
        ExpVideo item = new ExpVideo();
        item.setSeqId(UUID.randomUUID().toString().replace("-", ""));
        item.setExpId(expId);
        item.setVideoUrl(asString(video.get("videoUrl")));
        item.setFileId(asString(video.get("fileId")));
        item.setSortOrder(parseOrder(video.get("sortOrder"), getNextSortOrder(expId)));
        repository.save(item);
    }

    private int getNextSortOrder(String expId) {
        List<ExpVideo> existing = repository.findByExpIdOrderBySortOrderAsc(expId);
        if (existing == null || existing.isEmpty()) {
            return 0;
        }
        Integer max = existing.get(existing.size() - 1).getSortOrder();
        return max == null ? existing.size() : max + 1;
    }

    @Override
    public void delete(String seqId) {
        repository.deleteById(seqId);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer parseOrder(Object value, int defaultValue) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) {
            return defaultValue;
        }
        return Integer.valueOf(text);
    }
}
