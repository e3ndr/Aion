import fs from "fs";

const GO_VERSIONS = ["1.19.3", "1.18.8"];
let versions = {};

for (const version of GO_VERSIONS) {
  const major = version.substring(0, version.lastIndexOf("."));

  versions[major] = {
    patch: version,
    depends: [],
    binaries: {
      X86_64: {
        WINDOWS_NT: `https://go.dev/dl/go${version}.windows-amd64.zip`,
        MACOS: `https://go.dev/dl/go${version}.darwin-amd64.tar.gz`,
        LINUX: `https://go.dev/dl/go${version}.linux-amd64.tar.gz`,
      },
      X86: {
        WINDOWS_NT: `https://go.dev/dl/go${version}.windows-386.zip`,
        LINUX: `https://go.dev/dl/go${version}.linux-386.tar.gz`,
      },
      AARCH64: {
        WINDOWS_NT: `https://go.dev/dl/go${version}.windows-arm64.zip`,
        MACOS: `https://go.dev/dl/go${version}.darwin-arm64.tar.gz`,
        LINUX: `https://go.dev/dl/go${version}.linux-arm64.tar.gz`,
      },
      ARM: {
        LINUX: `https://go.dev/dl/go${version}.linux-armv6l.tar.gz`,
      },
    },
    commands: {
      go: "bin/go",
    },
    extract: {
      base: "go",
    },
  };
}

const latestMajor = GO_VERSIONS[0].substring(
  0,
  GO_VERSIONS[0].lastIndexOf(".")
);

const pkg = {
  slug: "go",
  aliases: [],
  latest: latestMajor,
  versions: versions,
};

fs.writeFileSync("go.packagelist.json", JSON.stringify([pkg], null, 2));
