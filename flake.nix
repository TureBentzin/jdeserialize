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
            buildInputs = [ jre ];
            nativeBuildInputs = packages;
            # installPhase = "";
            # buildPhase = "";
            # meta = {};
          };

        apps.default = flake-utils.lib.mkApp {
          drv = self.packages.${system}.default;
        };
      }
    );

}
