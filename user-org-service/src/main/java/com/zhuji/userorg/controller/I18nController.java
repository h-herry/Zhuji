package com.zhuji.userorg.controller;

import com.zhuji.common.core.result.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Tag(name = "多语言管理", description = "多语言配置和切换接口")
@RestController
@RequestMapping("/api/v1/i18n")
public class I18nController {

    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();

    static {
        SUPPORTED_LANGUAGES.put("zh_CN", "简体中文");
        SUPPORTED_LANGUAGES.put("en_US", "English");
        SUPPORTED_LANGUAGES.put("zh_TW", "繁體中文");
        SUPPORTED_LANGUAGES.put("ja_JP", "日本語");
        SUPPORTED_LANGUAGES.put("ko_KR", "한국어");
    }

    @Operation(summary = "获取支持的语言列表")
    @GetMapping("/languages")
    public ApiResponse<List<Map<String, String>>> getSupportedLanguages() {
        List<Map<String, String>> languages = SUPPORTED_LANGUAGES.entrySet().stream()
                .map(entry -> {
                    Map<String, String> lang = new HashMap<>();
                    lang.put("code", entry.getKey());
                    lang.put("name", entry.getValue());
                    return lang;
                })
                .toList();
        return ApiResponse.success(languages);
    }

    @Operation(summary = "切换语言")
    @PostMapping("/switch")
    public ApiResponse<Void> switchLanguage(
            @RequestParam String lang,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!SUPPORTED_LANGUAGES.containsKey(lang)) {
            return ApiResponse.error(400, "不支持的语言: " + lang);
        }

        Locale locale = parseLocale(lang);

        Cookie localeCookie = new Cookie("locale", lang);
        localeCookie.setMaxAge(604800);
        localeCookie.setPath("/");
        localeCookie.setHttpOnly(false);
        response.addCookie(localeCookie);

        return ApiResponse.success();
    }

    @Operation(summary = "获取当前语言")
    @GetMapping("/current")
    public ApiResponse<Map<String, String>> getCurrentLanguage(HttpServletRequest request) {
        String currentLang = "zh_CN";

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("locale".equals(cookie.getName())) {
                    currentLang = cookie.getValue();
                    break;
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        result.put("code", currentLang);
        result.put("name", SUPPORTED_LANGUAGES.getOrDefault(currentLang, "简体中文"));

        return ApiResponse.success(result);
    }

    @Operation(summary = "验证语言是否支持")
    @GetMapping("/validate/{lang}")
    public ApiResponse<Boolean> validateLanguage(@PathVariable String lang) {
        return ApiResponse.success(SUPPORTED_LANGUAGES.containsKey(lang));
    }

    private Locale parseLocale(String localeStr) {
        if (localeStr.contains("_")) {
            String[] parts = localeStr.split("_");
            return new Locale(parts[0], parts[1]);
        } else if (localeStr.contains("-")) {
            String[] parts = localeStr.split("-");
            return new Locale(parts[0], parts[1]);
        }
        return new Locale(localeStr);
    }
}