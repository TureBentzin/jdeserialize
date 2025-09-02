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
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            ant
            openjdk
          ];
        };

        packages.default = pkgs.hello;

        apps.default = flake-utils.lib.mkApp {
          drv = pkgs.hello;
        };
      }
    );

}
