/* --------------------------- */
/* This script is super janky, */
/* do not take it seriously.   */
/* --------------------------- */

import fs from "fs";

// https://github.com/Casterlabs/Commons/blob/main/Platform/src/main/java/co/casterlabs/commons/platform/Arch.java
const ARCHES = {
  X86_64: /x64/gi,
  X86: /x86/gi,
  AARCH64: /aarch64/gi,
  ARM: /arm/gi,
};

// https://github.com/Casterlabs/Commons/blob/main/Platform/src/main/java/co/casterlabs/commons/platform/OSDistribution.java
const OSES = {
  WINDOWS_NT: /windows/gi,
  LINUX: /_linux/gi,
  MACOS: /mac/gi,
};

const VERSIONS = ["19", "18", "17", "16", "11", "8"]; // These are the supported versions by Temurin.
const LATEST = VERSIONS[0];

function getArch(str) {
  for (const [key, value] of Object.entries(ARCHES)) {
    if (str.match(value)) {
      return key;
    }
  }
}

function getOS(str) {
  for (const [key, value] of Object.entries(OSES)) {
    if (str.match(value)) {
      return key;
    }
  }
}

function getType(str) {
  if (str.match(/-jdk_/)) {
    return "jdk";
  } else if (str.match(/-jre_/)) {
    return "jre";
  }
}

function getFormat(str) {
  if (str.endsWith(".tar.gz")) {
    return "tar.gz";
  } else if (str.endsWith(".zip")) {
    return "zip";
  }
}

function parseReleaseInfo(json) {
  let patch;
  let version;

  // jdk8u352-b08, jdk-19.0.1+10
  if (json.name.includes("jdk8u")) {
    patch = json.name.substring(json.name.indexOf("u") + 1);
    version = "8";
  } else {
    patch = json.name.substring(json.name.indexOf("-") + 1);
    version = patch.substring(0, patch.indexOf("."));
  }

  console.log("Found:", version, patch);

  const jdkBinaries = {};
  const jreBinaries = {};

  for (const asset of json.assets) {
    const arch = getArch(asset.name);
    const os = getOS(asset.name);
    const type = getType(asset.name);
    const format = getFormat(asset.name);
    if (!arch || !os || !type || !format) continue;

    // console.log(`Binary ${type}: ${os}/${arch} (${format})`);
    const binaries = type == "jdk" ? jdkBinaries : jreBinaries;
    const url = asset.browser_download_url;

    if (!binaries[arch]) {
      binaries[arch] = {};
    }

    binaries[arch][os] = url;
  }

  return [
    [
      "java-temurin-jdk",
      version,
      {
        patch: patch,
        depends: ["java:" + version],
        binaries: jdkBinaries,
        commands: {
          java: "/bin/java %@",
          javac: "/bin/javac %@",
        },
        extract: {
          base: json.name, // Move into this sub-folder.
          keep: [
            "/bin/.*",
            "/lib/.*",
            "/ASSEMBLY_EXCEPTION",
            "/LICENSE",
            "/NOTICE",
            "/THIRD_PARTY_README",
          ],
        },
      },
    ],
    [
      "java-temurin-jre",
      version,
      {
        patch: patch,
        depends: [],
        binaries: jreBinaries,
        commands: {
          java: "/bin/java %@",
        },
        extract: {
          base: json.name + "-jre", // Move into this sub-folder.
          discard: ["release"],
        },
      },
    ],
  ];
}

const packages = {
  "java-temurin-jdk": {
    __comment:
      "Autogenerated by java/temurin/generate.mjs. DO NOT MODIFY BY HAND.",
    slug: "java-temurin-jdk",
    aliases: ["jdk"],
    latest: LATEST,
    versions: {},
  },
  "java-temurin-jre": {
    __comment:
      "Autogenerated by java/temurin/generate.mjs. DO NOT MODIFY BY HAND.",
    slug: "java-temurin-jre",
    aliases: ["java"],
    latest: LATEST,
    versions: {},
  },
};

async function fetchAndAdd(v) {
  const response = await fetch(
    `https://api.github.com/repos/adoptium/temurin${v}-binaries/releases/latest`
  );
  const json = await response.json();

  for (const [slug, version, versionInfo] of parseReleaseInfo(json)) {
    packages[slug].versions[version] = versionInfo;
  }
}

(async () => {
  for (const version of VERSIONS) {
    await fetchAndAdd(version);
  }

  for (const pkg of Object.values(packages)) {
    const filename = pkg.slug.substring(pkg.slug.lastIndexOf("-") + 1);

    fs.writeFileSync(
      `${filename}.packagelist.json`,
      JSON.stringify([pkg], null, 2)
    );
  }

  console.log("Done.");
})();
