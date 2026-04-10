package com.likelion.animalface.infra.ai;

import com.likelion.animalface.global.config.FeignConfig;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeReq;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-client", url = "${ai.server.url}", configuration = FeignConfig.class)
public interface AiFeignClient {

    @PostMapping("/analyze")
    AiAnalyzeRes analyze(@RequestBody AiAnalyzeReq req);
}