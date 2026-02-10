package com.example.demo.config

import com.baomidou.mybatisplus.annotation.DbType
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.apache.ibatis.reflection.MetaObject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Configuration
class MybatisPlusConfig {
    
    @Bean
    fun mybatisPlusInterceptor(): MybatisPlusInterceptor {
        val interceptor = MybatisPlusInterceptor()
        interceptor.addInnerInterceptor(PaginationInnerInterceptor(DbType.MYSQL))
        return interceptor
    }
}

@Component
class MyMetaObjectHandler : MetaObjectHandler {
    
    override fun insertFill(metaObject: MetaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::class.java, LocalDateTime.now())
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::class.java, LocalDateTime.now())
    }
    
    override fun updateFill(metaObject: MetaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::class.java, LocalDateTime.now())
    }
}
