# Jdeserialize

[![pipeline status](https://git.fh-aachen.de/tb3838s/jdeserialize/badges/main/pipeline.svg)](https://git.fh-aachen.de/tb3838s/jdeserialize/-/commits/main)
[![Latest Release](https://git.fh-aachen.de/tb3838s/jdeserialize/-/badges/release.svg)](https://git.fh-aachen.de/tb3838s/jdeserialize/-/releases)

https://code.google.com/archive/p/jdeserialize/

## History

### Source

Jdeserialize was written by Brandon Creighton <cstone@pobox.com> and is archived on 'https://code.google.com/archive/p/jdeserialize/'!

This repository contains advanced packaging for the project.

### License

The original project is licensed under the 'New BSD License' as stated on 'https://code.google.com/archive/p/jdeserialize/'.
The available source does not contain a copy of said license, therefore a copy sourced from 'https://opensource.org/license/BSD-3-Clause' was added under './jdeserialize/ORIGINAL-LICENSE.md'


## How to use?

There are multiple ways to get your hands onto a compiled version of jdserialize:

### Nix (Recommended / Stable)
If you are fammiliar with [Nix](https://nixos.org/), you can use the nix flake of this repository available under the flake url "git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/"

Running without installing:
```bash
  nix run git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```

Installing to a temporary shell:
```bash
nix shell git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```

Or feel free to add jdserialize to your nixos configuration:

```nix
  # flake input:
  inputs = {
    # nixpkgs etc...
    jdeserialize.url = "git+https://git.fh-aachen.de/tb3838s/jdeserialize.git";
  };


  # Install jdeserialize package from flake (in a nixos module)
  environment.systemPackages = [
    jdeserialize.packages.${system}.default
  ];
```

The nix package comes with the following structure:

```
├── bin
│   └── jdeserialize
├── lib
│   └── jdeserialize.jar
└── share
    ├── doc
    │   └── jdeserialize
    │       └── javadoc
    └── licenses
        └── jdeserialize
            └── ORIGINAL-LICENSE.md
```
_(javadoc beeing a folder containing the javadoc as HTML)_

### Download prebuild binary (Easy)

If you want to download a prebuild binary (Java Archive), you can either use the releases section or download the artifacts from the latest successfull build on the main branch.

**Releases: **
Navigate to 'https://git.fh-aachen.de/tb3838s/jdeserialize/-/releases/' and download the jdserialize.jar file or the linked archive.

**Job Artifacts: **
Navigate to 'https://git.fh-aachen.de/tb3838s/jdeserialize/-/artifacts' and download the artifacts archive of the latest successfull build on the main branch.

### Compiling manually

If you want to compile the source on your machine, you can use the nix build command:

```
  nix build git+https://git.fh-aachen.de/tb3838s/jdeserialize.git/
```
(or with source checked out)
```
  git clone https://git.fh-aachen.de/tb3838s/jdserialize.git
  cd jdserialize
  nix build
```

This will produce a 'result' symlink to a folder in your nix store.

If you want to build without nix, you can just run `ant` and `ant javadoc` in the jdserialize subfolder of the repository.
