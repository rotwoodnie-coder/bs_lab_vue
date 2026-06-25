package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.MenuListItem;
import com.xuanyue.exp.system.dto.MenuPageQuery;
import com.xuanyue.exp.system.dto.MenuTreeItem;
import com.xuanyue.exp.system.dto.MenuSaveRequest;
import com.xuanyue.exp.system.dto.MenuUpdateRequest;
import com.xuanyue.exp.system.service.MenuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/sys/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<PageResult<MenuListItem>> page(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "status", required = false) String status,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        MenuPageQuery query = new MenuPageQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return ApiResponse.success(menuService.page(query));
    }

    @GetMapping("/visible")
    public ApiResponse<List<MenuTreeItem>> visible(HttpServletRequest request) {
        return ApiResponse.success(menuService.visibleMenus(request.getHeader("X-User-Id")));
    }

    @PostMapping
    public ApiResponse<Void> create(@Validated @RequestBody MenuSaveRequest request) {
        menuService.create(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{menuId}")
    public ApiResponse<Void> update(@PathVariable String menuId, @Validated @RequestBody MenuUpdateRequest request) {
        menuService.update(menuId, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{menuId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String menuId, @RequestParam String status) {
        menuService.updateStatus(menuId, status);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{menuId}")
    public ApiResponse<Void> delete(@PathVariable String menuId) {
        menuService.delete(menuId);
        return ApiResponse.success(null);
    }
}
