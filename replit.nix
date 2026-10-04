{ pkgs }:

let
  javafxLibraries = [
    pkgs.glib
    pkgs.gtk3
    pkgs.pango
    pkgs.cairo
    pkgs.gdk-pixbuf
    pkgs.at-spi2-core

    pkgs.xorg.libX11
    pkgs.xorg.libXxf86vm
    pkgs.libGL
    pkgs.xorg.libXtst
  ];
in
{
  deps = [
    pkgs.unzip
    pkgs.openjdk17
    pkgs.maven
  ] ++ javafxLibraries;

  env = {
    LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath javafxLibraries;
  };
}
