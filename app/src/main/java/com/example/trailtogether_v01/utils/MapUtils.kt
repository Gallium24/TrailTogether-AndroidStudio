package com.example.trailtogether_v01.utils

import org.osmdroid.tileprovider.tilesource.XYTileSource
object MapUtils {


    // Add this definition for the new map style
    val OpenTopoMapSource = XYTileSource(
        "OpenTopoMap",
        1, 17, 256, ".png",
        arrayOf(
            "https://a.tile.opentopomap.org/",
            "https://b.tile.opentopomap.org/",
            "https://c.tile.opentopomap.org/"
        ),
        "© OpenTopoMap (CC-BY-SA)"
    )

}