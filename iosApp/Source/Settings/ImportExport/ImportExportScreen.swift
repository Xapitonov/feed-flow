//
//  ImportExportScreen.swift
//  FeedFlow
//
//  Created by Marco Gomiero on 01/09/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared
import KMPNativeCoroutinesAsync

struct ImportExportScreen: View {

    @EnvironmentObject var appState: AppState
    @StateObject var viewModel = KotlinDependencies.shared.getImportExportViewModel()

    @State var feedImportExportState: FeedImportExportState = FeedImportExportState.Idle()

    @State var sheetToShow: ImportExportSheetToShow?

    var body: some View {
        ImportExportContent(
            feedImportExportState: $feedImportExportState,
            sheetToShow: $sheetToShow,
            onExportClick: {
                viewModel.startExport()
                if let url = getUrlForOpmlExport() {
                    viewModel.exportFeed(opmlOutput: OpmlOutput(url: url))
                } else {
                    viewModel.reportExportError()
                }
            },
            onRetryClick: {
                viewModel.clearState()
            },
            onDoneClick: {
                viewModel.clearState()
            }
        )
        .navigationTitle(MR.strings().import_export_opml.localized)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            do {
                let stream = asyncSequence(for: viewModel.importExportStateFlow)
                for try await state in stream {
                    self.feedImportExportState = state
                    if state is FeedImportExportState.ExportSuccess {
                        self.sheetToShow = .shareSheet
                    }
                }
            } catch {
                self.appState.snackbarQueue.append(
                    SnackbarData(
                        title: MR.strings().generic_error_message.localized,
                        subtitle: nil,
                        showBanner: true
                    )
                )
            }
        }
        .sheet(item: $sheetToShow) { item in
            switch item {
            case .filePicker:
                FilePickerController { url in
                    do {
                        let data = try Data(contentsOf: url)
                        viewModel.importFeed(opmlInput: OpmlInput(opmlData: data))
                    } catch {
                        viewModel.reportExportError()
                        self.appState.snackbarQueue.append(
                            SnackbarData(
                                title: MR.strings().load_file_error_message.localized,
                                subtitle: nil,
                                showBanner: true
                            )
                        )
                    }
                }

            case .shareSheet:

                ShareSheet(
                    activityItems: [getUrlForOpmlExport()! as URL],
                    applicationActivities: nil
                ) { _, _, _, _ in }
            }

            //        Text("Hello World")
        }

    }
}

struct ImportExportContent: View {

    @Environment(\.presentationMode) var presentationMode

    @Binding var feedImportExportState: FeedImportExportState
    @Binding var sheetToShow: ImportExportSheetToShow?

    let onExportClick: () -> Void
    let onRetryClick: () -> Void
    let onDoneClick: () -> Void

    var body: some View {

        switch feedImportExportState {
        case is FeedImportExportState.Idle:
            ImportExportIdleView(
                onImportClick: {
                    self.sheetToShow = .filePicker
                },
                onExportClick: onExportClick
            )

        case is FeedImportExportState.Error:
            ImportExportErrorView(
                onRetryClick: onRetryClick
            )

        case is FeedImportExportState.LoadingImport:
            ImportExportLoadingView(
                message: MR.strings().feed_add_in_progress_message.localized
            )

        case is FeedImportExportState.LoadingExport:
            ImportExportLoadingView(
                message: MR.strings().export_started_message.localized
            )

        case is FeedImportExportState.ExportSuccess:
            ExportDoneView(
                onDoneClick: {
                    onDoneClick()
                    self.sheetToShow = nil
                    self.presentationMode.wrappedValue.dismiss()
                }
            )

        case let state as FeedImportExportState.ImportSuccess:
            ImportDoneView(
                onDoneClick: {
                    onDoneClick()
                    self.sheetToShow = nil
                    self.presentationMode.wrappedValue.dismiss()
                },
                feedSources: state.notValidFeedSources
            )

        default:
            Text("Aronne")
        }

    }
}

struct ImportExportIdleView: View {

    let onImportClick: () -> Void
    let onExportClick: () -> Void

    var body: some View {
        VStack {
            Form {
                Button(
                    action: onImportClick,
                    label: {
                        Label(
                            MR.strings().import_feed_button.localized,
                            systemImage: "arrow.down.doc"
                        )
                    }
                )

                Button(
                    action: onExportClick,
                    label: {
                        Label(
                            MR.strings().export_feeds_button.localized,
                            systemImage: "arrow.up.doc"
                        )
                    }
                )
            }

            Spacer()
        }
    }
}

struct ImportExportErrorView: View {

    let onRetryClick: () -> Void

    var body: some View {
        VStack {
            Spacer()

            Text(MR.strings().generic_error_message.localized)
                .font(.body)

            Button(
                MR.strings().retry_button.localized,
                action: onRetryClick
            )
            .buttonStyle(.bordered)
            .padding(.top, Spacing.regular)

            Spacer()
        }
    }
}

struct ImportExportLoadingView: View {

    let message: String

    var body: some View {
        VStack {
            Spacer()

            ProgressView()

            Text(message)
                .padding(Spacing.regular)
                .font(.body)
                .multilineTextAlignment(.center)

            Spacer()
        }
    }
}

struct ExportDoneView: View {

    let onDoneClick: () -> Void

    var body: some View {
        VStack {
            Spacer()

            Text(MR.strings().feeds_export_done_message.localized)
                .font(.body)
                .multilineTextAlignment(.center)

            Button(
                MR.strings().done_button.localized,
                action: onDoneClick
            )
            .buttonStyle(.bordered)
            .padding(.top, Spacing.regular)

            Spacer()
        }
    }
}

struct ImportDoneView: View {

    let onDoneClick: () -> Void
    let feedSources: [ParsedFeedSource]

    var body: some View {
        VStack {

            if feedSources.isEmpty {

                Spacer()

                Text(MR.strings().feeds_import_done_message.localized)
                    .font(.body)
                    .multilineTextAlignment(.center)

                Button(
                    MR.strings().done_button.localized,
                    action: onDoneClick
                )
                .buttonStyle(.bordered)
                .padding(.top, Spacing.regular)

                Spacer()
            } else {

                Text(MR.strings().wrong_link_report_title.localized)
                    .font(.body)
                    .padding(Spacing.regular)

                List {
                    ForEach(feedSources, id: \.self.url) { feedSource in
                        VStack(alignment: .leading) {
                            Text(feedSource.title)
                                .font(.system(size: 16))
                                .padding(.top, Spacing.xsmall)

                            Text(feedSource.url)
                                .font(.system(size: 14))
                                .padding(.top, Spacing.xxsmall)
                                .padding(.bottom, Spacing.xsmall)

                        }
                    }
                }
                .listStyle(PlainListStyle())

                Spacer()

                Button {
                    onDoneClick()
                } label: {
                    Text(MR.strings().done_button.localized)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .padding(Spacing.regular)

            }
        }
    }
}

private func getUrlForOpmlExport() -> URL? {
    let cacheDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first
    return cacheDirectory?.appendingPathComponent("feed-export.opml")
}

struct ImportExportScreen_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            ForEach(PreviewItemsKt.importExportStates, id: \.self) { state in
                ImportExportContent(
                    feedImportExportState: .constant(state),
                    sheetToShow: .constant(nil),
                    onExportClick: {},
                    onRetryClick: {},
                    onDoneClick: {}
                )
            }
        }
    }
}