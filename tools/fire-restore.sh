#!/bin/bash

echo "=== VenusKiosk Fire restore ==="

PACKAGES=(
  com.amazon.photos
  com.amazon.photos.importer
  com.amazon.weather
  com.amazon.calculator
  com.amazon.mp3
  com.amazon.kindle
  com.amazon.kindle.personal_video
  com.amazon.avod
  com.amazon.ags.app
  com.amazon.windowshop
  com.amazon.vans.alexatabletshopping.app
  com.amazon.firespotlight
  com.audible.application.kindle
  com.goodreads.kindle
)

for pkg in "${PACKAGES[@]}"; do
    echo "Enabling $pkg"
    adb shell pm enable "$pkg"
done

echo
echo "Restore completed."
