package com.zebrarfid.demo.service.printconnect;

import com.zebrarfid.demo.dto.*;
import com.zebrarfid.demo.dto.vo.*;
import com.zebrarfid.demo.result.Result;
import java.util.List;

public interface PrinterService {
    // 1. 获取（搜索）打印机列表
    Result<List<PrinterVO>> getPrinterList(PrinterListRequest request);

    // 2. 测试打印机连接
    Result<TestConnectResponse> testPrinterConnection(PrinterTestRequest request);

    // 3. 发送测试打印任务
    Result<PrintTestResponse> sendTestPrint(PrintTestRequest request);

    // 4. 保存打印机配置（关联当前用户）
    Result<?> savePrinterConfig(ConfigSaveRequest request, Long userId);

    // 5. 加载当前用户保存的配置
    Result<List<SavedConfigVO>> loadPrinterConfigs(Long userId);
}
