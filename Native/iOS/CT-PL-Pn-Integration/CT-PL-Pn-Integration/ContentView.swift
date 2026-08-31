//
//  ContentView.swift
//  CT-PL-Pn-Integration
//
//  Created by Work on 31/08/26.
//

import SwiftUI
import CleverTapSDK
import Plotline

struct ContentView: View {
    @ObservedObject private var deepLinkModal = DeepLinkModalManager.shared

    var body: some View {
        VStack(spacing: 16) {
            Text("CleverTap + Plotline Integration")

            Button("Track CleverTap Event") {
                CleverTap.sharedInstance()?.recordEvent("CT Test Event")
            }

            Button("Track Plotline Event") {
                Plotline.track(eventName: "Plotline Test Event", properties: ["category": "testing"])
            }
        }
        .padding()
        .alert(
            "Deeplink from \(deepLinkModal.data?.source ?? "")",
            isPresented: Binding(
                get: { deepLinkModal.data != nil },
                set: { if !$0 { deepLinkModal.dismiss() } }
            )
        ) {
            Button("OK", role: .cancel) { deepLinkModal.dismiss() }
        } message: {
            Text(deepLinkModal.data?.deepLink ?? "")
        }
    }
}

#Preview {
    ContentView()
}