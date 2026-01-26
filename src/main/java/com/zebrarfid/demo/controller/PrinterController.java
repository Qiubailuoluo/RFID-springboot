package com.zebrarfid.demo.controller;

import com.zebrarfid.demo.dto.printconnect.*;
import com.zebrarfid.demo.dto.printconnect.vo.SavedConfigVO;
import com.zebrarfid.demo.result.Result;
import com.zebrarfid.demo.service.login.LoginService;
import com.zebrarfid.demo.service.printconnect.PrinterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 打印机管理控制器（接口与前端文档完全一致）
 */
@RestController
@RequestMapping("/api/printer")
@RequiredArgsConstructor
public class PrinterController {

    private final PrinterService printerService;
    private final LoginService loginService;

    /**
     * 1. 获取（搜索）打印机列表
     */
    @PostMapping("/list")
    public Result<?> getPrinterList(@Valid @RequestBody PrinterListRequest request) {
        return printerService.getPrinterList(request);
    }

    /**
     * 2. 测试打印机连接
     */
    @PostMapping("/test")
    public Result<?> testPrinterConnection(@Valid @RequestBody PrinterTestRequest request) {
        return printerService.testPrinterConnection(request);
    }

    /**
     * 3. 发送测试打印任务
     */
    @PostMapping("/print/test")
    public Result<?> sendTestPrint(@Valid @RequestBody PrintTestRequest request) {
        return printerService.sendTestPrint(request);
    }

    /**
     * 4. 保存打印机配置（POST）
     */
    @PostMapping("/config")
    public Result<?> savePrinterConfig(
            @Valid @RequestBody ConfigSaveRequest request,
            Authentication authentication // Spring Security自动注入当前登录用户信息
    ) {
        // 从JWT中获取当前登录用户的用户名，进而获取用户ID
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        // （假设User实体有username字段，通过username查询userId）
        // 此处需补充：根据username查询User实体，获取userId（实际项目中可缓存或存在上下文）
        Long userId = getUserIdByUsername(username); // 需自行实现（调用UserMapper查询）
        return printerService.savePrinterConfig(request, userId);
    }

    /**
     * 5. 加载打印机配置（GET）
     */
    @GetMapping("/config")
    public Result<List<SavedConfigVO>> loadPrinterConfigs(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        Long userId = getUserIdByUsername(username);
        return printerService.loadPrinterConfigs(userId);
    }

    // 辅助方法
    // 根据用户名查询用户ID
    private Long getUserIdByUsername(String username) {

        return loginService.getUserIdByUsername(username);

    }
}
