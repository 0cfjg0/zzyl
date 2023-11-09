package com.zzyl.config;

import com.zzyl.intercept.AutoFillInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName WebMvcConfig.java
 * @Description webMvc高级配置
 */
@Configuration
public class MybatisConfig {

    /***
     * @description 自动填充拦截器
     *
     * @return: com.zzyl.intercept.AutoFillInterceptor
     */
    @Bean
    public AutoFillInterceptor autoFillInterceptor(){
        return new AutoFillInterceptor();
    }

}
