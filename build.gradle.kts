import org.apache.commons.lang3.SystemUtils

plugins {
	idea
	java
	id("gg.essential.loom") version "0.10.0.+"
	id("dev.architectury.architectury-pack200") version "0.1.3"
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

//Constants:
val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val mixinGroup = "$baseGroup.mixin"
val modid: String by project
val beta: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")

fun createFileName(): String {
	return if (beta.toBoolean()) {
		"${rootProject.name}-beta"
	} else {
		rootProject.name
	}
}

// Toolchains:
java {
	withSourcesJar()
	toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration:
loom {
	log4jConfigs.from(file("log4j2.xml"))
	runConfigs {
		"client" {
			// If you don't want mixins, remove these lines

//			arg("--tweakClass", "com.golem.deps.io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker")
		}
//	launchConfigs {
//		"client" {
//			// If you don't want mixins, remove these lines
//			property("mixin.debug", "true")
//			property("asmhelper.verbose", "true")
//			property("devauth.enabled", "true")
//			arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
//			arg("--tweakClass", "io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker")
//			arg("--mixin", "mixins.skyblockutils.json")
////			arg("--tweakClass", "com.golem.deps.io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker")
//		}
	}
	runConfigs {
		"client" {
			property("mixin.debug", "true")
			property("asmhelper.verbose", "true")
			property("devauth.enabled", "true")
			programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
			programArgs("--tweakClass", "io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker")
			programArgs("--mixin", "mixins.skyblockutils.json")
			
			if (SystemUtils.IS_OS_MAC_OSX) {
				// This argument causes a crash on macOS
				vmArgs.remove("-XstartOnFirstThread")
			}
		}
		remove(getByName("server"))
	}
	forge {
		pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
		// If you don't want mixins, remove this lines
		mixinConfig("mixins.$modid.json")
		println("mixins.$modid.json")
		if (transformerFile.exists()) {
			println("Installing access transformer")
			accessTransformer(transformerFile)
		} else {
			println("Skipping access transformers")
		}
	}
	// If you don't want mixins, remove these lines
	mixin {
		useLegacyMixinAp.set(true)
		defaultRefmapName.set("mixins.$modid.refmap.json")
	}
}

sourceSets.main {
	output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies:

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://repo.spongepowered.org/maven/")
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	/*
		Essentials
	 */
//	maven("https://repo.sk1er.club/repository/maven-public")
//	maven("https://repo.sk1er.club/repository/maven-releases/")
	maven("https://repo.essential.gg/repository/maven-public")
	maven("https://repo.essential.gg/repository/maven-releases")
	/*
		NEU
	 */
	maven("https://maven.notenoughupdates.org/releases/")
}

val shadowImpl: Configuration by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}

dependencies {
	minecraft("com.mojang:minecraft:1.8.9")
	mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
	forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
	
	// If you don't want mixins, remove these lines
	/*shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
		isTransitive = false
	}*/
	implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
//	implementation("org.spongepowered:mixin:0.8.5-SNAPSHOT")
	annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
	runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
	
	// Our dependency
	implementation("org.projectlombok:lombok:1.18.26")
	annotationProcessor("org.projectlombok:lombok:1.18.26")
	// Our logging dependency
//	implementation("com.github.VlxtIykg:Logger:1.1.0")
	shadowImpl("com.github.VlxtIykg:Logger:1.1.0")
	// Old essential GUI -> Porting to Moul Config
	implementation("gg.essential:loader-launchwrapper:1.1.3")
	shadowImpl("org.notenoughupdates.moulconfig:legacy:3.5.0")
//	implementation("org.notenoughupdates.moulconfig:legacy:3.5.0")
	implementation("gg.essential:essential-1.8.9-forge:2666")
}

// Tasks:

tasks.withType(JavaCompile::class) {
	options.encoding = "UTF-8"
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
	archiveBaseName.set(createFileName())
	manifest.attributes.run {
		this["FMLCorePlugin"] = "com.golem.skyblockutils.injection.FMLPlugin"
		this["FMLCorePluginContainsFMLMod"] = "true"
		this["ForceLoadAsMod"] = "true"
		
		// If you want to disable mixins, remove these lines
		this["TweakClass"] = "com.golem.deps.io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker"
		this["MixinConfigs"] = "mixins.$modid.json"
		if (transformerFile.exists())
			this["FMLAT"] = "${modid}_at.cfg"
	}
}

tasks.processResources {
	inputs.property("version", project.version)
	inputs.property("mcversion", mcVersion)
	inputs.property("modid", modid)
	inputs.property("basePackage", baseGroup)
	
	filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
		expand(inputs.properties)
	}
	
	rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}


val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
	archiveClassifier.set("")
	from(tasks.shadowJar)
	input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
	archiveClassifier.set("without-deps")
	destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
	destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
	archiveClassifier.set("non-obfuscated-with-deps")
	configurations = listOf(shadowImpl)
	doLast {
		configurations.forEach {
			println("Copying dependencies into mod: ${it.files}")
		}
	}
	
	// If you want to include other dependencies and shadow them, you can relocate them in here
	fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
	relocate("io.github.notenoughupdates.moulconfig")
	relocate("logger")
	println("Mods moved")
}

tasks.assemble.get().dependsOn(tasks.remapJar)

