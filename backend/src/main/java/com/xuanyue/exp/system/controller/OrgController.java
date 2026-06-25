package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.system.dto.OrgNode;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.service.OrgService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sys/orgs")
public class OrgController {

    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrgNode>> tree() {
        return ApiResponse.success(orgService.tree());
    }

    @GetMapping("/{id}")
    public ApiResponse<SysOrg> get(@PathVariable String id) {
        return ApiResponse.success(orgService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody SysOrg org) {
        orgService.create(org);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody SysOrg org) {
        orgService.update(id, org);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        orgService.delete(id);
        return ApiResponse.success(null);
    }
}
