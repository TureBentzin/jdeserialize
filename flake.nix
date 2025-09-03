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
        packages = [
          pkgs.ant
          pkgs.jdk17
          pkgs.makeWrapper
        ];
      in
      {
        devShells.default = pkgs.mkShell {
          inherit packages;
        };

        packages.default =
          let
            jre = pkgs.jre17_minimal;
          in
          pkgs.stdenv.mkDerivation {
            pname = "jdeserialize";
            version = "1.2";
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
