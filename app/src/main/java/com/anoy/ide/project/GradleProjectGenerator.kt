package com.anoy.ide.project

import java.io.File

/**
 * Generates a conventional Android project using only files that Android
 * Studio and the Gradle wrapper understand. The output is a standard project:
 *
 * - settings + root build file (Kotlin DSL or Groovy)
 * - `gradle/wrapper/gradle-wrapper.properties`
 * - an `app` (or `library`) module
 * - the selected template's source + resources
 *
 * Forge writes this locally; the project can be opened by Android Studio or
 * built by Forge itself via the project's own wrapper.
 */
object GradleProjectGenerator {

    private const val AGP = "8.7.2"
    private const val KOTLIN = "2.1.20"
    private const val COMPOSE_BOM = "2024.09.02"

    fun generate(root: File, config: ProjectConfig): File {
        require(root.exists() || root.mkdirs()) { "Could not create project dir ${root.absolutePath}" }

        val isLibrary = config.template == ProjectTemplate.LIBRARY_MODULE
        val moduleName = if (isLibrary) "library" else "app"
        val sourceDirName = if (config.language == ProjectLanguage.KOTLIN) "kotlin" else "java"

        writeSettings(root, config, isLibrary)
        writeRootBuild(root, config, isLibrary)
        writeProperties(root)
        writeModuleBuild(root, moduleName, config, isLibrary)

        if (isLibrary) {
            writeLibrarySources(root, moduleName, config, sourceDirName)
        } else {
            writeAppSources(root, moduleName, config, sourceDirName)
        }

        return root
    }

    private fun writeSettings(root: File, config: ProjectConfig, isLibrary: Boolean) {
        val dsl = config.buildConfig
        val moduleName = if (isLibrary) "library" else "app"
        val content = if (dsl == ProjectBuildConfig.KOTLIN_DSL) {
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = "${config.name}"
            include(":$moduleName")
            """.trimIndent()
        } else {
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = "${config.name}"
            include ':$moduleName'
            """.trimIndent()
        }
        write(root, "settings.${if (dsl == ProjectBuildConfig.KOTLIN_DSL) "kts" else "gradle"}", content)
    }

    private fun writeRootBuild(root: File, config: ProjectConfig, isLibrary: Boolean) {
        val dsl = config.buildConfig
        val appPlugin = if (isLibrary) "com.android.library" else "com.android.application"
        val needsKotlin = config.language == ProjectLanguage.KOTLIN
        val needsComposePlugin = needsKotlin && config.template.usesCompose
        val composeLine = if (needsComposePlugin) {
            if (dsl == ProjectBuildConfig.KOTLIN_DSL) {
                "    id(\"org.jetbrains.kotlin.plugin.compose\") version \"$KOTLIN\" apply false\n"
            } else {
                "    id 'org.jetbrains.kotlin.plugin.compose' version '$KOTLIN' apply false\n"
            }
        } else ""

        val content = if (dsl == ProjectBuildConfig.KOTLIN_DSL) {
            """
            plugins {
                id("$appPlugin") version "$AGP" apply false
                ${if (needsKotlin) "id(\"org.jetbrains.kotlin.android\") version \"$KOTLIN\" apply false\n" else ""}$composeLine
            }
            """.trimIndent().trimEnd()
        } else {
            """
            plugins {
                id '$appPlugin' version '$AGP' apply false
                ${if (needsKotlin) "id 'org.jetbrains.kotlin.android' version '$KOTLIN' apply false\n" else ""}$composeLine
            }
            """.trimIndent().trimEnd()
        }
        write(root, "build.${if (dsl == ProjectBuildConfig.KOTLIN_DSL) "kts" else "gradle"}", content)
    }

    private fun writeProperties(root: File) {
        write(
            root, "gradle.properties",
            """
            org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
            android.useAndroidX=true
            android.nonTransitiveRClass=true
            """.trimIndent()
        )
    }

    private fun writeModuleBuild(
        root: File,
        moduleName: String,
        config: ProjectConfig,
        isLibrary: Boolean
    ) {
        val dsl = config.buildConfig
        val plugins = mutableListOf<String>()
        if (isLibrary) plugins.add("com.android.library")
        else plugins.add("com.android.application")
        if (config.language == ProjectLanguage.KOTLIN) plugins.add("org.jetbrains.kotlin.android")
        if (config.language == ProjectLanguage.KOTLIN && config.template.usesCompose) {
            plugins.add("org.jetbrains.kotlin.plugin.compose")
        }

        val content = if (dsl == ProjectBuildConfig.KOTLIN_DSL) {
            val pluginLines = plugins.joinToString("\n") { "    id(\"$it\")" }
            """
            plugins {
            $pluginLines
            }

            android {
                namespace = "${config.packageName}"
                compileSdk = 35

                defaultConfig {
                    ${if (!isLibrary) "applicationId = \"${config.packageName}\"\n" else ""}minSdk = ${config.minSdk}
                    targetSdk = 35
                    versionCode = 1
                    versionName = "1.0"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                ${if (config.language == ProjectLanguage.KOTLIN) "kotlinOptions { jvmTarget = \"17\" }\n" else ""}buildFeatures {
                    ${if (config.template.usesCompose && config.language == ProjectLanguage.KOTLIN) "compose = true\n" else ""}buildConfig = true
                }
            }

            dependencies {
                implementation("androidx.core:core-ktx:1.13.1")
                ${if (!isLibrary) "implementation(\"androidx.appcompat:appcompat:1.7.0\")\n" else ""}${if (config.template.usesCompose && config.language == ProjectLanguage.KOTLIN) """
                    implementation(platform("androidx.compose:compose-bom:$COMPOSE_BOM"))
                    implementation("androidx.compose.ui:ui")
                    implementation("androidx.compose.material3:material3")
                    implementation("androidx.activity:activity-compose:1.9.2")
                """.trimIndent() else ""}
            }
            """.trimIndent().trimEnd()
        } else {
            val pluginLines = plugins.joinToString("\n") { "    id '$it'" }
            """
            plugins {
            $pluginLines
            }

            android {
                namespace '${config.packageName}'
                compileSdk 35

                defaultConfig {
                    ${if (!isLibrary) "applicationId '${config.packageName}'\n" else ""}minSdk ${config.minSdk}
                    targetSdk 35
                    versionCode 1
                    versionName "1.0"
                }

                buildTypes {
                    release {
                        minifyEnabled false
                    }
                }
                compileOptions {
                    sourceCompatibility JavaVersion.VERSION_17
                    targetCompatibility JavaVersion.VERSION_17
                }
                ${if (config.language == ProjectLanguage.KOTLIN) "kotlinOptions { jvmTarget = '17' }\n" else ""}buildFeatures {
                    ${if (config.template.usesCompose && config.language == ProjectLanguage.KOTLIN) "compose true\n" else ""}buildConfig true
                }
            }

            dependencies {
                implementation 'androidx.core:core-ktx:1.13.1'
                ${if (!isLibrary) "implementation 'androidx.appcompat:appcompat:1.7.0'\n" else ""}${if (config.template.usesCompose && config.language == ProjectLanguage.KOTLIN) """
                    implementation platform('androidx.compose:compose-bom:$COMPOSE_BOM')
                    implementation 'androidx.compose.ui:ui'
                    implementation 'androidx.compose.material3:material3'
                    implementation 'androidx.activity:activity-compose:1.9.2'
                """.trimIndent() else ""}
            }
            """.trimIndent().trimEnd()
        }

        write(root, "$moduleName/build.${if (dsl == ProjectBuildConfig.KOTLIN_DSL) "kts" else "gradle"}", content)
    }

    private fun writeAppSources(
        root: File,
        moduleName: String,
        config: ProjectConfig,
        sourceDirName: String
    ) {
        val moduleDir = File(root, moduleName)
        val manifest = buildString {
            append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n")
            append("<application android:label=\"${config.name}\" android:theme=\"@style/Theme.${config.name}\">\n")
            when (config.template) {
                ProjectTemplate.NO_ACTIVITY -> Unit
                ProjectTemplate.LIBRARY_MODULE -> Unit
                else -> {
                    append("<activity android:name=\".MainActivity\" android:exported=\"true\">\n")
                    append("<intent-filter>\n")
                    append("<action android:name=\"android.intent.action.MAIN\" />\n")
                    append("<category android:name=\"android.intent.category.LAUNCHER\" />\n")
                    append("</intent-filter>\n</activity>\n")
                }
            }
            append("</application>\n</manifest>")
        }
        write(moduleDir, "src/main/AndroidManifest.xml", manifest)

        val pkgPath = config.packageName.replace('.', '/')
        val resDir = File(moduleDir, "src/main/res")

        write(
            resDir, "values/strings.xml",
            "<resources>\n    <string name=\"app_name\">${config.name}</string>\n</resources>"
        )
        val parentTheme = if (config.template.usesCompose) "android:Theme.Material.Light.NoActionBar" else "Theme.AppCompat.Light.NoActionBar"
        write(
            resDir, "values/themes.xml",
            """
            <resources>
                <style name="Theme.${config.name}" parent="$parentTheme">
                    <item name="android:statusBarColor">@android:color/transparent</item>
                </style>
            </resources>
            """.trimIndent()
        )

        val sourceDir = File(moduleDir, "src/main/$sourceDirName/$pkgPath")
        sourceDir.mkdirs()

        when (config.template) {
            ProjectTemplate.LIBRARY_MODULE -> Unit
            ProjectTemplate.NO_ACTIVITY -> Unit
            else -> {
                if (config.language == ProjectLanguage.KOTLIN) {
                    writeKotlinActivity(sourceDir, config)
                } else {
                    writeJavaActivity(sourceDir, config)
                }
            }
        }

        if (config.template == ProjectTemplate.EMPTY_VIEWS_ACTIVITY ||
            config.template == ProjectTemplate.BASIC_VIEWS_NAVIGATION
        ) {
            write(resDir, "layout/activity_main.xml", viewsMainLayout(config))
        }
        if (config.template == ProjectTemplate.BASIC_VIEWS_NAVIGATION) {
            write(resDir, "layout/activity_second.xml", viewsSecondLayout())
        }
    }

    private fun writeLibrarySources(
        root: File,
        moduleName: String,
        config: ProjectConfig,
        sourceDirName: String
    ) {
        val moduleDir = File(root, moduleName)
        write(
            moduleDir, "src/main/AndroidManifest.xml",
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"></manifest>"
        )
        val sourceDir = File(moduleDir, "src/main/$sourceDirName/${config.packageName.replace('.', '/')}")
        sourceDir.mkdirs()
        if (config.language == ProjectLanguage.KOTLIN) {
            write(
                sourceDir, "LibraryApi.kt",
                """
                package ${config.packageName}

                object LibraryApi {
                    const val VERSION = "1.0"
                }
                """.trimIndent()
            )
        } else {
            write(
                sourceDir, "LibraryApi.java",
                """
                package ${config.packageName};

                public final class LibraryApi {
                    public static final String VERSION = "1.0";
                    private LibraryApi() {}
                }
                """.trimIndent()
            )
        }
    }

    private fun writeKotlinActivity(sourceDir: File, config: ProjectConfig) {
        val pkg = config.packageName
        when (config.template) {
            ProjectTemplate.EMPTY_COMPOSE_ACTIVITY -> write(
                sourceDir, "MainActivity.kt",
                """
                package $pkg

                import android.os.Bundle
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                import androidx.compose.foundation.layout.fillMaxSize
                import androidx.compose.material3.MaterialTheme
                import androidx.compose.material3.Surface
                import androidx.compose.material3.Text
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.unit.sp

                class MainActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent {
                            MaterialTheme {
                                Surface(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "Hello from Forge",
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
                """.trimIndent()
            )
            ProjectTemplate.EMPTY_VIEWS_ACTIVITY -> write(
                sourceDir, "MainActivity.kt",
                """
                package $pkg

                import android.os.Bundle
                import androidx.appcompat.app.AppCompatActivity

                class MainActivity : AppCompatActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.activity_main)
                    }
                }
                """.trimIndent()
            )
            ProjectTemplate.BASIC_VIEWS_NAVIGATION -> {
                write(
                    sourceDir, "MainActivity.kt",
                    """
                    package $pkg

                    import android.content.Intent
                    import android.os.Bundle
                    import android.widget.Button
                    import androidx.appcompat.app.AppCompatActivity

                    class MainActivity : AppCompatActivity() {
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            setContentView(R.layout.activity_main)
                            findViewById<Button>(R.id.next).setOnClickListener {
                                startActivity(Intent(this, SecondActivity::class.java))
                            }
                        }
                    }
                    """.trimIndent()
                )
                write(
                    sourceDir, "SecondActivity.kt",
                    """
                    package $pkg

                    import android.os.Bundle
                    import androidx.appcompat.app.AppCompatActivity

                    class SecondActivity : AppCompatActivity() {
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            setContentView(R.layout.activity_second)
                        }
                    }
                    """.trimIndent()
                )
            }
            ProjectTemplate.NO_ACTIVITY, ProjectTemplate.LIBRARY_MODULE -> Unit
        }
    }

    private fun writeJavaActivity(sourceDir: File, config: ProjectConfig) {
        val pkg = config.packageName
        when (config.template) {
            ProjectTemplate.EMPTY_VIEWS_ACTIVITY -> write(
                sourceDir, "MainActivity.java",
                """
                package $pkg;

                import android.os.Bundle;
                import androidx.appcompat.app.AppCompatActivity;

                public class MainActivity extends AppCompatActivity {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.activity_main);
                    }
                }
                """.trimIndent()
            )
            ProjectTemplate.BASIC_VIEWS_NAVIGATION -> {
                write(
                    sourceDir, "MainActivity.java",
                    """
                    package $pkg;

                    import android.content.Intent;
                    import android.os.Bundle;
                    import android.widget.Button;
                    import androidx.appcompat.app.AppCompatActivity;

                    public class MainActivity extends AppCompatActivity {
                        @Override
                        protected void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);
                            setContentView(R.layout.activity_main);
                            findViewById(R.id.next).setOnClickListener(v ->
                                startActivity(new Intent(this, SecondActivity.class))
                            );
                        }
                    }
                    """.trimIndent()
                )
                write(
                    sourceDir, "SecondActivity.java",
                    """
                    package $pkg;

                    import android.os.Bundle;
                    import androidx.appcompat.app.AppCompatActivity;

                    public class SecondActivity extends AppCompatActivity {
                        @Override
                        protected void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);
                            setContentView(R.layout.activity_second);
                        }
                    }
                    """.trimIndent()
                )
            }
            ProjectTemplate.NO_ACTIVITY, ProjectTemplate.LIBRARY_MODULE -> Unit
            ProjectTemplate.EMPTY_COMPOSE_ACTIVITY -> error("Compose templates require Kotlin.")
        }
    }

    private fun viewsMainLayout(config: ProjectConfig): String =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:padding="24dp">
            <TextView
                android:id="@+id/title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Hello from Forge" />
            ${if (config.template == ProjectTemplate.BASIC_VIEWS_NAVIGATION) """
                <Button
                    android:id="@+id/next"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Next" />
            """.trimIndent() else ""}
        </LinearLayout>
        """.trimIndent()

    private fun viewsSecondLayout(): String =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:padding="24dp">
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Second screen" />
        </LinearLayout>
        """.trimIndent()

    private fun write(file: File, relative: String, content: String) {
        val target = File(file, relative)
        target.parentFile?.mkdirs()
        target.writeText(content + "\n")
    }
}
