//
//  RegularView.swift
//  FeedFlow
//
//  Created by Marco Gomiero on 21/10/23.
//  Copyright © 2023 FeedFlow. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync

struct RegularView: View {
    @EnvironmentObject
    var appState: AppState

    @State
    var navDrawerState: NavDrawerState = NavDrawerState(timeline: [], categories: [], feedSourcesByCategory: [:])

    @Binding
    var selectedDrawerItem: DrawerItem?

    @State
    var scrollUpTrigger: Bool = false

    var drawerItems: [DrawerItem] = []
    let homeViewModel: HomeViewModel

    var body: some View {
        NavigationSplitView {
            SidebarDrawer(
                selectedDrawerItem: $selectedDrawerItem,
                navDrawerState: navDrawerState,
                onFeedFilterSelected: { feedFilter in
                    scrollUpTrigger.toggle()
                    homeViewModel.onFeedFilterSelected(selectedFeedFilter: feedFilter)
                }
            )
            .navigationBarTitleDisplayMode(.inline)
        } detail: {
            NavigationStack {
                HomeScreen(
                    toggleListScroll: $scrollUpTrigger,
                    homeViewModel: homeViewModel
                ).navigationDestination(for: CommonRoute.self) { route in
                    switch route {
                    case .aboutScreen:
                        AboutScreen()

                    case .importExportScreen:
                        ImportExportScreen()
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
        .navigationSplitViewStyle(.balanced)
        .task {
            do {
                let stream = asyncSequence(for: homeViewModel.navDrawerStateFlow)
                for try await state in stream {
                    self.navDrawerState = state
                }
            } catch {
                self.appState.emitGenericError()
            }
        }
    }
}
