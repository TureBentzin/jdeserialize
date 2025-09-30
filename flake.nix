{
  description = "jdeserialize flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
        jdk = pkgs.jdk21;
        junit =
          let
            pname = "junit-platform-console-standalone";
            version = "6.0.0-RC2";
            hash = "sha256-Ecoot6gbs+gSZOZrS+8dYZSBizPjurAKcoEAR6qg/pA=";
          in
          pkgs.stdenv.mkDerivation {
            inherit pname version;

            src = pkgs.fetchurl {
              url = "https://repo1.maven.org/maven2/org/junit/platform/${pname}/${version}/${pname}-${version}.jar";
              inherit hash;
            };

            nativeBuildInputs = [ pkgs.makeWrapper ];

            unpackPhase = "true";

            installPhase = ''
              mkdir -p $out/lib
              cp $src $out/lib/$pname.jar

              mkdir -p $out/bin
              makeWrapper ${jdk}/bin/java $out/bin/junit \
                --add-flags "-jar $out/lib/$pname.jar"
            '';
          };

        packages = [
          pkgs.ant
          jdk
          junit
          pkgs.makeWrapper
        ];

      in
      {
        devShells.default = pkgs.mkShell {
          inherit packages;
        };

        packages.default =
          let
            jre = pkgs.jre21_minimal;
          in
          pkgs.stdenv.mkDerivation {
            pname = "jdeserialize";
            version = "2.1";
            src = ./jdeserialize;
            buildInputs = [ ];
            nativeBuildInputs = packages;
            buildPhase = ''
              ant
              ant javadoc
            '';
            installPhase = ''
              mkdir -p $out/{bin,lib}
              mkdir -p $out/share/{doc,licenses}/jdeserialize/

              cp jdeserialize.jar $out/lib/
              cp -r javadoc $out/share/doc/jdeserialize/javadoc
              cp ORIGINAL-LICENSE.md $out/share/licenses/jdeserialize/

              makeWrapper ${jre}/bin/java $out/bin/jdeserialize --add-flags "-jar $out/lib/jdeserialize.jar"
            '';
            # meta = {};
          };

        apps.default = flake-utils.lib.mkApp {
          drv = self.packages.${system}.default;
        };
      }
    );

}
