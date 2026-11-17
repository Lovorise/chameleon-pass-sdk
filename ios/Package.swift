// swift-tools-version: 6.0
// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import PackageDescription

let package = Package(
    name: "ChameleonPassUI",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "ChameleonPassUI",
            targets: ["ChameleonPassUI"]
        )
    ],
    targets: [
        .target(
            name: "ChameleonPassUI",
            path: "Sources/ChameleonPassUI",
            swiftSettings: [
                .enableExperimentalFeature("StrictConcurrency")
            ]
        ),
        .testTarget(
            name: "ChameleonPassUITests",
            dependencies: ["ChameleonPassUI"],
            path: "Tests/ChameleonPassUITests"
        )
    ]
)
