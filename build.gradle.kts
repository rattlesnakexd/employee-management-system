plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.postgresql:postgresql:42.2.23")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("org.apache.oltu.oauth2:org.apache.oltu.oauth2.client:1.0.2")
}

tasks.test {
    useJUnitPlatform()
}