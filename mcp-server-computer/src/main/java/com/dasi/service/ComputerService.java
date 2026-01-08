package com.dasi.service;

import com.dasi.dto.ComputerFunctionRequest;
import com.dasi.dto.ComputerFunctionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class ComputerService {

    @Tool(name = "computer_config", description = "获取当前机器的系统与运行环境信息")
    public ComputerFunctionResponse queryConfig(ComputerFunctionRequest request) {
        Properties p = System.getProperties();

        String computerName = request != null ? request.getComputer() : "unknown";
        String osName = p.getProperty("os.name", "");
        String osVersion = p.getProperty("os.version", "");
        String osArch = p.getProperty("os.arch", "");
        String userName = p.getProperty("user.name", "");
        String userHome = p.getProperty("user.home", "");
        String userDir = p.getProperty("user.dir", "");
        String javaVersion = p.getProperty("java.version", "");

        ComputerFunctionResponse resp = new ComputerFunctionResponse();
        resp.setComputerName(computerName);
        resp.setOsName(osName);
        resp.setOsVersion(osVersion);
        resp.setOsArch(osArch);
        resp.setUserName(userName);
        resp.setUserHome(userHome);
        resp.setUserDir(userDir);
        resp.setJavaVersion(javaVersion);

        return resp;
    }
}
