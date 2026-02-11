plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.vaadin") version "24.3.5"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.vaadin:vaadin-spring-boot-starter:24.3.5")
    // implementation("com.vaadin:vaadin-charts-flow:24.3.5")  // 商业组件，需要许可证
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.github.mvysny.karibudsl:karibu-dsl:2.1.2")
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // MyBatis-Plus
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.5")
    
    // MySQL
    implementation("com.mysql:mysql-connector-j:8.3.0")
    
    // 数据库连接池
    implementation("com.zaxxer:HikariCP")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

vaadin {
    productionMode = false
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
