# Jdeserialize

[![pipeline status](https://git.fh-aachen.de/tb3838s/jdeserialize/badges/main/pipeline.svg)](https://git.fh-aachen.de/tb3838s/jdeserialize/-/commits/main)  
[![Latest Release](https://git.fh-aachen.de/tb3838s/jdeserialize/-/badges/release.svg)](https://git.fh-aachen.de/tb3838s/jdeserialize/-/releases)

https://code.google.com/archive/p/jdeserialize/

## History

### Source

Jdeserialize was written by Brandon Creighton <cstone@pobox.com> and is archived at https://code.google.com/archive/p/jdeserialize/.

This repository contains advanced packaging for the project.

### License

The original project is licensed under the *New BSD License* as stated at https://code.google.com/archive/p/jdeserialize/.  
The available source does not contain a copy of the license; therefore, a copy from https://opensource.org/license/BSD-3-Clause was added under `./jdeserialize/ORIGINAL-LICENSE.md`.

## How to use?

There are multiple ways to obtain a compiled version of Jdeserialize:

### Nix (Recommended / Stable)

If you are familiar with [Nix](https://nixos.org/), you can use the Nix flake of this repository available under the flake URL:

```
git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```

Running without installing:
```bash
nix run git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```

Installing to a temporary shell:
```bash
nix shell git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```

Or feel free to add Jdeserialize to your NixOS configuration:

```nix
# flake input:
inputs = {
  # nixpkgs etc...
  jdeserialize.url = "git+https://git.fh-aachen.de/tb3838s/jdeserialize.git";
};

# Install jdeserialize package from flake (in a NixOS module)
environment.systemPackages = [
  jdeserialize.packages.${system}.default
];
```

The Nix package comes with the following structure:

```
├── bin
│   └── jdeserialize
├── lib
│   └── jdeserialize.jar
└── share
    ├── doc
    │   └── jdeserialize
    │       └── javadoc
    └── licenses
        └── jdeserialize
            └── ORIGINAL-LICENSE.md
```

_(“javadoc” is a folder containing the generated Javadoc as HTML)_

### Download prebuilt binary (Easy)

If you want to download a prebuilt binary (Java Archive), you can either use the releases section or download the artifacts from the latest successful build on the main branch.

**Releases:**  
Navigate to https://git.fh-aachen.de/tb3838s/jdeserialize/-/releases/ and download the `jdeserialize.jar` file or the linked archive.

**Job Artifacts:**  
Navigate to https://git.fh-aachen.de/tb3838s/jdeserialize/-/artifacts and download the artifacts archive of the latest successful build on the main branch.

### Compiling manually

If you want to compile the source on your machine, you can use the Nix build command:

```bash
nix build git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```

(or with source checked out)

```bash
git clone https://git.fh-aachen.de/tb3838s/jdeserialize.git
cd jdeserialize
nix build
```

This will produce a `result` symlink to a folder in your Nix store.

If you want to build without Nix, you can simply run `ant` and `ant javadoc` in the `jdeserialize` subfolder of the repository.
